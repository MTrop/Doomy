package net.mtrop.doomy;

import java.io.File;

import net.mtrop.doomy.struct.OSUtils;

/**
 * Environment class for getting runtime paths.
 * @author Matthew Tropiano
 */
public final class DoomyEnvironment
{
    /** Doomy Config folder base. */
    private static final String USER_CONFIG_BASE = OSUtils.getApplicationSettingsPath() + "/Doomy/";
    /** Doomy directory for local paths. */
    private static final String PORTABLE_CONFIG_BASE = OSUtils.getWorkingDirectoryPath() + "/";
    /** Doomy downloads directory. */
    private static final String DOWNLOADS_DIR = "downloads";
    /** Doomy presets directory. */
    private static final String PRESETS_DIR = "presets";
    /** Doomy Data directory. */
    private static final String TEMP_DIR = "temp";
    /** Doomy DB name. */
    private static final String SETTINGS_DB = "doomy.db";
    /** Doomy portable file name. */
    private static final String PORTABLE_FILE = "portable.txt";
    
    // @return true if in portable mode.
    private static boolean inPortableMode()
    {
    	return (new File(PORTABLE_CONFIG_BASE + PORTABLE_FILE)).exists();
    }

    /** 
     * @return the configuration base directory. 
     */
    public static String getConfigBasePath()
    {
    	return inPortableMode() ? PORTABLE_CONFIG_BASE : USER_CONFIG_BASE;
    }

    /**
     * @return the temporary directory path.
     */
    public static String getTempDirectoryPath()
    {
    	return (new File(getConfigBasePath() + "/" + TEMP_DIR)).getAbsolutePath();
    }

    /**
     * Gets the path to a specific preset.
     * @param presetHash the preset hash.
     * @return the canonical directory path to the preset.
     */
    public static String getPresetDirectoryPath(String presetHash)
    {
    	DoomyCommon.checkNotEmpty(presetHash);
    	return (new File(getConfigBasePath() + "/" + PRESETS_DIR + "/" + presetHash)).getAbsolutePath();
    }

    /**
     * Gets the path to a specific preset.
     * @param presetHash the preset hash.
     * @return the canonical directory path to the preset.
     */
    public static String getDownloadDirectoryPath()
    {
    	return (new File(getConfigBasePath() + "/" + DOWNLOADS_DIR)).getAbsolutePath();
    }

    /**
     * Gets the path to a specific preset.
     * @param presetHash the preset hash.
     * @return the canonical directory path to the preset.
     */
    public static String getDatabasePath()
    {
    	return (new File(getConfigBasePath() + "/" + SETTINGS_DB)).getAbsolutePath();
    }

}
