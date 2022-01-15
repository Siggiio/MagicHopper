package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class RuleCount extends Rule {
	private final int counter;
	private final Sign sign;
	private final int line;

	public RuleCount(int counter, Sign sign, int line) {
		this.counter = counter;
		this.sign = sign;
		this.line = line;
	}

	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		sign.setLine(line, "count " + (counter + 1));
		sign.update(true, false);
		return true;
	}
}
