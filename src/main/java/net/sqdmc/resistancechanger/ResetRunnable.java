package net.sqdmc.resistancechanger;

import org.bukkit.scheduler.BukkitRunnable;

public class ResetRunnable extends BukkitRunnable {

    public void start() {
        this.runTaskTimer(ResistanceChanger.getInstance(), 1200, 1200);
    }

    @Override
    public void run() {
        BlockManager.getInstance().checkDurability();
    }
}
