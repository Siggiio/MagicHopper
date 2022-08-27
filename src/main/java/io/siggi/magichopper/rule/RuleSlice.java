package io.siggi.magichopper.rule;

import io.siggi.magichopper.MagicHopper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleSlice extends Rule {

	private static final Map<Material, SliceableItemOutput> sliceableItems;
	private static final Map<Material, SliceableItemOutput> diceableItems;

	static {
		Map<Material, SliceableItemOutput> sliceable = new HashMap<>();
		Map<Material, SliceableItemOutput> diceable = new HashMap<>();
		sliceable.put(Material.COAL_BLOCK, new SliceableItemOutput(9, Material.COAL));

		diceable.put(Material.IRON_INGOT, new SliceableItemOutput(9, Material.IRON_NUGGET));
		sliceable.put(Material.IRON_BLOCK, new SliceableItemOutput(9, Material.IRON_INGOT));
		sliceable.put(Material.RAW_IRON_BLOCK, new SliceableItemOutput(9, Material.RAW_IRON));

		sliceable.put(Material.COPPER_BLOCK, new SliceableItemOutput(9, Material.COPPER_INGOT));
		sliceable.put(Material.RAW_COPPER_BLOCK, new SliceableItemOutput(9, Material.RAW_COPPER));

		diceable.put(Material.GOLD_INGOT, new SliceableItemOutput(9, Material.GOLD_NUGGET));
		sliceable.put(Material.GOLD_BLOCK, new SliceableItemOutput(9, Material.GOLD_INGOT));
		sliceable.put(Material.RAW_GOLD_BLOCK, new SliceableItemOutput(9, Material.RAW_GOLD));

		sliceable.put(Material.LAPIS_BLOCK, new SliceableItemOutput(9, Material.LAPIS_LAZULI));

		sliceable.put(Material.REDSTONE_BLOCK, new SliceableItemOutput(9, Material.REDSTONE));

		sliceable.put(Material.NETHERITE_BLOCK, new SliceableItemOutput(9, Material.NETHERITE_INGOT));

		sliceable.put(Material.DIAMOND_BLOCK, new SliceableItemOutput(9, Material.DIAMOND));

		sliceable.put(Material.EMERALD_BLOCK, new SliceableItemOutput(9, Material.EMERALD));

		sliceable.put(Material.BONE_BLOCK, new SliceableItemOutput(9, Material.BONE_MEAL));

		sliceable.put(Material.DRIED_KELP_BLOCK, new SliceableItemOutput(9, Material.DRIED_KELP));

		diceable.putAll(sliceable);

		sliceableItems = Collections.unmodifiableMap(sliceable);
		diceableItems = Collections.unmodifiableMap(diceable);
	}

	private final boolean dice;

	public RuleSlice(boolean dice) {
		this.dice = dice;
	}

	private Map<Material,SliceableItemOutput> getMap() {
		return dice ? diceableItems : sliceableItems;
	}

	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		// to ensure the slicer will always have space to slice items up,
		// don't allow items to enter if there are less than 2 empty spaces
		int emptySpaces = 0;
		Inventory inventory = hopper.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack itemInSlot = inventory.getItem(i);
			if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
				emptySpaces += 1;
			}
		}
		return emptySpaces >= 2;
	}

	@Override
	public boolean allowItemToLeave(Block hopperBlock, Hopper hopper, ItemStack item) {
		return !getMap().containsKey(item.getType());
	}

	@Override
	public void postEvent(Block hopperBlock, Hopper hopper) {
		Map<Material, SliceableItemOutput> sliceable = getMap();
		Inventory inventory = hopper.getInventory();
		int size = inventory.getSize();
		for (int i = 0; i < size; i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || !sliceable.containsKey(item.getType()))
				continue;
			SliceableItemOutput output = sliceable.get(item.getType());
			if (output == null)
				continue;
			int amount = item.getAmount();
			int maxInput = output.output.getMaxStackSize() / output.outputCount;
			int input = Math.min(maxInput, amount);
			int outputAmount = input * output.outputCount;
			int remainder = amount - input;
			ItemStack outputStack = new ItemStack(output.output, outputAmount);
			if (remainder == 0) {
				inventory.setItem(i, null);
				inventory.addItem(outputStack);
			} else if (canFit(inventory, outputStack)) {
				item.setAmount(remainder);
				inventory.setItem(i, item);
				inventory.addItem(outputStack);
			}
			if (sliceable.containsKey(outputStack.getType())) {
				MagicHopper.tickLater(hopperBlock);
			}
		}
		for (int i = 0; i < size; i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || item.getType() == Material.AIR)
				continue;
			if (!sliceable.containsKey(item.getType()))
				break;
			for (int j = i + 1; j < size; j++) {
				ItemStack otherItem = inventory.getItem(j);
				if (otherItem == null || otherItem.getType() == Material.AIR)
					continue;
				if (!sliceable.containsKey(otherItem.getType())) {
					inventory.setItem(j, null);
					inventory.setItem(i, otherItem);
					inventory.addItem(item);
					break;
				}
			}
			break;
		}
		boolean foundEmptySlot = false;
		boolean mustShift = false;
		List<ItemStack> items = new ArrayList<>(5);
		for (int i = 0; i < size; i++) {
			ItemStack item = inventory.getItem(i);
			if (item != null && item.getType() != Material.AIR) {
				items.add(item);
				if (foundEmptySlot)
					mustShift = true;
			} else {
				foundEmptySlot = true;
			}
		}
		if (mustShift) {
			inventory.clear();
			for (ItemStack item : items) {
				inventory.addItem(item);
			}
		}
	}

	public boolean canFit(Inventory inventory, ItemStack item) {
		int maxStackSize = item.getMaxStackSize();
		int spacesAvailable = inventory.getSize() * maxStackSize;
		int size = inventory.getSize();
		for (int i = 0; i < size; i++) {
			ItemStack thisSlot = inventory.getItem(i);
			if (thisSlot == null || thisSlot.getType() == Material.AIR)
				continue;
			if (thisSlot.isSimilar(item))
				spacesAvailable -= thisSlot.getAmount();
			else
				spacesAvailable -= maxStackSize;
		}
		return spacesAvailable >= item.getAmount();
	}

	@Override
	public String toString() {
		return dice ? "Dice" : "Slice";
	}

	private static class SliceableItemOutput {
		private final int outputCount;
		private final Material output;

		private SliceableItemOutput(int outputCount, Material output) {
			this.outputCount = outputCount;
			this.output = output;
		}
	}
}
