package net.mtrop.doomy.commands.wad.dependency;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADDependencyManager;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that prints all dependencies of a WAD.
 * @author Matthew Tropiano
 */
public class WADDependencyListCommand implements DoomyCommand
{
	private static final String SWITCH_FULL1 = "--full";
	private static final String SWITCH_FULL2 = "-f";

	private String name;
	private boolean full;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");

		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_FULL1) || matchArgument(args, SWITCH_FULL2))
				full = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		if (!WADManager.get().containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		String[] deps = full 
			? WADDependencyManager.get().getFullDependencies(name) 
			: WADDependencyManager.get().getDependencies(name);
			
		if (deps.length > 0)
		{
			for (int i = 0; i < deps.length; i++)
				out.println(deps[i]);
		}
		else
		{
			out.println("No dependencies.");
		}
		return ERROR_NONE;
	}

}
