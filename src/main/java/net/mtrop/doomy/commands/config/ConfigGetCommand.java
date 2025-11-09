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
 * A command that prints a single setting value by name.
 * @author Matthew Tropiano
 */
public class ConfigGetCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the setting name. 
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name)
	{
		String value = ConfigManager.get().getValue(name);
		
		if (value == null)
		{
			handler.errln("ERROR: No such setting: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			handler.outln(value);
		}
		
		return ERROR_NONE;
	}
	
}
