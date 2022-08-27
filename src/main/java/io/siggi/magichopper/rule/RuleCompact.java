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

public class RuleCompact extends Rule {

	private static final Map<Material, CompactableItemOutput> compactableItems;

	static {
		Map<Material, CompactableItemOutput> compactable = new HashMap<>();
		compactable.put(Material.COAL, new CompactableItemOutput(9, Material.COAL_BLOCK));

		compactable.put(Material.IRON_NUGGET, new CompactableItemOutput(9, Material.IRON_INGOT));
		compactable.put(Material.IRON_INGOT, new CompactableItemOutput(9, Material.IRON_BLOCK));
		compactable.put(Material.RAW_IRON, new CompactableItemOutput(9, Material.RAW_IRON_BLOCK));

		compactable.put(Material.COPPER_INGOT, new CompactableItemOutput(9, Material.COPPER_BLOCK));
		compactable.put(Material.RAW_COPPER, new CompactableItemOutput(9, Material.RAW_COPPER_BLOCK));

		compactable.put(Material.GOLD_NUGGET, new CompactableItemOutput(9, Material.GOLD_INGOT));
		compactable.put(Material.GOLD_INGOT, new CompactableItemOutput(9, Material.GOLD_BLOCK));
		compactable.put(Material.RAW_GOLD, new CompactableItemOutput(9, Material.RAW_GOLD_BLOCK));

		compactable.put(Material.LAPIS_LAZULI, new CompactableItemOutput(9, Material.LAPIS_BLOCK));

		compactable.put(Material.REDSTONE, new CompactableItemOutput(9, Material.REDSTONE_BLOCK));

		compactable.put(Material.NETHERITE_INGOT, new CompactableItemOutput(9, Material.NETHERITE_BLOCK));

		compactable.put(Material.DIAMOND, new CompactableItemOutput(9, Material.DIAMOND_BLOCK));

		compactable.put(Material.EMERALD, new CompactableItemOutput(9, Material.EMERALD_BLOCK));

		compactable.put(Material.BONE_MEAL, new CompactableItemOutput(9, Material.BONE_BLOCK));

		compactable.put(Material.DRIED_KELP, new CompactableItemOutput(9, Material.DRIED_KELP_BLOCK));

		compactable.put(Material.SNOWBALL, new CompactableItemOutput(4, Material.SNOW_BLOCK));

		compactableItems = Collections.unmodifiableMap(compactable);
	}

	@Override
	public boolean allowItemToLeave(Block hopperBlock, Hopper hopper, ItemStack item) {
		return !compactableItems.containsKey(item.getType());
	}

	@Override
	public void postEvent(Block hopperBlock, Hopper hopper) {
		Inventory inventory = hopper.getInventory();
		int size = inventory.getSize();
		for (int i = 0; i < size; i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || !compactableItems.containsKey(item.getType()))
				continue;
			CompactableItemOutput output = compactableItems.get(item.getType());
			if (output == null)
				continue;
			int amount = item.getAmount();
			if (amount < output.requiredInputs)
				continue;
			int remainder = amount % output.requiredInputs;
			int outputAmount = amount / output.requiredInputs;
			ItemStack outputStack = new ItemStack(output.output, outputAmount);
			if (remainder == 0) {
				inventory.setItem(i, null);
				inventory.addItem(outputStack);
			} else if (canFit(inventory, outputStack)) {
				item.setAmount(remainder);
				inventory.setItem(i, item);
				inventory.addItem(outputStack);
			}
			if (compactableItems.containsKey(outputStack.getType())) {
				MagicHopper.tickLater(hopperBlock);
			}
		}
		for (int i = 0; i < size; i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null || item.getType() == Material.AIR)
				continue;
			if (!compactableItems.containsKey(item.getType()))
				break;
			for (int j = i + 1; j < size; j++) {
				ItemStack otherItem = inventory.getItem(j);
				if (otherItem == null || otherItem.getType() == Material.AIR)
					continue;
				if (!compactableItems.containsKey(otherItem.getType())) {
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
		return "Compact";
	}

	private static class CompactableItemOutput {
		private final int requiredInputs;
		private final Material output;

		private CompactableItemOutput(int requiredInputs, Material output) {
			this.requiredInputs = requiredInputs;
			this.output = output;
		}
	}
}
