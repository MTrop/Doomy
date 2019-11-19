package net.mtrop.doomy.commands;

import java.io.File;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.util.Common;

/**
 * A command that prints the config help output and exits.
 * @author Matthew Tropiano
 */
public class ConfigCommand implements DoomyCommand
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
			
		Common.splash(out, DoomyMain.VERSION);
		Common.help(out, CONFIG);
		out.println("\nConfig directory is: " + (new File(DoomyEnvironment.getConfigBasePath())).getAbsolutePath());
		return ERROR_NONE;
	}

}
