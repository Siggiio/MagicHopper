package io.siggi.magichopper;

import io.siggi.magichopper.rule.Rule;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HopperEventHandler implements Listener {
	private final MagicHopper plugin;
	private final PermissionChecker permissionChecker;

	public HopperEventHandler(MagicHopper plugin, PermissionChecker permissionChecker) {
		this.plugin = plugin;
		this.permissionChecker = permissionChecker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void signChangeEvent(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		String firstLine = ChatColor.stripColor(lines[0]).toUpperCase().replace(" ", "");
		if (firstLine.matches("\\[MH[0-9]*\\]")) {
			if (!permissionChecker.hasPermission(player, "magichopper.use")) {
				event.setLine(0, ChatColor.DARK_RED + "[no permission]");
				return;
			}
			String numberString = firstLine.substring(3, firstLine.length() - 1);
			int number = -1;
			try {
				number = Integer.parseInt(numberString);
			} catch (Exception e) {
			}
			if (number <= 0) {
				Block blockOfSign = Util.getBlockSignIsOn(event.getBlock());
				List<Sign> signsOnBlock = Util.orderSigns(Util.getSignsOnBlock(blockOfSign));
				number = 1;
				boolean changed;
				do {
					changed = false;
					for (Sign sign : signsOnBlock) {
						if (Util.getSignNumber(sign) == number) {
							changed = true;
							number += 1;
						}
					}
				} while (changed);
			}
			event.setLine(0, ChatColor.BLUE + (number == 1 ? "[MH]" : ("[MH " + number + "]")));
			for (int i = 1; i < 4; i++) {
				String line = event.getLine(i);
				if (line.trim().isEmpty())
					continue;
				int spacePosition = line.indexOf(" ");
				if (spacePosition == -1) spacePosition = line.length();
				String rule = line.substring(0, spacePosition).toLowerCase();
				if ((rule.equals("duplicate") && !permissionChecker.hasPermission(player, "magichopper.type.duplicate"))
						|| (!permissionChecker.hasPermission(player, "magichopper.type.standard") && !permissionChecker.hasPermission(player, "magichopper.type." + rule))) {
					event.setLine(i, ChatColor.RED + "[!]");
				}
			}
		}
	}

	@EventHandler
	public void hopperEvent(InventoryMoveItemEvent event) {
		ItemStack item = event.getItem();

		Inventory source = event.getSource();
		InventoryHolder sourceHolder = source.getHolder();
		boolean duplicator = false;
		if (sourceHolder instanceof Hopper) {
			Hopper hopper = (Hopper) sourceHolder;
			Block block = hopper.getBlock();
			if (!allowItemToLeave(block, hopper, item)) {
				event.setCancelled(true);
				return;
			}
			if (Util.isDuplicator(block)) {
				duplicator = true;
			}
		}

		Inventory target = event.getDestination();
		InventoryHolder targetHolder = target.getHolder();
		if (targetHolder instanceof Hopper) {
			Hopper hopper = (Hopper) targetHolder;
			Block block = hopper.getBlock();
			if (!allowItemToEnter(block, hopper, item)) {
				event.setCancelled(true);
				return;
			}
		} else if (targetHolder instanceof Dropper) {
			Dropper dropper = (Dropper) targetHolder;
			Block block = dropper.getBlock();
			if (Util.isAutoDropper(block)) {
				tickLater(block);
			}
		}

		if (!event.isCancelled() && duplicator) {
			event.setCancelled(true);
			target.addItem(item);
		}
	}

	@EventHandler
	public void hopperPickupEvent(InventoryPickupItemEvent event) {
		Inventory inventory = event.getInventory();
		InventoryHolder holder = inventory.getHolder();
		if (!(holder instanceof Hopper))
			return;
		Hopper hopper = (Hopper) holder;
		Block block = hopper.getBlock();
		if (!allowItemToEnter(block, hopper, event.getItem().getItemStack())) {
			event.setCancelled(true);
		}
	}

	private final Set<Block> tickLater = new HashSet<>();
	public void tickLater(Block block) {
		if (tickLater.isEmpty()) {
			(new BukkitRunnable() {
				@Override
				public void run() {
					tickLater();
				}
			}).runTaskLater(plugin, 1L);
		}
		tickLater.add(block);
	}
	private void tickLater() {
		Block[] blocks = tickLater.toArray(new Block[tickLater.size()]);
		tickLater.clear();
		for (Block block : blocks) {
			World world = block.getWorld();
			if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4))
				continue;
			switch (block.getType()) {
				case HOPPER: {
					Hopper hopper = (Hopper) block.getState();
					postEvent(block, hopper);
				}
				break;
				case DROPPER: {
					Dropper dropper = (Dropper) block.getState();
					if (Util.isAutoDropper(block) && !dropper.getInventory().isEmpty()) {
						long oldQuantities = Util.dropperQuantities(dropper);
						dropper.drop();
						long newQuantities = Util.dropperQuantities(dropper);
						tickDroppersPointingTo(block);
						if (oldQuantities != newQuantities) tickLater(block);
					}
				}
				break;
			}
		}
	}

	private boolean allowItemToEnter(Block block, Hopper hopper, ItemStack itemStack) {
		List<Rule> rules = plugin.getRules(block);
		for (Rule rule : rules) {
			if (!rule.allowItemToEnter(block, hopper, itemStack))
				return false;
		}
		tickLater(block);
		return true;
	}

	private boolean allowItemToLeave(Block block, Hopper hopper, ItemStack itemStack) {
		List<Rule> rules = plugin.getRules(block);
		for (Rule rule : rules) {
			if (!rule.allowItemToLeave(block, hopper, itemStack))
				return false;
		}
		tickLater(block);
		return true;
	}

	private void postEvent(Block block, Hopper hopper) {
		List<Rule> rules = plugin.getRules(block);
		for (Rule rule : rules) {
			rule.postEvent(block, hopper);
		}
	}

	private void tickDroppersPointingTo(Block block) {
		for (BlockFace face : BlockFace.values()) {
			if (!face.isCartesian()) continue;
			BlockFace oppositeFace = face.getOppositeFace();
			Block otherBlock = block.getRelative(face);
			BlockState state = otherBlock.getState();
			if (!(state instanceof Dropper)) continue;
			Dropper dropper = (Dropper) state;
			Directional directional = (Directional) dropper.getBlockData();
			if (directional.getFacing() == oppositeFace) {
				tickLater(otherBlock);
			}
		}
	}
}
