package io.siggi.magichopper;

import io.siggi.magichopper.rule.Rule;
import io.siggi.magichopper.rule.RuleAllow;
import io.siggi.magichopper.rule.RuleBlock;
import io.siggi.magichopper.rule.RuleCompact;
import io.siggi.magichopper.rule.RuleCount;
import io.siggi.magichopper.rule.RuleFuelIfEmpty;
import io.siggi.magichopper.rule.RuleMatchFurnace;
import io.siggi.magichopper.rule.RuleSkip;
import io.siggi.magichopper.rule.RuleSlice;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MagicHopper extends JavaPlugin {
	@Override
	public void onEnable() {
		instance = this;
		PermissionChecker permissionChecker;
		if (new File(getDataFolder(), "permissionless").exists()) {
			permissionChecker = (player, permission) -> player.getGameMode() == GameMode.CREATIVE || !permission.equals("magichopper.type.duplicate");
		} else {
			permissionChecker = Player::hasPermission;
		}
		getServer().getPluginManager().registerEvents(playerEventHandler = new PlayerEventHandler(this, permissionChecker), this);
		getServer().getPluginManager().registerEvents(worldEventHandler = new WorldEventHandler(this), this);
		getServer().getPluginManager().registerEvents(eventHandler = new HopperEventHandler(this), this);
	}

	private static MagicHopper instance = null;
	private PlayerEventHandler playerEventHandler = null;
	private WorldEventHandler worldEventHandler = null;
	private HopperEventHandler eventHandler = null;

	public static void tickLater(Block block) {
		instance.eventHandler.tickLater(block);
	}

	private final BlockFace[] allFaces = new BlockFace[] {
		BlockFace.UP,
		BlockFace.DOWN,
		BlockFace.NORTH,
		BlockFace.EAST,
		BlockFace.SOUTH,
		BlockFace.WEST
	};
	private final Map<Chunk, Map<Block,BlockConfig>> blockConfigs = new HashMap<>();
	private final Function<Chunk,HashMap<Block,BlockConfig>> chunkMapMaker = chunk -> new HashMap<>();
	private final Function<Block,BlockConfig> blockConfigMaker = this::getBlockConfigFromSigns;

	public BlockConfig getBlockConfig(Block block) {
		Map<Block, BlockConfig> chunkMap = blockConfigs.computeIfAbsent(block.getChunk(), chunkMapMaker);
		return chunkMap.computeIfAbsent(block, blockConfigMaker);
	}

	private void doWipeBlockConfig(Block block) {
		Map<Block, BlockConfig> chunkConfigMap = blockConfigs.get(block.getChunk());
		if (chunkConfigMap == null) return;
		chunkConfigMap.remove(block);
		if (chunkConfigMap.isEmpty()) {
			blockConfigs.remove(block.getChunk());
		}
	}

	public void wipeBlockConfig(Block block) {
		doWipeBlockConfig(block);
		for (BlockFace face : allFaces) {
			doWipeBlockConfig(block.getRelative(face));
		}
	}

	public void wipeChunkConfig(Chunk chunk) {
		blockConfigs.remove(chunk);
	}

	public void wipeWorldConfig(World world) {
		for (Chunk chunk : world.getLoadedChunks()) {
			wipeChunkConfig(chunk);
		}
	}

	private BlockConfig getBlockConfigFromSigns(Block hopper) {
		List<Sign> signs = Util.orderSigns(Util.getSignsOnBlock(hopper));
		List<Rule> rules = new ArrayList<>();
		boolean duplicator = false;
		boolean autoDropper = false;
		Consumer<Rule> addRule = (rule) -> {
			for (Iterator<Rule> it = rules.iterator(); it.hasNext(); ) {
				Rule existingRule = it.next();
				Rule merged = rule.mergeWith(existingRule);
				if (merged != null) {
					it.remove();
					rules.add(merged);
					return;
				}
			}
			rules.add(rule);
		};
		for (Sign sign : signs) {
			int lineIdx = -1;
			for (String line : sign.getLines()) {
				lineIdx += 1;
				int spacePos = line.indexOf(" ");
				String ruleType;
				String ruleData;
				if (spacePos == -1) {
					ruleType = line;
					ruleData = null;
				} else {
					ruleType = line.substring(0, spacePos);
					ruleData = line.substring(spacePos + 1);
				}
				Rule rule = null;
				switch (ruleType.toLowerCase()) {
					case "allow": {
						rule = new RuleAllow(ruleData);
					}
					break;
					case "block": {
						rule = new RuleBlock(ruleData);
					}
					break;
					case "fuelifempty": {
						rule = new RuleFuelIfEmpty();
					}
					break;
					case "matchfurnace": {
						rule = new RuleMatchFurnace();
					}
					break;
					case "compact": {
						rule = new RuleCompact();
					}
					break;
					case "slice": {
						rule = new RuleSlice(false);
					}
					break;
					case "dice": {
						rule = new RuleSlice(true);
					}
					break;
					case "skip": {
						try {
							String[] split = ruleData.split("/");
							if (split.length == 1) {
								rule = new RuleSkip(0, Integer.parseInt(split[0]), sign, lineIdx);
							} else {
								rule = new RuleSkip(Integer.parseInt(split[0]), Integer.parseInt(split[1]), sign, lineIdx);
							}
						} catch (Exception e) {
						}
					}
					break;
					case "count": {
						int count = 0;
						try {
							count = Integer.parseInt(ruleData);
						} catch (Exception e) {
						}
						rule = new RuleCount(count, sign, lineIdx);
					}
					break;
					case "duplicate":
						duplicator = true;
						break;
					case "autodrop":
						autoDropper = true;
						break;
				}
				if (rule != null)
					addRule.accept(rule);
			}
		}
		return new BlockConfig(rules, duplicator, autoDropper);
	}
}
