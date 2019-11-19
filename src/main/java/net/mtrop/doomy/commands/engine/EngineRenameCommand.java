package net.mtrop.doomy.commands.engine;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that renames an engine.
 * @author Matthew Tropiano
 */
public class EngineRenameCommand implements DoomyCommand
{
	private String name;
	private String newName;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new engine.");
		newName = args.pollFirst();
		if (newName == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		EngineManager mgr = EngineManager.get();
		
		if (!mgr.containsEngine(name))
		{
			err.println("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_ADDED;
		}
		
		if (mgr.containsEngine(newName))
		{
			err.println("ERROR: Source engine '" + newName + "' already exists.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameEngine(name, newName))
		{
			err.println("ERROR: Engine '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_ADDED;
		}

		out.println("Renamed engine '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
