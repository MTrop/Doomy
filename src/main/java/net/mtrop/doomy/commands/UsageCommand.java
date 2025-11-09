/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the basic usage and exits.
 * @author Matthew Tropiano
 */
public class UsageCommand implements DoomyCommand
{
	private String badCommand;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		badCommand = !args.isEmpty() ? args.pop() : null;
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, badCommand);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param badCommand the name of the bad command, if any. Can be null.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String badCommand)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.splash(handler, DoomyMain.VERSION);
		DoomyCommon.usage(handler);
		return ERROR_NONE;
	}
	
}
