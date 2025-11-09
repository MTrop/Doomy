/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.wad;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.File;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that prints all stored IWADs.
 * @author Matthew Tropiano
 */
public class WADCleanCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";

	private boolean quiet;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
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
		return execute(handler, quiet);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param quiet if true, do not ask to confirm.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, boolean quiet)
	{
		WADManager manager = WADManager.get();
		
		WAD[] records = manager.getAllWADs(null);
		
		int count = 0;
		int len = 0;
		for (int i = 0; i < records.length; i++)
		{
			len = Math.max(records[i].name.length() + 1, len);
			File file = new File(records[i].path);
			if (!file.exists())
				count++;
		}

		if (count == 0)
		{
			handler.outln("No IWADs missing.");
			return ERROR_NONE;
		}

		String format = "%-" + len + "s %s\n";
		
		if (!quiet)
		{
			String response = handler.prompt("Found " + count + " WADs with missing files. Remove them, or display (Y/N/D)?");
			if ("d".equalsIgnoreCase(response))
			{
				handler.outf(format, "Name", "Path");
				handler.outf(format, "====", "====");
				for (int i = 0; i < records.length; i++)
				{
					File file = new File(records[i].path);
					if (!file.exists())
						handler.outf(format, records[i].name, records[i].path);
				}
				return ERROR_NONE;
			}
			else if (!"y".equalsIgnoreCase(response))
				return ERROR_NONE;
		}

		for (int i = 0; i < records.length; i++)
		{
			File file = new File(records[i].path);
			if (!file.exists())
				manager.removeWAD(records[i].name);
		}
		
		handler.outln(count + " WADs removed.");
		return ERROR_NONE;
	}

}
