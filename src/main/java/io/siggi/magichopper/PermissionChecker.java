package io.siggi.magichopper;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PermissionChecker {
    public boolean hasPermission(Player player, String permission);
}
