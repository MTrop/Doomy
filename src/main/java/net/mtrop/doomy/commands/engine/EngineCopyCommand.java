package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		return execute(handler, name, engine);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the new engine name.
	 * @param engine the name of the engine to copy. 
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String engine)
	{
		EngineManager mgr = EngineManager.get();
		
		if (mgr.containsEngine(name))
		{
			handler.errln("ERROR: Engine '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (!mgr.containsEngine(engine))
		{
			handler.errln("ERROR: Source engine '" + engine + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.addEngineUsingEngine(name, engine) == null)
		{
			handler.errln("ERROR: Engine '" + name + "' could not be created from '" + engine + "'.");
			return ERROR_NOT_ADDED;
		}

		handler.outln("Created engine '" + name + "' from '" + engine + "'.");
		return ERROR_NONE;
	}

}
