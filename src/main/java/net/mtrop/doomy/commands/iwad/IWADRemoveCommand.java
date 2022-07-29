package net.mtrop.doomy.commands.iwad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.IWADManager;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing IWAD.
 * @author Matthew Tropiano
 */
public class IWADRemoveCommand implements DoomyCommand
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
			throw new BadArgumentException("Expected name of IWAD to remove.");
		
		if (!args.isEmpty())
		{
			if (!matchArgument(args, SWITCH_QUIET1) && !matchArgument(args, SWITCH_QUIET2))
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
			else
				quiet = true;
		}
	}

	@Override
	public int call(IOHandler handler)
	{
		IWADManager mgr = IWADManager.get();
		
		if (!mgr.containsIWAD(name))
		{
			handler.errln("ERROR: IWAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(handler.prompt("Are you sure that you want to remove IWAD '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.removeIWAD(name))
		{
			handler.errln("ERROR: IWAD '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed IWAD '" + name + "'.");
		return ERROR_NONE;
	}

}
