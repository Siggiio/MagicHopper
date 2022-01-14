package io.siggi.magichopper;

import io.siggi.magichopper.rule.Rule;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
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

	public HopperEventHandler(MagicHopper plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void signChangeEvent(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[MH]")) {
			if (!player.hasPermission("magichopper.use")) {
				event.setLine(0, ChatColor.DARK_RED + "[no permission]");
				return;
			}
			event.setLine(0, ChatColor.BLUE + "[MH]");
		}
	}

	@EventHandler
	public void hopperEvent(InventoryMoveItemEvent event) {
		ItemStack item = event.getItem();

		Inventory source = event.getSource();
		InventoryHolder sourceHolder = source.getHolder();
		if (sourceHolder instanceof Hopper) {
			Hopper hopper = (Hopper) sourceHolder;
			Block block = hopper.getBlock();
			if (!allowItemToLeave(block, hopper, item)) {
				event.setCancelled(true);
				return;
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
			}).runTask(plugin);
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
			if (block.getType() != Material.HOPPER)
				continue;
			BlockState state = block.getState();
			Hopper hopper = (Hopper) state;
			postEvent(block, hopper);
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
}
