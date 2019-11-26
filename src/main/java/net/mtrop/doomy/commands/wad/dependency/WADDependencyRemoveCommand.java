package net.mtrop.doomy.commands.wad.dependency;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADDependencyManager;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that removes an existing WAD dependency.
 * @author Matthew Tropiano
 */
public class WADDependencyRemoveCommand implements DoomyCommand
{
	private String name;
	private String dependencyName;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		dependencyName = args.pollFirst();
		if (dependencyName == null)
			throw new BadArgumentException("Expected name of WAD dependency to remove.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager wadmgr = WADManager.get();
		WADDependencyManager mgr = WADDependencyManager.get();
		
		if (!wadmgr.containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!wadmgr.containsWAD(dependencyName))
		{
			err.println("ERROR: WAD '" + dependencyName + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.containsDependency(name, dependencyName))
		{
			err.println("ERROR: WAD '" + dependencyName + "' is not a dependency for WAD '" + name + "'.");
			return ERROR_NOT_REMOVED;
		}

		if (!mgr.removeDependency(name, dependencyName))
		{
			err.println("ERROR: WAD dependency '" + dependencyName + "' for WAD '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}

		out.println("Removed WAD dependency '" + dependencyName + "' for WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
