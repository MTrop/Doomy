/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the version and exits.
 * @author Matthew Tropiano
 */
public class EnvironmentCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler)
	{
		handler.outln("Doomy Path:         " + DoomyEnvironment.getDoomyPath());
		handler.outln("Doomy JAR Path:     " + DoomyEnvironment.getDoomyJarPath());
		handler.outln("App Config Path:    " + DoomyEnvironment.getApplicationConfigPath());
		handler.outln("App Data Path:      " + DoomyEnvironment.getApplicationDataPath());
		handler.outln("App Cache Path:     " + DoomyEnvironment.getApplicationCachePath());
		handler.outln("App State Path:     " + DoomyEnvironment.getApplicationStatePath());
		handler.outln("System Config Path: " + DoomyEnvironment.getSystemConfigPath());
		handler.outln("System Data Path:   " + DoomyEnvironment.getSystemDataPath());
		handler.outln("System Temp Path:   " + DoomyEnvironment.getSystemTempPath());
		return ERROR_NONE;
	}
	
}
