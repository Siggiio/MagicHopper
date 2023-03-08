package io.siggi.magichopper;

import io.siggi.magichopper.rule.Rule;
import java.util.Collections;
import java.util.List;

public class BlockConfig {
	private final List<Rule> rules;
	private final List<Rule> unmodifiableRules;
	private final boolean duplicator;
	private final boolean autoDropper;

	public BlockConfig(List<Rule> rules, boolean duplicator, boolean autoDropper) {
		this.rules = rules;
		this.unmodifiableRules = Collections.unmodifiableList(rules);
		this.duplicator = duplicator;
		this.autoDropper = autoDropper;
	}

	public List<Rule> getRules() {
		return unmodifiableRules;
	}
	public boolean isDuplicator() {
		return duplicator;
	}
	public boolean isAutoDropper() {
		return autoDropper;
	}
}
