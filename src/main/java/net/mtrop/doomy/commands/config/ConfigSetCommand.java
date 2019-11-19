package net.mtrop.doomy.commands.config;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.ConfigManager;

/**
 * A command that sets a config value.
 * @author Matthew Tropiano
 */
public class ConfigSetCommand implements DoomyCommand
{
	private String name;
	private String value;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
		value = args.pollFirst();
		if (value == null)
			throw new BadArgumentException("Expected value after setting name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		ConfigManager config = ConfigManager.get();
		
		String readValue;
		if (!config.setValue(name, value))
		{
			err.println("ERROR: Could not set: " + name);
			return ERROR_NOT_ADDED;
		}
		else
		{
			readValue = config.getValue(name);
			out.println("'" + name + "' is '" + readValue + "'");
		}
		
		return ERROR_NONE;
	}

}
