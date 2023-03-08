package io.siggi.magichopper;

import io.siggi.magichopper.rule.Rule;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
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
						dropper.drop();
						tickLater(block);
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
}
