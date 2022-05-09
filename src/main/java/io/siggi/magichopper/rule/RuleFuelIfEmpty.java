package io.siggi.magichopper.rule;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public class RuleFuelIfEmpty extends Rule {
	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		org.bukkit.block.data.type.Hopper blockData;
		try {
			blockData = (org.bukkit.block.data.type.Hopper) hopperBlock.getBlockData();
		} catch (Exception e) {
			return true;
		}
		if (blockData.getFacing().getModY() != 0)
			return true;
		Block attachedFurnace = hopperBlock.getRelative(blockData.getFacing());
		BlockState state = attachedFurnace.getState();
		if (!(state instanceof Furnace))
			return true;
		Furnace furnace = (Furnace) state;
		FurnaceInventory inventory = furnace.getInventory();
		ItemStack itemInFurnace = inventory.getFuel();
		if (itemInFurnace != null && itemInFurnace.getType() != Material.AIR) {
			return false;
		}
		return hopper.getInventory().isEmpty() && item.getType().isFuel();
	}

	@Override
	public String toString() {
		return "Fuel If Empty";
	}
}
