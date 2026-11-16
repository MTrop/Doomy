/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.struct.ProcessCallable;
import net.mtrop.doomy.struct.InstancedFuture;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.LauncherManager.LaunchException;

/**
 * A command that calls the engine setup EXE.
 * @author Matthew Tropiano
 */
public class EngineSetupCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of engine.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the engine name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name)
	{
		EngineSettings settings = EngineConfigManager.get().getEngineSettings(name);
		
		if (settings == null)
		{
			handler.errln("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}

		ProcessCallable callable;
		InstancedFuture<Integer> process;

		try {
			callable = settings.plan.planTypeId.createSetupProcess(settings.plan);
		} catch (LaunchException e) {
			handler.errln("ERROR: " + e.getLocalizedMessage());
			return ERROR_LAUNCH_ERROR;
		}
		
		process = TaskManager.get().spawn(callable);
		process.join();
		
		return ERROR_NONE;
	}

}
