/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.commands.preset.PresetRunCommand;
import net.mtrop.doomy.commands.run.RunCommand;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.WADManager;


/**
 * A command that runs or creates presets and runs them.
 * @author Matthew Tropiano
 */
public class DefaultCommand implements DoomyCommand
{
	private static final String[] NO_WADS = new String[0];

	private static final String SWITCH_ARGS = "--";
	private static final String SWITCH_NAME1 = "--name";
	private static final String SWITCH_NAME2 = "-n";
	private static final String SWITCH_NOCLEANUP = "--no-cleanup";

	private String presetName;
	private String engineName;
	private String iwadName;
	private String[] pwadNames;
	private String[] additionalArgs;
	private boolean skipCleanup;
	
	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		presetName = args.pollFirst();
		if (presetName == null)
			throw new BadArgumentException("Expected preset or engine name.");

		PresetManager presetManager = PresetManager.get();
		
		// resolve preset or engine.
		if (!presetManager.containsPreset(presetName))
		{
			Preset[] presets = presetManager.getPresetByHash(presetName);
			if (presets.length == 0)
			{
				if (EngineManager.get().containsEngine(presetName))
				{
					engineName = presetName;
					presetName = null;
				}
				else
				{
					throw new BadArgumentException("Expected preset or engine name.");
				}
			}
			else if (presets.length > 1)
			{
				throw new BadArgumentException("Hash matches too many presets.");
			}
			else
			{
				presetName = presets[0].name;
			}
		}
		
		if (presetName != null)
			initWithPreset(args);
		else if (engineName != null)
			initWithEngine(args);
		else
			throw new BadArgumentException("Expected preset or engine name.");
	}
	
	private void initWithPreset(Deque<String> args)
	{
		List<String> argsList = new LinkedList<>(); 
		
		final int STATE_START = 0;
		final int STATE_ARGS = 4;
		int state = STATE_START;
		while (!args.isEmpty())
		{
			switch (state)
			{
				case STATE_START:
				{
					if (matchArgument(args, SWITCH_ARGS))
						state = STATE_ARGS;
					else if (matchArgument(args, SWITCH_NOCLEANUP))
						skipCleanup = true;
					else
						argsList.add(args.pollFirst());
				}
				break;

				case STATE_ARGS:
				{
					argsList.add(args.pollFirst());
				}
				break;
			}
		}
		
		pwadNames = NO_WADS;
		additionalArgs = argsList.toArray(new String[argsList.size()]);
	}
	
	private void initWithEngine(Deque<String> args)
	{
		List<String> wadList = new LinkedList<>(); 
		List<String> argsList = new LinkedList<>(); 

		final int STATE_START = 0;
		final int STATE_NAME = 1;
		final int STATE_ARGS = 4;
		int state = STATE_START;
		
		while (!args.isEmpty())
		{
			switch (state)
			{
				case STATE_START:
				{
					if (matchArgument(args, SWITCH_NAME1) || matchArgument(args, SWITCH_NAME2))
						state = STATE_NAME;
					else if (matchArgument(args, SWITCH_ARGS))
						state = STATE_ARGS;
					else if (matchArgument(args, SWITCH_NOCLEANUP))
						skipCleanup = true;
					else
					{
						String arg = args.pollFirst();
						if (IWADManager.get().containsIWAD(arg))
							iwadName = arg;
						else if (WADManager.get().containsWAD(arg))
							wadList.add(arg);
					}
				}
				break;
				
				case STATE_NAME:
				{
					presetName = args.pollFirst();
					state = STATE_START;
				}
				break;
				
				case STATE_ARGS:
				{
					argsList.add(args.pollFirst());
				}
				break;
			}
		}
		
		pwadNames = wadList.toArray(new String[wadList.size()]);
		additionalArgs = argsList.toArray(new String[argsList.size()]);
	}
	

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, presetName, engineName, iwadName, pwadNames, additionalArgs, skipCleanup);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param presetName the preset name (can be null).
	 * @param engineName the engine name (can be null).
	 * @param iwadName the IWAD name (can be null).
	 * @param wadNames the WAD names (can be null).
	 * @param additionalArgs additional arguments.
	 * @param skipCleanup if true, skip the cleanup after run.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String presetName, String engineName, String iwadName, String[] wadNames, String[] additionalArgs, boolean skipCleanup)
	{
		if (engineName != null)
		{
			return RunCommand.execute(handler, presetName, engineName, iwadName, wadNames, additionalArgs, skipCleanup);
		}
		else if (presetName != null)
		{
			return PresetRunCommand.execute(handler, presetName, additionalArgs, skipCleanup);
		}
		else
		{
			handler.errln("ERROR: Nothing to run!");
			return ERROR_BAD_COMMAND;
		}
	}
	
}
