/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.preset;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.PresetManager;

/**
 * A command that names a preset.
 * @author Matthew Tropiano
 */
public class PresetNameCommand implements DoomyCommand
{
	private String hash;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		hash = args.pollFirst();
		if (hash == null)
			throw new BadArgumentException("Expected preset hash.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, hash, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param hash the preset hash.
	 * @param name the preset name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String hash, String name)
	{
		PresetManager mgr = PresetManager.get();
		
		if (mgr.containsPreset(name))
		{
			handler.errln("ERROR: Preset '" + name + "' already exists. Choose a different name.");
			return ERROR_NOT_UPDATED;
		}
		
		if (mgr.countPreset(hash) < 1)
		{
			handler.errln("ERROR: No matching presets by hash '" + hash + "'.");
			return ERROR_NOT_FOUND;
		}

		if (mgr.countPreset(hash) > 1)
		{
			handler.errln("ERROR: Too many preset hashes start with '" + hash + "'.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.setPresetName(hash, name))
		{
			handler.errln("ERROR: Preset could not be updated.");
			return ERROR_NOT_UPDATED;
		}
		
		handler.outln("Named preset '" + name + "'.");
		return ERROR_NONE;
	}

}
