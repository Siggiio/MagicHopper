package io.siggi.magichopper;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class PlayerEventHandler implements Listener {
	private final MagicHopper plugin;
	private final PermissionChecker permissionChecker;

	public PlayerEventHandler(MagicHopper plugin, PermissionChecker permissionChecker) {
		this.plugin = plugin;
		this.permissionChecker = permissionChecker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void signChangeEvent(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		String firstLine = ChatColor.stripColor(lines[0]).toUpperCase().replace(" ", "");
		if (firstLine.matches("\\[MH[0-9]*\\]")) {
			if (!permissionChecker.hasPermission(player, "magichopper.use")) {
				event.setLine(0, ChatColor.DARK_RED + "[no permission]");
				return;
			}
			plugin.wipeBlockConfig(event.getBlock());
			String numberString = firstLine.substring(3, firstLine.length() - 1);
			int number = -1;
			try {
				number = Integer.parseInt(numberString);
			} catch (Exception e) {
			}
			if (number <= 0) {
				Block blockOfSign = Util.getBlockSignIsOn(event.getBlock());
				if (blockOfSign == null) {
					event.setLine(0, ChatColor.DARK_RED + "[fail]");
					event.setLine(1, "Hanging");
					event.setLine(2, "signs not");
					event.setLine(3, "supported");
					return;
				}
				List<Sign> signsOnBlock = Util.orderSigns(Util.getSignsOnBlock(blockOfSign));
				number = 1;
				boolean changed;
				do {
					changed = false;
					for (Sign sign : signsOnBlock) {
						if (Util.getSignNumber(sign) == number) {
							changed = true;
							number += 1;
						}
					}
				} while (changed);
			}
			event.setLine(0, ChatColor.BLUE + (number == 1 ? "[MH]" : ("[MH " + number + "]")));
			for (int i = 1; i < 4; i++) {
				String line = event.getLine(i);
				if (line.trim().isEmpty())
					continue;
				int spacePosition = line.indexOf(" ");
				if (spacePosition == -1) spacePosition = line.length();
				String rule = line.substring(0, spacePosition).toLowerCase();
				if ((rule.equals("duplicate") && !permissionChecker.hasPermission(player, "magichopper.type.duplicate"))
					|| (!permissionChecker.hasPermission(player, "magichopper.type.standard") && !permissionChecker.hasPermission(player, "magichopper.type." + rule))) {
					event.setLine(i, ChatColor.RED + "[!]");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlaced(BlockPlaceEvent event) {
		plugin.wipeBlockConfig(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBroken(BlockBreakEvent event) {
		plugin.wipeBlockConfig(event.getBlock());
	}
}
