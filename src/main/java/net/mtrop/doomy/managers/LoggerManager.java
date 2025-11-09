/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.io.File;
import java.io.IOException;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.struct.LoggingFactory;
import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.util.FileUtils;

/**
 * DoomTools GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class LoggerManager 
{
	/** Logging filename. */
	private static final String LOG_FILENAME = "doomy.log";
	/** Configuration file. */
	private static final File LOG_FILE = new File(DoomyEnvironment.getConfigBasePath() + LOG_FILENAME);

	/** The instance encapsulator. */
	private static final SingletonProvider<LoggerManager> INSTANCE = new SingletonProvider<>(() -> new LoggerManager()); 

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static LoggerManager get()
	{
		return INSTANCE.get();
	}

	/**
	 * Fetches a logger for a class.
	 * @param clazz the class.
	 * @return a new logger.
	 */
	public static Logger getLogger(Class<?> clazz)
	{
		return get().loggingFactory.getLogger(clazz);
	}
	
	/* ==================================================================== */
	
	private LoggingFactory loggingFactory;
	
	private LoggerManager()
	{
		this.loggingFactory = LoggingFactory.createConsoleLoggingFactory();
		try {
			if (LOG_FILE.exists())
				LOG_FILE.delete();
			if (!FileUtils.createPathForFile(LOG_FILE))
				return;
			this.loggingFactory.addDriver(new LoggingFactory.FileLogger(LOG_FILE)); 
		} catch (IOException e) {
			// Do nothing.
		}
	}
	
}
