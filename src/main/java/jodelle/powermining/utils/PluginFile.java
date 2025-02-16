package jodelle.powermining.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import jodelle.powermining.PowerMining;

import java.io.File;
import java.io.IOException;

/**
 * Represents an abstract file handler for managing configuration files in the PowerMining plugin.
 * 
 * <p>
 * This class provides functionality for loading, saving, and reloading YAML configuration files.
 * It ensures that the file is created if it does not exist and allows subclasses to interact with the configuration.
 * </p>
 */
public abstract class PluginFile {

    private File file;
    private FileConfiguration fileConfig;

    /**
     * Constructs a {@code PluginFile} instance and initializes the YAML configuration file.
     * 
     * <p>
     * If the file does not already exist, it is created by copying a default version from the plugin's resources.
     * </p>
     * 
     * @param plugin The instance of {@link PowerMining} managing the configuration files.
     * @param name   The name of the file, including its relative path inside the plugin's data folder.
     */
    public PluginFile(PowerMining plugin, String name) {
        file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        reload();
    }

    /**
     * Saves the current configuration to the file.
     * 
     * <p>
     * If an error occurs while saving, an {@link IOException} is thrown as a runtime exception.
     * </p>
     */
    public void save() {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reloads the configuration from the file.
     * 
     * <p>
     * This method loads the latest changes from disk, ensuring that any modifications
     * made outside the plugin are applied.
     * </p>
     */
    public void reload() {
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Retrieves the {@link FileConfiguration} object associated with this configuration file.
     * 
     * <p>
     * This allows other classes to access and modify configuration settings.
     * </p>
     * 
     * @return The {@link FileConfiguration} instance for this file.
     */
    public FileConfiguration getFileConfig() {
        return fileConfig;
    }

    /**
     * Retrieves the {@link File} object representing the physical file on disk.
     * 
     * @return The {@link File} associated with this configuration.
     */
    public File getFile() {
        return file;
    }
}
