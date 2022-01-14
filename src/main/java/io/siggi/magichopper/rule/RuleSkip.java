package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class RuleSkip extends Rule {
	private final int counter;
	private final int total;
	private final Sign sign;
	private final int line;

	public RuleSkip(int counter, int total, Sign sign, int line) {
		this.counter = counter;
		this.total = total;
		this.sign = sign;
		this.line = line;
	}

	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		if (counter >= total) {
			sign.setLine(line, "skip 0/" + total);
			sign.update(true, false);
		} else {
			sign.setLine(line, "skip " + (counter + 1) + "/" + total);
			sign.update(true, false);
		}
		return counter == 0;
	}
}
