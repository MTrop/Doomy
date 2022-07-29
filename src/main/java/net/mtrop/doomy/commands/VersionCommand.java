package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.IOHandler;

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
	public int call(IOHandler handler)
	{
		handler.outln(DoomyMain.VERSION);
		return ERROR_NONE;
	}

}
