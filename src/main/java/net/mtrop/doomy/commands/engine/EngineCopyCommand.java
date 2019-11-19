package net.mtrop.doomy.commands.engine;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that adds a new engine from an existing one.
 * @author Matthew Tropiano
 */
public class EngineCopyCommand implements DoomyCommand
{
	private String name;
	private String engine;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new engine.");
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of existing engine.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		EngineManager mgr = EngineManager.get();
		
		if (mgr.containsEngine(name))
		{
			err.println("ERROR: Engine '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (!mgr.containsEngine(engine))
		{
			err.println("ERROR: Source engine '" + engine + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.addEngineUsingEngine(name, engine) == null)
		{
			err.println("ERROR: Engine '" + name + "' could not be created from '" + engine + "'.");
			return ERROR_NOT_ADDED;
		}

		out.println("Created engine '" + name + "' from '" + engine + "'.");
		return ERROR_NONE;
	}

}
