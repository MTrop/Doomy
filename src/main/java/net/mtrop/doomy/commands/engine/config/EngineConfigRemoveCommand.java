/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.engine.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that removes a single engine setting value.
 * @author Matthew Tropiano
 */
public class EngineConfigRemoveCommand implements DoomyCommand
{
	private String engine;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of engine.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, engine, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param engine the engine name.
	 * @param name the setting name to remove.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String engine, String name)
	{
		if (!EngineManager.get().containsEngine(engine))
		{
			handler.errln("ERROR: No such engine: " + engine);
			return ERROR_NOT_FOUND;
		}

		EngineConfigManager config = EngineConfigManager.get();
		
		if (!config.containsSetting(engine, name))
		{
			handler.errln("ERROR: Could not remove engine setting: '" + name + "'. It does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!config.removeSetting(engine, name))
		{
			handler.errln("ERROR: Could not remove engine setting: '" + name + "'. An error may have occurred.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed engine setting: " + name);
		return ERROR_NONE;
	}

}
