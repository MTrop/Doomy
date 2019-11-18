package net.mtrop.doomy.commands.config;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.ConfigManager;

/**
 * A command that removes a config value.
 * @author Matthew Tropiano
 */
public class ConfigRemoveCommand implements DoomyCommand
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
		ConfigManager config = ConfigManager.get();
		
		if (!config.containsValue(name))
		{
			err.println("ERROR: Could not remove: '" + name + "'. It does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!config.removeValue(name))
		{
			err.println("ERROR: Could not remove: '" + name + "'. An error may have occurred.");
			return ERROR_NOT_REMOVED;
		}
		
		out.println("Removed setting: " + name);
		return ERROR_NONE;
	}

}
