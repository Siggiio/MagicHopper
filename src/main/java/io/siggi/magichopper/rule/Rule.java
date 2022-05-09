package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

public abstract class Rule {
	Rule() {
	}
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		return true;
	}
	public boolean allowItemToLeave(Block hopperBlock, Hopper hopper, ItemStack item) {
		return true;
	}
	public void postEvent(Block hopperBlock, Hopper hopper) {
	}
	public Rule mergeWith(Rule rule) {
		return null;
	}
	@Override
	public abstract String toString();
}
