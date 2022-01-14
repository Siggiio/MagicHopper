package io.siggi.magichopper.rule;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RuleMatchFurnace extends Rule {
	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		if (hopperBlock.getY() <= hopperBlock.getWorld().getMinHeight())
			return true;
		Block blockBelow = hopperBlock.getRelative(BlockFace.DOWN);
		BlockState state = blockBelow.getState();
		if (!(state instanceof Furnace))
			return true;
		Furnace furnace = (Furnace) state;
		FurnaceInventory inventory = furnace.getInventory();
		ItemStack itemInFurnace = inventory.getSmelting();
		if (itemInFurnace != null && itemInFurnace.getType() != Material.AIR) {
			return itemInFurnace.isSimilar(item);
		}
		Inventory hopperInventory = hopper.getInventory();
		if (hopperInventory.isEmpty())
			return true;
		int hopperSize = hopperInventory.getSize();
		for (int i = 0; i < hopperSize; i++) {
			ItemStack itemInHopper = hopperInventory.getItem(i);
			if (itemInHopper != null && itemInHopper.getType() != Material.AIR) {
				if (itemInHopper.isSimilar(item)) {
					return true;
				}
			}
		}
		return false;
	}
}
