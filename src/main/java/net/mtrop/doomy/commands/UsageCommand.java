package net.mtrop.doomy.commands;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.util.Common;

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
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		if (badCommand != null)
			err.println("ERROR: Unknown command: " + badCommand);
			
		Common.splash(out, DoomyMain.VERSION);
		Common.usage(out);
		return ERROR_NONE;
	}

}
