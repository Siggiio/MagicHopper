package io.siggi.magichopper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldEventHandler implements Listener {
	private final MagicHopper plugin;

	public WorldEventHandler(MagicHopper plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void worldUnloaded(WorldUnloadEvent event) {
		plugin.wipeWorldConfig(event.getWorld());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void chunkUnloaded(ChunkUnloadEvent event) {
		plugin.wipeChunkConfig(event.getChunk());
	}
}
