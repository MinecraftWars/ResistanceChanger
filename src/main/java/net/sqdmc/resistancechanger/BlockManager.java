package net.sqdmc.resistancechanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class BlockManager {
    private static BlockManager instance;
    private HashMap<Integer, Integer> durability = new HashMap<Integer, Integer>();
    private HashMap<Integer, BlockTimer> timer = new HashMap<Integer, BlockTimer>();
    private Map<Integer, DuraBlock> rBlocks = new HashMap<Integer, DuraBlock>();

    private boolean displayedWarning = false;

    public BlockManager() {
        instance = this;
        rBlocks = RCConfig.getInstance().getResistanceBlocks();
    }

    public void setDuraBlocks(Map<Integer, DuraBlock> rBocks) {
        this.rBlocks = rBocks;
    }

    /**
     * Handles a block on an EntityExplodeEvent
     * 
     * @param block in EntityExplodeEvent
     * @return block to remove from Explosion blocklist
     */
    public Block blowBlockUp(final Block block, EntityExplodeEvent event) {
        if (block == null) {
            return null;
        }
        if (block.getType() == Material.AIR) {
            return null;
        }
        Location at = block.getLocation();
        if (at == null) {
            return null; 
        }

        if (!contains(block.getTypeId())) {
            return null;
        }

        final String eventTypeRep = event.getEntity().toString();

        if (eventTypeRep.equals("CraftTNTPrimed") && !getTntEnabled(block.getTypeId())) {
            return null;
        }
        if (eventTypeRep.equals("CraftSnowball") && !getCannonsEnabled(block.getTypeId())) {
            return null;
        }
        if (eventTypeRep.equals("CraftCreeper") && !getCreepersEnabled(block.getTypeId())) {
            return null;
        }
        if ((eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast")) && !getGhastsEnabled(block.getTypeId())) {
            return null;
        }

        Block resistedBlock = null;
        //ResistanceChanger.LOG.info("Protecting Block..!");
        resistedBlock = block;
        Integer representation = at.getWorld().hashCode() + at.getBlockX() * 2389 + at.getBlockY() * 4027 + at.getBlockZ() * 2053;
        if (getDurabilityEnabled(block.getTypeId()) && getDurability(block.getTypeId()) > 1) {
            if (durability.containsKey(representation)) {
                int currentDurability = (int) durability.get(representation);
                currentDurability++;
                if (checkIfMax(currentDurability, block.getTypeId())) {
                    // counter has reached max durability, so remove the
                    // block and drop an item
                    dropBlockAndResetTime(representation, at, block.getTypeId());
                } else {
                    // counter has not reached max durability yet
                    durability.put(representation, currentDurability);
                    if (getDurabilityResetTimerEnabled(block.getTypeId())) {
                        startNewTimer(representation, block.getTypeId());
                    }
                }
            } else {
                durability.put(representation, 1);
                if (getDurabilityResetTimerEnabled(block.getTypeId())) {
                    startNewTimer(representation, block.getTypeId());
                }
                if (checkIfMax(1, block.getTypeId())) {
                    dropBlockAndResetTime(representation, at, block.getTypeId());
                }
            }
        } else {
            destroyBlockAndDropItem(at);
        }
        return resistedBlock;
    }

    private void destroyBlockAndDropItem(final Location at) {
        if (at == null) {
            return;
        }

        final Block b = at.getBlock();

        if (!contains(b.getTypeId())) {
            return;
        }

        //ResistanceChanger.LOG.info("Destroying Block!!");
        double chance = getChanceToDropBlock(b.getTypeId());

        if (chance > 1.0)
            chance = 1.0;
        if (chance < 0.0)
            chance = 0.0;

        final double random = Math.random();

        if (chance == 1.0 || chance <= random) {
            ItemStack is = new ItemStack(b.getType(), 1, b.getData());

            if (is.getType() == Material.AIR) {
                return;
            }

            // drop item
            at.getWorld().dropItemNaturally(at, is);
        }

        // changes original block to Air block
        b.setTypeId(Material.AIR.getId());
    }

    private boolean checkIfMax(int value, int id) {
        return value == getDurability(id);
    }

    private void dropBlockAndResetTime(Integer representation, Location at, int key) {
        durability.remove(representation);
        destroyBlockAndDropItem(at);

        if (getDurabilityResetTimerEnabled(key)) {
            if (timer.get(representation) != null) {
                timer.remove(representation);
            }
        }
    }

    public void checkDurability() {
        //ResistanceChanger.LOG.info("BlockTimers: " + timer.size());
        List<Integer> timersExpired = new ArrayList<Integer>();
        for (Entry<Integer, BlockTimer> blockTimer : timer.entrySet()) {
            if (System.currentTimeMillis() > blockTimer.getValue().getTimeToLive()) {
                timersExpired.add(blockTimer.getKey());
            }
        }
        for (Integer timerExpired : timersExpired) {
            timer.remove(timerExpired);
            durability.remove(timerExpired);
        }
    }

    private void startNewTimer(Integer representation, int key) {
        if (timer.get(representation) != null) {
            timer.remove(representation);
        }

        // This should always be on be on in this case.
        // TODO: EXPERIMENTAL: Some safety just in case the server is running low on memory.
        // This will prevent a new timer from being created. However, durability will not regenerate
        if (RCConfig.getInstance().getDurabilityTimerSafey() || Runtime.getRuntime().freeMemory() + (1024 * 1024 * 5) > Runtime.getRuntime().maxMemory()) {
            float cmaxmemuse = ((float) Runtime.getRuntime().freeMemory() + (1024 * 1024 * RCConfig.getInstance().getMinFreeMemoryLimit()));
            if (cmaxmemuse >= Runtime.getRuntime().maxMemory()) {
                if (displayedWarning) {
                    ResistanceChanger.LOG.log(Level.INFO, "Server Memory: {0}MB free out of {1}MB available.", new Object[]{(Runtime.getRuntime().freeMemory() / 1024) / 1024, (Runtime.getRuntime().maxMemory() / 1024) / 1024});
                    ResistanceChanger.LOG.log(Level.INFO, "Server is running low on resources.. Let''s not start a new timer, there are {0} other timers running!", timer.size());
                    displayedWarning = false;
                }
                return;
            } else {
                displayedWarning = true;
            }
        }

        timer.put(representation, new BlockTimer(getDurabilityResetTime(key)));
    }

    /**
     * Returns the HashMap containing all saved durabilities.
     * 
     * @return the HashMap containing all saved durabilities
     */
    public HashMap<Integer, Integer> getObsidianDurability() {
        return durability;
    }

    /**
     * Sets the HashMap containing all saved durabilities.
     * 
     * @param map containing all saved durabilities
     */
    public void setObsidianDurability(HashMap<Integer, Integer> map) {
        if (map == null) {
            return;
        }

        durability = map;
    }

    /**
     * Returns the HashMap containing all saved durability timers.
     * 
     * @return the HashMap containing all saved durability timers
     */
    public HashMap<Integer, BlockTimer> getObsidianTimer() {
        return timer;
    }

    /**
     * Sets the HashMap containing all saved durability timers.
     * 
     * @param map containing all saved durability timers
     */
    public void setObsidianTimer(HashMap<Integer, BlockTimer> map) {
        if (map == null) {
            return;
        }

        timer = map;
    }

    /**
     * Gets the instance
     * 
     * @return instance
     */
    public static BlockManager getInstance() {
        return instance;
    }

    /**
     * Checks if the managed blocks contains an item
     * 
     * @param item to compare against
     * @return true if item equals managed block
     */
    public boolean contains(int item) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(item) || entry.getValue().equals(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether durability for block is enabled.
     * 
     * @return whether durability for block is enabled
     */
    public boolean getDurabilityEnabled(int key) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(key)) {
                if (entry.getValue().getEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the max durability.
     * 
     * @return the max durability
     */
    public int getDurability(int key) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue().getDurability();
            }
        }
        return 0;
    }

    /**
     * Returns whether durability timer for block is enabled.
     * 
     * @return whether durability timer for block is enabled
     */
    public boolean getDurabilityResetTimerEnabled(int key) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(key)) {
                if (entry.getValue().getResetEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the time in milliseconds after which the durability gets reset.
     * 
     * @return the time in milliseconds after which the durability gets reset
     */
    public long getDurabilityResetTime(int key) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue().getResetTime();
            }
        }
        return 100000L;
    }

    /**
     * Returns the chance to drop an item from a blown up block.
     * 
     * @return the chance to drop an item from a blown up block
     */
    public double getChanceToDropBlock(int key) {
        for (Entry<Integer, DuraBlock> entry : rBlocks.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue().getChanceTopDrop();
            }
        }
        return 0.6D;
    }

    /**
     * Returns if Fireball damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getGhastsEnabled(int key) {
        if (rBlocks.containsKey(key)) {
            return rBlocks.get(key).getGhastsEnabled();
        }
        return false;
    }

    /**
     * Returns if Creeper damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getCreepersEnabled(int key) {
        if (rBlocks.containsKey(key)) {
            return rBlocks.get(key).getCreepersEnabled();
        }
        return false;
    }

    /**
     * Returns if Cannon damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getCannonsEnabled(int key) {
        if (rBlocks.containsKey(key)) {
            return rBlocks.get(key).getCannonsEnabled();
        }
        return false;
    }

    /**
     * Returns if TNT damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getTntEnabled(int key) {
        if (rBlocks.containsKey(key)) {
            return rBlocks.get(key).getTntEnabled();
        }
        return false;
    }
}
