package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the basic usage and exits.
 * @author Matthew Tropiano
 */
public class UsageCommand implements DoomyCommand
{
	private String badCommand;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		badCommand = !args.isEmpty() ? args.pop() : null;
	}

	@Override
	public int call(IOHandler handler)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.splash(handler, DoomyMain.VERSION);
		DoomyCommon.usage(handler);
		return ERROR_NONE;
	}

}
