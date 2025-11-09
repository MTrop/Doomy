/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;

/**
 * A command that removes a preset.
 * @author Matthew Tropiano
 */
public class PresetRemoveCommand implements DoomyCommand
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
			throw new BadArgumentException("Expected name/hash of preset to remove.");
		
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
	 * @param name the preset name.
	 * @param quiet if true, do not ask to confirm.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, boolean quiet)
	{
		PresetManager mgr = PresetManager.get();
		
		Preset preset;
		int hashCount;
		
		if (mgr.containsPreset(name))
		{
			preset = mgr.getPresetByName(name);
		}
		else if ((hashCount = mgr.countPreset(name)) > 0)
		{
			if (hashCount == 1)
			{
				preset = mgr.getPresetByHash(name)[0];
			}
			else
			{
				handler.errln("ERROR: Hash '" + name + "' matches more than one preset.");
				return ERROR_NOT_FOUND;
			}
			
		}
		else
		{
			handler.errln("ERROR: '" + name + "' does not match a preset name nor hash.");
			return ERROR_NOT_FOUND;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(handler.prompt("Are you sure that you want to remove preset '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.deletePresetById(preset.id))
		{
			handler.errln("ERROR: Preset '" + preset.name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed preset '" + name + "'.");
		return ERROR_NONE;
	}

}
