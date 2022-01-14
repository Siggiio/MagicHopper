package io.siggi.magichopper;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Util {
	public static final Set<Material> signPosts;
	public static final Set<Material> wallSigns;
	public static final Set<Material> allSigns;
	public static final List<BlockFace> directions;

	static {
		HashSet<Material> posts = new HashSet<>();
		HashSet<Material> walls = new HashSet<>();
		for (Material material : Material.values()) {
			String name = material.name().toLowerCase();
			if (name.startsWith("legacy_"))
				continue;
			if (name.endsWith("_wall_sign")) {
				walls.add(material);
			} else if (name.endsWith("_sign")) {
				posts.add(material);
			}
		}
		HashSet<Material> all = new HashSet<>();
		all.addAll(posts);
		all.addAll(walls);
		signPosts = Collections.unmodifiableSet(posts);
		wallSigns = Collections.unmodifiableSet(walls);
		allSigns = Collections.unmodifiableSet(all);
		directions = Collections.unmodifiableList(
				Arrays.asList(
						BlockFace.NORTH,
						BlockFace.SOUTH,
						BlockFace.WEST,
						BlockFace.EAST
				)
		);
	}

	public static boolean isSign(Material material) {
		return allSigns.contains(material);
	}

	public static boolean isWallSign(Material material) {
		return wallSigns.contains(material);
	}

	public static boolean isSignPost(Material material) {
		return signPosts.contains(material);
	}

	public static List<Sign> getSignsOnBlock(Block block) {
		List<Sign> signs = new ArrayList<>();
		if (block.getY() < block.getWorld().getMaxHeight()) {
			Block up = block.getRelative(BlockFace.UP);
			if (isSignPost(up.getType())) {
				try {
					Sign sign = (Sign) up.getState();
					signs.add(sign);
				} catch (Exception e) {
				}
			}
		}
		for (BlockFace face : directions) {
			Block relative = block.getRelative(face);
			if (!isWallSign(relative.getType()))
				continue;
			try {
				Sign sign = (Sign) relative.getState();
				org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) relative.getBlockData();
				if (signData.getFacing() != face)
					continue;
				signs.add(sign);
			} catch (Exception e) {
			}
		}
		return signs;
	}
}
