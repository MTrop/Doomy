/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the version and exits.
 * @author Matthew Tropiano
 */
public class VersionCommand implements DoomyCommand
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
		handler.outln(DoomyMain.VERSION);
		return ERROR_NONE;
	}
	
}
