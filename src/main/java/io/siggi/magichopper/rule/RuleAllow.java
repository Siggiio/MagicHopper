package io.siggi.magichopper.rule;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

public class RuleAllow extends Rule {
	private final String[] allowedItems;

	public RuleAllow(String allowedItem) {
		this(new String[]{allowedItem});
	}

	public RuleAllow(String[] allowedItems) {
		this.allowedItems = new String[allowedItems.length];
		for (int i = 0; i < allowedItems.length; i++) {
			this.allowedItems[i] = allowedItems[i].toUpperCase();
		}
	}

	@Override
	public boolean allowItemToEnter(Block hopperBlock, Hopper hopper, ItemStack item) {
		String itemName = item.getType().name();
		for (String r : allowedItems) {
			if (itemName.contains(r))
				return true;
		}
		return false;
	}

	@Override
	public Rule mergeWith(Rule rule) {
		if (rule instanceof RuleAllow) {
			RuleAllow ruleAllow = (RuleAllow) rule;
			String[] newAllowedItems = new String[allowedItems.length + ruleAllow.allowedItems.length];
			System.arraycopy(allowedItems, 0, newAllowedItems, 0, allowedItems.length);
			System.arraycopy(ruleAllow.allowedItems, 0, newAllowedItems, allowedItems.length, ruleAllow.allowedItems.length);
			return new RuleAllow(newAllowedItems);
		}
		return null;
	}
}
