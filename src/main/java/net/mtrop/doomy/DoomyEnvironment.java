/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy;

import java.io.File;

import net.mtrop.doomy.struct.util.OSUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

/**
 * Environment class for getting runtime paths.
 * @author Matthew Tropiano
 */
public final class DoomyEnvironment
{
    /** Doomy downloads directory. */
    private static final String DOWNLOADS_DIR = "downloads";
    /** Doomy presets directory. */
    private static final String PRESETS_DIR = "presets";
    /** Doomy DB name. */
    private static final String SETTINGS_DB = "doomy.db";
    /** Doomy properties name. */
    private static final String PROPERTIES_FILE = "doomy.properties";
    
	private static final EnvironmentState ENV_STATE;

	static
	{
		if (isPortableEnvironmentPresent())
			ENV_STATE = new PortableEnvironmentState();
		else if (OSUtils.isWindows())
			ENV_STATE = new WindowsEnvironmentState();
		else if (OSUtils.isOSX())
			ENV_STATE = new MacOSEnvironmentState();
		else if (OSUtils.isLinux())
		{
			if (isXDGEnvironmentPresent())
				ENV_STATE = new XDGEnvironmentState();
			else
				ENV_STATE = new LinuxEnvironmentState();
		}
		else
			ENV_STATE = new DefaultEnvironmentState(); 
	}
	
	private static boolean isXDGEnvironmentPresent()
	{
		return System.getenv("XDG_CONFIG_HOME") != null
			|| System.getenv("XDG_DATA_HOME") != null
			|| System.getenv("XDG_STATE_HOME") != null
			|| System.getenv("XDG_CACHE_HOME") != null
			|| System.getenv("XDG_DATA_DIRS") != null
			|| System.getenv("XDG_CONFIG_DIRS") != null
		;
	}
	
	private static boolean isPortableEnvironmentPresent()
	{
		return (new File(getDoomyPath() + File.separator + "portable.txt")).exists();
	}
	
    /** 
	 * If null, this variable may not have been set - you are running from an IDE.
     * @return the configuration base directory. 
     */
    public static String getDoomyPath()
    {
		String path = System.getenv("DOOMY_PATH");
		if (path == null)
			return null;
		return path.endsWith(File.separator) ? path.substring(0, path.length() - File.separator.length()) : path;
    }

	/**
	 * Gets the path of the JAR that Doomy is running from.
	 * If null, this variable may not have been set - you are running from an IDE.
	 * @return the corresponding value.
	 * @throws SecurityException if the variable could not be retrieved.
	 */
	public static String getDoomyJarPath()
	{
		return System.getenv("DOOMY_JAR");
	}
	
	/** 
	 * @return the path to where configuration gets stored. 
	 */
	public static String getApplicationConfigPath()
	{
		return ENV_STATE.getApplicationConfigPath();
	}
	
	/** 
	 * @return the path to where application data gets stored. 
	 */
	public static String getApplicationDataPath()
	{
		return ENV_STATE.getApplicationDataPath();
	}
	
	/** 
	 * @return the path to where cache data gets stored. 
	 */
	public static String getApplicationCachePath()
	{
		return ENV_STATE.getApplicationCachePath();
	}
	
	/** 
	 * @return the path to where cache data gets stored. 
	 */
	public static String getApplicationStatePath()
	{
		return ENV_STATE.getApplicationStatePath();
	}
	
	/**
	 * @return the path to where system configuration gets stored. 
	 */
	public static String getSystemConfigPath()
	{
		return ENV_STATE.getSystemConfigPath();
	}
	
	/**
	 * @return the path to where system configuration gets stored. 
	 */
	public static String getSystemDataPath()
	{
		return ENV_STATE.getSystemDataPath();
	}
	
	/** 
	 * @return the path to where temporary data gets stored. 
	 */
	public static String getSystemTempPath()
	{
		return ENV_STATE.getSystemTempPath();
	}
	
    /**
     * Gets the path to all presets.
     * @return the canonical directory path to the preset.
     */
    public static String getPresetDirectoryPath()
    {
    	return getApplicationDataPath() + File.separator + PRESETS_DIR;
    }

    /**
     * Gets the path to a specific preset.
     * @param presetHash the preset hash.
     * @return the canonical directory path to the preset.
     */
    public static String getPresetDirectoryPath(String presetHash)
    {
    	DoomyCommon.checkNotEmpty(presetHash);
    	return (new File(getPresetDirectoryPath() + "/" + presetHash)).getAbsolutePath();
    }

    /**
     * Gets the path to the download directory.
     * @return the canonical directory path to the preset.
     */
    public static String getDownloadDirectoryPath()
    {
    	return (new File(getApplicationDataPath() + File.separator + DOWNLOADS_DIR)).getAbsolutePath();
    }

    /**
     * Gets the path to the database.
     * @return the canonical directory path to the preset.
     */
    public static String getDatabasePath()
    {
    	return (new File(getApplicationDataPath() + File.separator + SETTINGS_DB)).getAbsolutePath();
    }

    /**
     * Gets the path to a specific preset.
     * @return the canonical directory path to the preset.
     */
    public static String getPropertiesPath()
    {
    	return (new File(getApplicationConfigPath() + File.separator + PROPERTIES_FILE)).getAbsolutePath();
    }

	private interface EnvironmentState
	{
		/** 
		 * @return the path to where configuration gets stored. 
		 */
		String getApplicationConfigPath();

		/** 
		 * @return the path to where application data gets stored. 
		 */
		String getApplicationDataPath();

		/** 
		 * @return the path to where application cache gets stored. 
		 */
		String getApplicationCachePath();

		/** 
		 * @return the path to where application state info gets stored. 
		 */
		String getApplicationStatePath();

		/** 
		 * @return the path to where system configuration gets stored. 
		 */
		String getSystemConfigPath();
		
		/** 
		 * @return the path to where system data gets stored. 
		 */
		String getSystemDataPath();

		/** 
		 * @return the path to where temporary data gets stored. 
		 */
		String getSystemTempPath();
	}
	
	private static class DefaultEnvironmentState implements EnvironmentState
	{
		private static final String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + File.separator + "Doomy";

		@Override
		public String getApplicationConfigPath()
		{
			return APPDATA_PATH;
		}

		@Override
		public String getApplicationDataPath()
		{
			return APPDATA_PATH + File.separator + "data";
		}

		@Override
		public String getApplicationCachePath()
		{
			return APPDATA_PATH + File.separator + "cache";
		}

		@Override
		public String getApplicationStatePath()
		{
			return getApplicationDataPath();
		}

		@Override
		public String getSystemConfigPath()
		{
			return DoomyEnvironment.getDoomyPath() + File.separator + "config";
		}

		@Override
		public String getSystemDataPath()
		{
			return DoomyEnvironment.getDoomyPath() + File.separator + "data";
		}

		@Override
		public String getSystemTempPath()
		{
			return OSUtils.getTempDirectoryPath();
		}

	}

	private static class PortableEnvironmentState implements EnvironmentState
	{
		private static final String LOCAL_CONFIG_DIR = DoomyEnvironment.getDoomyPath(); 

		@Override
		public String getApplicationConfigPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "config";
		}

		@Override
		public String getApplicationDataPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "data";
		}
	
		@Override
		public String getApplicationCachePath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "cache";
		}

		@Override
		public String getApplicationStatePath()
		{
			return getApplicationDataPath();
		}

		@Override
		public String getSystemConfigPath()
		{
			return getApplicationConfigPath();
		}

		@Override
		public String getSystemDataPath()
		{
			return getApplicationDataPath();
		}

		@Override
		public String getSystemTempPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "temp";
		}

	}
	
	private static class WindowsEnvironmentState extends DefaultEnvironmentState
	{
		// No changes.
	}
	
	private static class MacOSEnvironmentState extends DefaultEnvironmentState
	{
		// No changes.
	}
	
	private static class LinuxEnvironmentState extends DefaultEnvironmentState
	{
		@Override
		public String getSystemConfigPath()
		{
			return "/usr/share/Doomy/config";
		}

		@Override
		public String getSystemDataPath()
		{
			return "/usr/share/Doomy/data";
		}
	}
	
	private static class XDGEnvironmentState implements EnvironmentState
	{
		@Override
		public String getApplicationConfigPath()
		{
			String path = System.getenv("XDG_CONFIG_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.config" + File.separator + "Doomy";
			else
				return path + File.separator + "Doomy";
		}

		@Override
		public String getApplicationDataPath()
		{
			String path = System.getenv("XDG_DATA_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.local/share" + File.separator + "Doomy";
			else
				return path + File.separator + "Doomy";
		}
	
		@Override
		public String getApplicationCachePath()
		{
			String path = System.getenv("XDG_CACHE_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.cache" + File.separator + "Doomy";
			else
				return path + File.separator + "Doomy";
		}
	
		@Override
		public String getApplicationStatePath()
		{
			String path = System.getenv("XDG_STATE_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.local/state" + File.separator + "Doomy";
			else
				return path + File.separator + "Doomy";
		}

		@Override
		public String getSystemConfigPath()
		{
			String path = System.getenv("XDG_CONFIG_DIRS");
			if (ObjectUtils.isEmpty(path))
				return "/etc/xdg/Doomy";
			else
			{
				String[] paths = path.split(File.pathSeparator);
				return paths[paths.length - 1] + "Doomy";
			}
		}

		@Override
		public String getSystemDataPath()
		{
			String path = System.getenv("XDG_DATA_DIRS");
			if (ObjectUtils.isEmpty(path))
				return "/usr/share/Doomy";
			else
			{
				String[] paths = path.split(File.pathSeparator);
				return paths[paths.length - 1] + "Doomy";
			}
		}

		@Override
		public String getSystemTempPath()
		{
			return OSUtils.getTempDirectoryPath();
		}
	}
	
}
