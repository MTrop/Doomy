package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineManager;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing engine.
 * @author Matthew Tropiano
 */
public class EngineRemoveCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";
	
	private String name;
	private boolean quiet;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of engine to remove.");
		
		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_QUIET1) || matchArgument(args, SWITCH_QUIET2))
				quiet = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, quiet);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the engine name to remove.
	 * @param quiet if true, auto-confirm.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, boolean quiet)
	{
		EngineManager mgr = EngineManager.get();
		
		if (!mgr.containsEngine(name))
		{
			handler.errln("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(handler.prompt("Are you sure that you want to remove engine '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.removeEngine(name))
		{
			handler.errln("ERROR: Engine '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed engine '" + name + "'.");
		return ERROR_NONE;
	}

}
