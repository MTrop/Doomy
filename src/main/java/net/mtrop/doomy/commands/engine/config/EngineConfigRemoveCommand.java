package net.mtrop.doomy.commands.engine.config;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that removes a single engine setting value.
 * @author Matthew Tropiano
 */
public class EngineConfigRemoveCommand implements DoomyCommand
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
			err.println("ERROR: No such engine: " + engine);
			return ERROR_NOT_FOUND;
		}

		EngineConfigManager config = EngineConfigManager.get();
		
		if (!config.containsSetting(engine, name))
		{
			err.println("ERROR: Could not remove engine setting: '" + name + "'. It does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!config.removeSetting(engine, name))
		{
			err.println("ERROR: Could not remove engine setting: '" + name + "'. An error may have occurred.");
			return ERROR_NOT_REMOVED;
		}
		
		out.println("Removed engine setting: " + name);
		return ERROR_NONE;
	}

}
