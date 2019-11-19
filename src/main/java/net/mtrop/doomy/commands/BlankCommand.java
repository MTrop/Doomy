package net.mtrop.doomy.commands;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;

/**
 * A command that [DOES NOTHING].
 * @author Matthew Tropiano
 */
public class BlankCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// TODO: Finish this.
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		// TODO: Finish this.
		return ERROR_NONE;
	}

}
