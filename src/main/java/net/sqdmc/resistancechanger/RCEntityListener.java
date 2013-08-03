package net.sqdmc.resistancechanger;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Custom Entity Listener for the ResistanceChanger plugin.
 * 
 * @author Pandemoneus [Orig], Badzombie, Squidicuz
 * 
 */
public final class RCEntityListener implements Listener {

    private ResistanceChanger plugin;

    public RCEntityListener(ResistanceChanger plugin) {
        this.plugin = plugin;
    }

    /**
     * Destroys obsidian blocks in a radius around TNT, Creepers and/or Ghast
     * Fireballs, and/or Withers.
     * 
     * @param event
     *            event data
     */
    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        // do not do anything in case explosions get canceled
        if (event == null || event.isCancelled()) {
            return;
        }

        if (RCConfig.getInstance().getDisabledWorlds().contains(event.getLocation().getWorld().getName())) {
            return;
        }

        final int radius = RCConfig.getInstance().getRadius();

        // cancel if radius is < 0
        if (radius < 0) {
            ResistanceChanger.LOG.warning("Explosion radius is less than zero. Current value: " + radius);
            return;
        }

        final Entity detonator = event.getEntity();

        if (detonator == null) {
            // some other plugins create new explosions passing 'null' as
            // Entity, so we need this here to fix it
            return;
        }

        final Location detonatorLoc = detonator.getLocation();
        final String eventTypeRep = event.getEntity().toString();

        // List of blocks that will be removed from the blocklist
        List<Block> protectedBlocks = new ArrayList<Block>();

        // Hook into cannons.. (with xObsidianDestroyer)
        if (eventTypeRep.equals("CraftSnowball")) {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                if (BlockManager.getInstance().blowBlockUp(block, event) != null) {
                    protectedBlocks.add(block);
                }
            }
        }

        // Liquid override
        if (RCConfig.getInstance().getExplodeInLiquids()) {
            ExplosionsInLiquid.Handle(event, this.plugin);
        }

        // Check explosion blocks
        for (Block block : event.blockList()) {
            if ((detonatorLoc.getBlock().isLiquid()) && (RCConfig.getInstance().getWaterProtection())) {
                return;
            }
            if (BlockManager.getInstance().blowBlockUp(block, event) != null) {
                protectedBlocks.add(block);
            }
        }

        // For stuff that isn't destroyable.
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
                    if (detonatorLoc.distance(targetLoc) <= radius) {
                        if (protectedBlocks.contains(targetLoc.getBlock())) {
                            continue;
                        }
                        if (!BlockManager.getInstance().contains(targetLoc.getBlock().getTypeId()) || targetLoc.getBlock().getTypeId() == 0) {
                            continue;
                        }
                        if ((detonatorLoc.getBlock().isLiquid()) && (RCConfig.getInstance().getWaterProtection())) {
                            return;
                        }
                        if (BlockManager.getInstance().blowBlockUp(targetLoc.getBlock(), event) != null) {
                            protectedBlocks.add(targetLoc.getBlock());
                        }
                    }
                }
            }
        }

        // Remove managed blocks
        for (Block block : protectedBlocks) {
            event.blockList().remove(block);
        }
    }
}
