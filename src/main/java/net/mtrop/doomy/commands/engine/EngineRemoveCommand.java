package net.mtrop.doomy.commands.engine;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.util.Common;

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
			throw new BadArgumentException("Expected name of template to remove.");
		
		if (!args.isEmpty())
		{
			if (!matchArgument(args, SWITCH_QUIET1) && !matchArgument(args, SWITCH_QUIET2))
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
			else
				quiet = true;
		}
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		EngineManager mgr = EngineManager.get();
		
		if (!mgr.containsEngine(name))
		{
			err.println("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(Common.prompt(out, in, "Are you sure that you want to remove engine '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.removeEngine(name))
		{
			err.println("ERROR: Engine '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		out.println("Removed engine '" + name + "'.");
		return ERROR_NONE;
	}

}
