package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

public class RuleBlock extends Rule {
	private final String rule;

	public RuleBlock(String rule) {
		this.rule = rule.toUpperCase();
	}

	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		return !item.getType().name().contains(rule);
	}

	@Override
	public String toString() {
		return "Block: " + this.rule;
	}
}
