package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyMain;

/**
 * A command that prints the WAD help output and exits.
 * @author Matthew Tropiano
 */
public class WADDependencyCommand implements DoomyCommand
{
	private String badCommand;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		badCommand = !args.isEmpty() ? args.pop() : null;
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		if (badCommand != null)
			err.println("ERROR: Unknown command: " + badCommand);
			
		if (badCommand == null)
			DoomyCommon.splash(out, DoomyMain.VERSION);
		DoomyCommon.help(out, WAD);
		return ERROR_NONE;
	}

}
