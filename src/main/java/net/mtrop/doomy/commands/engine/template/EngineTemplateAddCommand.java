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

/**
 * A command that adds a new engine template.
 * @author Matthew Tropiano
 */
public class EngineTemplateAddCommand implements DoomyCommand
{
	private String name;
	private String template;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new template.");
		template = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, template);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the name of the template.
	 * @param template the source template to copy. Can be null.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String template)
	{
		EngineTemplateManager mgr = EngineTemplateManager.get();
		
		if (mgr.containsTemplate(name))
		{
			handler.errln("ERROR: Engine template '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (template != null)
		{
			if (!mgr.containsTemplate(template))
			{
				handler.errln("ERROR: Source engine template '" + template + "' does not exist.");
				return ERROR_NOT_FOUND;
			}
			
			if (mgr.addTemplateFrom(name, template) == null)
			{
				handler.errln("ERROR: Engine template '" + name + "' could not be copied from '" + template + "'.");
				return ERROR_NOT_ADDED;
			}

			handler.outln("Created engine template '" + name + "' from '" + template + "'.");
			return ERROR_NONE;
		}
		
		if (mgr.addTemplate(name) == null)
		{
			handler.errln("ERROR: Engine template '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		handler.outln("Created engine template '" + name + "'.");
		return ERROR_NONE;
	}

}
