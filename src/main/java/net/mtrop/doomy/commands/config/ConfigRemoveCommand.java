package net.mtrop.doomy.commands.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		return execute(handler, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the setting name. 
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name)
	{
		ConfigManager config = ConfigManager.get();
		
		if (!config.containsValue(name))
		{
			handler.errln("ERROR: Could not remove: '" + name + "'. It does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!config.removeValue(name))
		{
			handler.errln("ERROR: Could not remove: '" + name + "'. An error may have occurred.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed setting: " + name);
		return ERROR_NONE;
	}

}
