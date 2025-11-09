/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.engine.template;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineTemplateManager;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing engine template.
 * @author Matthew Tropiano
 */
public class EngineTemplateRemoveCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";
	
	private String name;
	private boolean quiet;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of template to remove.");
		
		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_QUIET1) || matchArgument(args, SWITCH_QUIET2))
				quiet = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, quiet);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the name of the template.
	 * @param quiet if true, confirm removal without asking.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, boolean quiet)
	{
		EngineTemplateManager mgr = EngineTemplateManager.get();
		
		if (!mgr.containsTemplate(name))
		{
			handler.errln("ERROR: Engine template '" + name + "' does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(handler.prompt("Are you sure that you want to remove template '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.removeTemplate(name))
		{
			handler.errln("ERROR: Engine template '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed engine template '" + name + "'.");
		return ERROR_NONE;
	}

}
