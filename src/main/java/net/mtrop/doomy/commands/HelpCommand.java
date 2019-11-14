package net.mtrop.doomy.commands;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.util.Common;

/**
 * A command that prints the help output and exits.
 * @author Matthew Tropiano
 */
public class HelpCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		Common.splash(System.out, DoomyMain.VERSION);
		Common.help(System.out, null);
		return ERROR_NONE;
	}

}
