package net.sqdmc.resistancechanger;

import java.io.IOException;
import java.util.logging.Logger;

import net.sqdmc.resistancechanger.Metrics;
import net.sqdmc.resistancechanger.RCCommands;
import net.sqdmc.resistancechanger.RCConfig;
import net.sqdmc.resistancechanger.RCEntityListener;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * The ResistanceChanger plugin.
 * 
 * Allows certain explosions to destroy Obsidian.
 * 
 * @author Pandemoneus
 * 
 */
public final class ResistanceChanger extends JavaPlugin {
    /**
     * Plugin related stuff
     */
    private static ResistanceChanger instance;
    private final RCCommands cmdExecutor = new RCCommands(this);
    private final RCEntityListener entityListener = new RCEntityListener(this);
    private final RCPlayerListener playerListener = new RCPlayerListener();
    public static ResistanceChanger plugin;
    public static Logger LOG;
    private static PluginManager PM;

    private static String version;
    private static final String PLUGIN_NAME = "ResistanceChanger";

    private static boolean IS_FACTIONS_HOOKED = false;
    private static boolean IS_TOWNY_HOOKED = false;
    private static boolean IS_WORLDGUARD_HOOKED = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        RCConfig.getInstance().saveDurabilityToFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        instance = this;
        PM = getServer().getPluginManager();
        LOG = getLogger();
        PluginDescriptionFile pdfFile = getDescription();
        version = pdfFile.getVersion();

        getCommand("resistancechanger").setExecutor(cmdExecutor);
        getCommand("rc").setExecutor(cmdExecutor);

        new RCConfig(this);

        new BlockManager();

        // Load durabilities of hit blocks
        BlockManager.getInstance().setObsidianDurability(RCConfig.getInstance().loadDurabilityFromFile());

        checkFactionsHook();
        checkTownyHook();
        checkWorldGuardGHook();

        // start Metrics
        startMetrics();

        getServer().getPluginManager().registerEvents(entityListener, this);
        getServer().getPluginManager().registerEvents(playerListener, this);
    }
    
    public void startMetrics() {     
        PluginDescriptionFile pdfFile = this.getDescription();
        try {    
            Metrics metrics = new Metrics(this);    
            metrics.start();
            ResistanceChanger.LOG.info("[" + pdfFile.getName() + "] Metrics connection started.");
        } catch (IOException e) {
            ResistanceChanger.LOG.warning("[" + pdfFile.getName() + "] Failed to submit the stats :-("); // Failed to submit the stats :-(
        }
    }

    /**
     * Returns the version of the plugin.
     * 
     * @return the version of the plugin
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Returns the name of the plugin.
     * 
     * @return the name of the plugin
     */
    public static String getPluginName() {
        return PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPluginName();
    }

    /**
     * Returns the config of this plugin.
     * 
     * @return the config of this plugin
     */
    public RCConfig getRCConfig() {
        return RCConfig.getInstance();
    }

    /**
     * Returns the entity listener of this plugin.
     * 
     * @return the entity listener of this plugin
     */
    public RCEntityListener getListener() {
        return entityListener;
    }

    /**
     * Method that handles what gets reloaded
     * 
     * @return true if everything loaded properly, otherwise false
     */
    public boolean reload() {
        try {
            new RCConfig(instance);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* ====================================================
     * Hooks to other plugins
     * ==================================================== */

    /**
     * Checks to see if the Factions plugin is active.
     */
    private void checkFactionsHook() {
        Plugin plug = PM.getPlugin("Factions");

        if (plug != null) {
            String[] ver = plug.getDescription().getVersion().split("\\.");
            String version = ver[0] + "." + ver[1];
            if (version.equalsIgnoreCase("1.8")) {
                LOG.info("Factions 1.8.x Found! Enabling hook..");
                IS_FACTIONS_HOOKED = true;
            } else if (version.equalsIgnoreCase("1.6")) {
                LOG.info("Factions found, but v1.6.x is not supported!");
            }
        }
    }

    /**
     * Gets the state of the Factions hook.
     * 
     * @return Factions hook state
     */
    public static boolean isHookedFactions() {
        return IS_FACTIONS_HOOKED;
    }

    /**
     * Checks to see if the Towny plugin is active.
     */
    private void checkTownyHook() {
        Plugin plug = PM.getPlugin("Towny");

        if (plug != null) {
            LOG.info("Towny Found! Enabling hook..");
            IS_TOWNY_HOOKED = true;
        }
    }

    /**
     * Gets the state of the Towny hook.
     * 
     * @return Towny hook state
     */
    public static boolean isHookedTowny() {
        return IS_TOWNY_HOOKED;
    }

    /**
     * Checks to see if the WorldGuard plugin is active.
     */
    private void checkWorldGuardGHook() {
        Plugin plug = PM.getPlugin("WorldGuard");

        if (plug != null) {
            LOG.info("WorldGuard Found! Enabling hook..");
            IS_WORLDGUARD_HOOKED = true;
        }
    }

    /**
     * Gets the state of the WorldGuard hook.
     * 
     * @return WorldGuard hook state
     */
    public static boolean isHookedWorldGuard() {
        return IS_WORLDGUARD_HOOKED;
    }

    /**
     * Gets the WorldGuard plugin
     * 
     * @return WorldGuardPlugin
     * @throws Exception 
     */
    public WorldGuardPlugin getWorldGuard() throws Exception {
        Plugin plugin = PM.getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            throw new Exception("WorldGuard could not be reached!");
        }

        return (WorldGuardPlugin) plugin;
    }

    public static ResistanceChanger getInstance() {
        return instance;
    }
}
