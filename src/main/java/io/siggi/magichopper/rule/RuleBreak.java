package io.siggi.magichopper.rule;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RuleBreak extends Rule {
	private static final Map<Material, BreakableItemOutput> breakableItems;
	private static final Map<Material, BreakableItemOutput> breakableItemsToNuggets;

	static {
		Map<Material, BreakableItemOutput> breakable = new HashMap<>();
		Map<Material, BreakableItemOutput> breakableNugget = new HashMap<>();

		breakable.put(Material.COAL_BLOCK, new BreakableItemOutput(Material.COAL, 9));

		breakable.put(Material.IRON_BLOCK, new BreakableItemOutput(Material.IRON_INGOT, 9));
		breakable.put(Material.RAW_IRON_BLOCK, new BreakableItemOutput(Material.RAW_IRON, 9));
		breakableNugget.put(Material.IRON_INGOT, new BreakableItemOutput(Material.IRON_NUGGET, 9));

		breakable.put(Material.COPPER_BLOCK, new BreakableItemOutput(Material.COPPER_INGOT, 9));
		breakable.put(Material.RAW_COPPER_BLOCK, new BreakableItemOutput(Material.RAW_COPPER, 9));

		breakable.put(Material.GOLD_BLOCK, new BreakableItemOutput(Material.GOLD_INGOT, 9));
		breakable.put(Material.RAW_GOLD_BLOCK, new BreakableItemOutput(Material.RAW_GOLD, 9));
		breakableNugget.put(Material.GOLD_INGOT, new BreakableItemOutput(Material.GOLD_NUGGET, 9));

		breakable.put(Material.LAPIS_BLOCK, new BreakableItemOutput(Material.LAPIS_LAZULI, 9));

		breakable.put(Material.REDSTONE_BLOCK, new BreakableItemOutput(Material.REDSTONE, 9));

		breakable.put(Material.NETHERITE_BLOCK, new BreakableItemOutput(Material.NETHERITE_INGOT, 9));

		breakable.put(Material.DIAMOND_BLOCK, new BreakableItemOutput(Material.DIAMOND, 9));

		breakable.put(Material.EMERALD_BLOCK, new BreakableItemOutput(Material.EMERALD, 9));

		breakable.put(Material.BONE_BLOCK, new BreakableItemOutput(Material.BONE_MEAL, 9));

		breakableItems = Collections.unmodifiableMap(breakable);
		breakableItemsToNuggets = Collections.unmodifiableMap(breakableNugget);
	}

	public RuleBreak(boolean nugget) {
		this.table = nugget ? breakableItemsToNuggets : breakableItems;
	}

	private final Map<Material,BreakableItemOutput> table;

	@Override
	public boolean allowItemToLeave(Block hopperBlock, Hopper hopper, ItemStack item) {
		return !table.containsKey(item.getType());
	}

	@Override
	public void postEvent(Block hopperBlock, Hopper hopper) {

	}

	private static class BreakableItemOutput {
		private final Material material;
		private final int count;

		private BreakableItemOutput(Material material, int count) {
			this.material = material;
			this.count = count;
		}
	}
}
