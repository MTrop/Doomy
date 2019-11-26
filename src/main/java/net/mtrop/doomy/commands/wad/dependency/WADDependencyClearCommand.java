package net.mtrop.doomy.commands.wad.dependency;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADDependencyManager;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that clears all dependencies for a WAD.
 * @author Matthew Tropiano
 */
public class WADDependencyClearCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		if (!WADManager.get().containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!WADDependencyManager.get().clearDependency(name))
		{
			out.println("No dependencies removed for WAD '" + name + "'.");
			return ERROR_NONE;
		}
		
		out.println("All dependencies removed for WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
