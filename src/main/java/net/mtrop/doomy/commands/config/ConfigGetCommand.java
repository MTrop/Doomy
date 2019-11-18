package net.mtrop.doomy.commands.config;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.ConfigManager;

/**
 * A command that prints a single setting value by name.
 * @author Matthew Tropiano
 */
public class ConfigGetCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		String value = ConfigManager.get().getValue(name);
		
		if (value == null)
		{
			err.println("ERROR: No such setting: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			out.println(value);
		}
		
		return ERROR_NONE;
	}

}
