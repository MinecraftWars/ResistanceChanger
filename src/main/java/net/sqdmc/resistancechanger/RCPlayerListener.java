package net.sqdmc.resistancechanger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class RCPlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.getGameMode() != GameMode.CREATIVE) {
            if (player.getItemInHand().getAmount() > 0) {
                int itemInHand = player.getItemInHand().getTypeId();
                BlockManager bm = BlockManager.getInstance();
                Block block = event.getClickedBlock();
                if (itemInHand == RCConfig.getInstance().getCheckItemId()) {
                    Location loc = block.getLocation();
                    if (bm.getDurabilityEnabled(block.getTypeId())) {
                        int amount = 0;
                        Integer representation = Integer.valueOf(loc.getWorld().hashCode() + loc.getBlockX() * 2389 + loc.getBlockY() * 4027 + loc.getBlockZ() * 2053);
                        if (bm.getObsidianDurability().containsKey(representation)) {
                            amount = ((Integer) bm.getObsidianDurability().get(representation).intValue());
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                    + ChatColor.WHITE + (bm.getDurability(block.getTypeId()) - amount) + "/" + bm.getDurability(block.getTypeId()));
                        } else {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                    + ChatColor.WHITE + bm.getDurability(block.getTypeId()) + "/" + bm.getDurability(block.getTypeId()));
                        }
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && player.getGameMode() == GameMode.CREATIVE) {
            if (player.getItemInHand().getAmount() > 0) {
                if (player.getItemInHand().getTypeId() == RCConfig.getInstance().getCheckItemId()) {
                    if (BlockManager.getInstance().getDurabilityEnabled(event.getClickedBlock().getTypeId())) {
                        BlockManager bm = BlockManager.getInstance();
                        Block block = event.getClickedBlock();
                        int amount = 0;
                        Location loc = block.getLocation();
                        Integer representation = Integer.valueOf(loc.getWorld().hashCode() + loc.getBlockX() * 2389 + loc.getBlockY() * 4027 + loc.getBlockZ() * 2053);
                        if (bm.getObsidianDurability().containsKey(representation)) {
                            amount = ((Integer) bm.getObsidianDurability().get(representation).intValue());
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                    + ChatColor.WHITE + (bm.getDurability(block.getTypeId()) - amount) + "/" + bm.getDurability(block.getTypeId()));
                        } else {
                            player.sendMessage(ChatColor.DARK_PURPLE + "Durability of this block is: "
                                    + ChatColor.WHITE + bm.getDurability(block.getTypeId()) + "/" + bm.getDurability(block.getTypeId()));
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
        
    }
}
