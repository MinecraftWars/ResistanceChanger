package net.sqdmc.resistancechanger;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sqdmc.resistancechanger.ResistanceChanger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * The configuration file for the ResistanceChanger plugin, uses YML.
 * 
 * @author Pandemoneus
 * 
 */
public final class RCConfig {

    private ResistanceChanger plugin;
    private static String pluginVersion;

    /**
     * File handling
     */
    private File durabilityFile;

    /**
     * Default settings
     */
    private int explosionRadius = 5;
    private Map<Integer, DuraBlock> durablocks;
    private boolean waterProtection = true;
    private boolean checkUpdate = true;
    private int checkitemid = 38;
    private static String[] VALUES = new String[10];
    private boolean durabilityTimerSafey = false;
    private int minFreeMemoryLimit = 80;
    private boolean explodeInLiquid = false;
    private boolean protectTNTCannons = true;
    private ArrayList<String> disabledWorlds = new ArrayList<String>();

    private static RCConfig instance;
    private YamlConfiguration config;

    public RCConfig(ResistanceChanger plugin) {
        this.plugin = plugin;
        loadFile();
        if (config != null) {
            durablocks = new HashMap<Integer, DuraBlock>();
            loadData();
        }
        instance = this;
    }

    public static RCConfig getInstance() {
        return instance;
    }

    private void loadFile() {
        File folder = plugin.getDataFolder();
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        durabilityFile = new File(plugin.getDataFolder(), "durability.dat");
        if (!configFile.exists()) {
            ResistanceChanger.LOG.info("Creating config File...");
            createFile(configFile);
        }
        else {
            ResistanceChanger.LOG.info("Loading config File...");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    protected void createFile(File configFile) {
        configFile.getParentFile().mkdirs();

        InputStream inputStream = plugin.getResource("config.yml");

        if (inputStream == null) {
            ResistanceChanger.LOG.severe("Missing resource file: 'config'");
            return;
        }

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(configFile);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        ConfigurationSection section = null;
        try {
            pluginVersion = config.getString("Version");
            checkUpdate = config.getBoolean("checkupdate", true);
            explosionRadius = config.getInt("Radius");
            waterProtection = config.getBoolean("FluidsProtect");
            checkitemid = config.getInt("CheckItemId", 38);

            durabilityTimerSafey = config.getBoolean("Threading.UseTimerSafety", true);
            minFreeMemoryLimit = config.getInt("Threading.SystemMinMemory", 80);

            explodeInLiquid = config.getBoolean("Explosions.BypassAllFluidProtection", false);
            protectTNTCannons = config.getBoolean("Explosions.TNTCannonsProtected", true);

            disabledWorlds = (ArrayList<String>) config.getStringList("DisabledOnWorlds");

            section = config.getConfigurationSection("HandledBlocks");
            Map<Integer, DuraBlock> rBocks = new HashMap<Integer, DuraBlock>();
            for (String dura : section.getKeys(false)) {
                ConfigurationSection s = section.getConfigurationSection(dura);
                ResistanceChanger.LOG.info("Apply Durability to " + dura + " " + s.getInt("BlockID"));
                // derp constructor!
                DuraBlock rBlock = new DuraBlock(s.getInt("BlockID"),
                        s.getInt("Durability.Amount"), 
                        s.getBoolean("Durability.Enabled"),
                        s.getDouble("ChanceToDrop"),
                        s.getBoolean("Durability.ResetEnabled", false),
                        s.getLong("Durability.ResetAfter", 10000L),
                        s.getBoolean("EnabledFor.TNT", true),
                        s.getBoolean("EnabledFor.Cannons", false),
                        s.getBoolean("EnabledFor.Creepers", false),
                        s.getBoolean("EnabledFor.Ghasts", false),
                        s.getBoolean("EnabledFor.Withers", false));

                rBocks.put(s.getInt("BlockID"), rBlock);
            }

            // Clear the blocks list then add all from config file
            durablocks.clear();
            durablocks.putAll(rBocks);

            ChatColor y = ChatColor.YELLOW;
            ChatColor g = ChatColor.GRAY;

            VALUES[0] = y + "checkupdate: " + g + this.checkUpdate;
            VALUES[1] = y + "ExplosionRadius: " + g + this.getRadius();
            VALUES[2] = y + "FluidsProtectBlocks: " + g + this.getWaterProtection();
            VALUES[3] = y + "CheckItemId: " + g + this.getCheckItemId();
            VALUES[4] = y + "UseTimerSafety: " + g + this.getDurabilityTimerSafey();
            VALUES[5] = y + "SystemMinMemory: " + g + this.getMinFreeMemoryLimit();
            VALUES[6] = y + "BypassAllFluidProtection: " + g + this.getExplodeInLiquids();
            VALUES[7] = y + "TNTCannonsProtected: " + g + this.getProtectTNTCannons();
            VALUES[8] = y + "DisabledOnWorlds: " + g;
            if (this.getDisabledWorlds() != null) {
                for (String dWorld : this.getDisabledWorlds()) {
                    VALUES[9] += dWorld + " ";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRadius() {
        return explosionRadius;
    }

    public boolean getWaterProtection() {
        return waterProtection;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public int getCheckItemId() {
        return this.checkitemid;
    }

    public String[] getConfigList() {
        return VALUES;
    }

    public int getMinFreeMemoryLimit() {
        return this.minFreeMemoryLimit;
    }

    public boolean getDurabilityTimerSafey() {
        return this.durabilityTimerSafey;
    }

    public boolean getExplodeInLiquids() {
        return this.explodeInLiquid;
    }

    public boolean getProtectTNTCannons() {
        return this.protectTNTCannons;
    }

    public List<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }

    public boolean getCheckUpdate() {
        return this.checkUpdate;
    }

    /**
     * Saves the durability hash map to a file.
     */
    public void saveDurabilityToFile() {
        if (plugin.getListener() == null || BlockManager.getInstance().getObsidianDurability() == null) {
            return;
        }

        HashMap<Integer, Integer> map = BlockManager.getInstance().getObsidianDurability();

        if (durabilityFile == null) {
            ResistanceChanger.LOG.severe("OHHH CRAP!!!");
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(durabilityFile));
            oos.writeObject(map);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            ResistanceChanger.LOG.severe("Failed writing block durability for " + ResistanceChanger.getPluginName());
            e.printStackTrace();
        }
    }

    /**
     * Loads the durability hash map from a file.
     * 
     * @return the durability hash map from a file
     */
    @SuppressWarnings("unchecked")
    public HashMap<Integer, Integer> loadDurabilityFromFile() {
        if (!durabilityFile.exists() || plugin.getListener() == null || BlockManager.getInstance().getObsidianDurability() == null) {
            return null;
        }

        if (durabilityFile == null) {
            ResistanceChanger.LOG.severe("OHHH CRAP!!!");
        }

        HashMap<Integer, Integer> map = null;
        Object result = null;

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(durabilityFile));
            result = ois.readObject();
            map = (HashMap<Integer, Integer>) result;
            ois.close();
        } catch (IOException ioe) {
            ResistanceChanger.LOG.severe("Failed reading block durability for " + ResistanceChanger.getPluginName());
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            ResistanceChanger.LOG.severe("block durability file contains an unknown class, was it modified?");
            cnfe.printStackTrace();
        }

        return map;
    }

    public Map<Integer, DuraBlock> getResistanceBlocks() {
        return durablocks;
    }
}
