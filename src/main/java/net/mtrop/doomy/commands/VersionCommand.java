package net.mtrop.doomy.commands;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.struct.Common;

/**
 * A command that prints the version and exits.
 * @author Matthew Tropiano
 */
public class VersionCommand extends Object implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		Common.splash(out, DoomyMain.VERSION);
		return ERROR_NONE;
	}

}
