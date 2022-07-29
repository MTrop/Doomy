package net.mtrop.doomy.commands;

import java.io.File;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;

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
	public int call(IOHandler handler)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.help(handler, CONFIG);
		handler.outln("\nConfig directory is: " + (new File(DoomyEnvironment.getConfigBasePath())).getAbsolutePath());
		return ERROR_NONE;
	}

}
