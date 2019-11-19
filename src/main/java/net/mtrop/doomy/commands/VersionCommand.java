package net.mtrop.doomy.commands;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;

/**
 * A command that prints the version and exits.
 * @author Matthew Tropiano
 */
public class VersionCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		out.println(DoomyMain.VERSION);
		return ERROR_NONE;
	}

}
