package net.mtrop.doomy.commands.engine.template;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.EngineTemplateManager;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing engine template.
 * @author Matthew Tropiano
 */
public class EngineTemplateRemoveCommand implements DoomyCommand
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
		
		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_QUIET1) || matchArgument(args, SWITCH_QUIET2))
				quiet = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		EngineTemplateManager mgr = EngineTemplateManager.get();
		
		if (!mgr.containsTemplate(name))
		{
			err.println("ERROR: Engine template '" + name + "' does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(DoomyCommon.prompt(out, in, "Are you sure that you want to remove template '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		if (!mgr.removeTemplate(name))
		{
			err.println("ERROR: Engine template '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}
		
		out.println("Removed engine template '" + name + "'.");
		return ERROR_NONE;
	}

}
