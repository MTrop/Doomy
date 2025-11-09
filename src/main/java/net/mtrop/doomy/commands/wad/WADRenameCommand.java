/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.wad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that renames an WAD.
 * @author Matthew Tropiano
 */
public class WADRenameCommand implements DoomyCommand
{
	private String name;
	private String newName;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		newName = args.pollFirst();
		if (newName == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, newName);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the WAD name.
	 * @param newName the new WAD name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String newName)
	{
		WADManager mgr = WADManager.get();
		
		if (!mgr.containsWAD(name))
		{
			handler.errln("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsWAD(newName))
		{
			handler.errln("ERROR: Source WAD '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameWAD(name, newName))
		{
			handler.errln("ERROR: WAD '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		handler.outln("Renamed WAD '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
