package net.mtrop.doomy.commands.engine.config;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that gets a single template setting value by name.
 * @author Matthew Tropiano
 */
public class EngineConfigGetCommand implements DoomyCommand
{
	private String engine;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of engine.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		if (!EngineManager.get().containsEngine(engine))
		{
			err.println("ERROR: No such template: " + engine);
			return ERROR_NOT_FOUND;
		}

		String value = EngineConfigManager.get().getSetting(engine, name);
		
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
