package net.mtrop.doomy.commands.engine.config;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that sets a single engine setting value.
 * @author Matthew Tropiano
 */
public class EngineConfigSetCommand implements DoomyCommand
{
	private String engine;
	private String name;
	private String value;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of engine.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
		value = args.pollFirst();
		if (value == null)
			throw new BadArgumentException("Expected value after name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		if (!EngineManager.get().containsEngine(engine))
		{
			err.println("ERROR: No such engine: " + engine);
			return ERROR_NOT_FOUND;
		}

		EngineConfigManager config = EngineConfigManager.get();
		
		String readValue;
		if (!config.setSetting(engine, name, value))
		{
			err.println("ERROR: Could not set: " + name);
			return ERROR_NOT_ADDED;
		}
		else
		{
			readValue = config.getSetting(engine, name);
			out.println("'" + name + "' is '" + readValue + "'");
		}
		
		return ERROR_NONE;
	}

}
