/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.iwad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;

/**
 * A command that gets an IWAD path by name.
 * @author Matthew Tropiano
 */
public class IWADGetCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of IWAD.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the IWAD name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name)
	{
		IWAD iwad = IWADManager.get().getIWAD(name);
		
		if (iwad == null)
		{
			handler.errln("ERROR: No such IWAD: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			handler.outln(iwad.path);
		}
		
		return ERROR_NONE;
	}

}
