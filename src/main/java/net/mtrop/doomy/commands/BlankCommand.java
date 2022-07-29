package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;

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
	public int call(IOHandler handler)
	{
		// TODO: Finish this.
		return ERROR_NONE;
	}

}
