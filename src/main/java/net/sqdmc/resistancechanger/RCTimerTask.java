package net.sqdmc.resistancechanger;

import java.util.HashMap;
import java.util.TimerTask;

public final class RCTimerTask extends TimerTask {
    private final Integer duraID;

    public RCTimerTask(ResistanceChanger plugin, Integer duraID) {
        this.duraID = duraID;
    }

    @Override
    public void run() {
        resetDurability(duraID);
    }

    private void resetDurability(Integer id) {
        if (id == null) {
            return;
        }

        HashMap<Integer, Integer> map = BlockManager.getInstance().getObsidianDurability();

        if (map == null) {
            return;
        }

        map.remove(id);
    }
}