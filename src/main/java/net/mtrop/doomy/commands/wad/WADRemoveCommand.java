/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.wad;

import java.io.File;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing WAD.
 * @author Matthew Tropiano
 */
public class WADRemoveCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";
	private static final String SWITCH_FILE = "--file";
	
	private String name;
	private boolean quiet;
	private boolean removeFile;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD to remove.");
		
		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_QUIET1) || matchArgument(args, SWITCH_QUIET2))
				quiet = true;
			else if (matchArgument(args, SWITCH_FILE))
				removeFile = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
		
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, quiet, removeFile);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the WAD name.
	 * @param quiet if true, do not ask to confirm.
	 * @param removeFile if true, attempt to remove the file as well.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, boolean quiet, boolean removeFile)
	{
		WADManager mgr = WADManager.get();
		
		if (!mgr.containsWAD(name))
		{
			handler.errln("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(handler.prompt("Are you sure that you want to remove WAD '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		WAD wad = mgr.getWAD(name);
		
		if (!mgr.removeWAD(name))
		{
			handler.errln("ERROR: WAD '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}

		if (removeFile && wad.path != null)
		{
			if ((new File(wad.path)).delete())
				handler.outln("Removed WAD file.");
		}

		handler.outln("Removed WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
