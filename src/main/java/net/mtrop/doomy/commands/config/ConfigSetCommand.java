/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.ConfigManager;

/**
 * A command that sets a config value.
 * @author Matthew Tropiano
 */
public class ConfigSetCommand implements DoomyCommand
{
	private String name;
	private String value;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
		value = args.pollFirst();
		if (value == null)
			throw new BadArgumentException("Expected value after setting name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, value);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the setting name. 
	 * @param value the setting value.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String value)
	{
		ConfigManager config = ConfigManager.get();
		
		String readValue;
		if (!config.setValue(name, value))
		{
			handler.errln("ERROR: Could not set: " + name);
			return ERROR_NOT_ADDED;
		}
		else
		{
			readValue = config.getValue(name);
			handler.outln("'" + name + "' is '" + readValue + "'");
		}
		
		return ERROR_NONE;
	}

}
