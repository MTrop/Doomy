/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.engine.template;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;

/**
 * A command that handles template configuration.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigCommand implements DoomyCommand
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
	 * @param badCommand the unknown command.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String badCommand)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.help(handler, ENGINE);
		return ERROR_NONE;
	}

}
