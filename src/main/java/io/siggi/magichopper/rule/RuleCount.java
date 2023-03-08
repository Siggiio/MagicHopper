package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class RuleCount extends Rule {
	private int counter;
	private final Sign sign;
	private final int line;

	public RuleCount(int counter, Sign sign, int line) {
		this.counter = counter;
		this.sign = sign;
		this.line = line;
	}

	@Override
	public boolean allowItemToLeave(Block hopperBlock, Hopper hopper, ItemStack item) {
		counter += 1;
		sign.setLine(line, "count " + counter);
		sign.update(true, false);
		return true;
	}

	@Override
	public String toString() {
		return "Count: " + counter;
	}
}
