package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that adds a new WAD.
 * @author Matthew Tropiano
 */
public class WADAddCommand implements DoomyCommand
{
	private String name;
	private String path;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new WAD.");
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected WAD path.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager mgr = WADManager.get();
		
		if (mgr.containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (!(new File(path)).exists())
		{
			err.println("ERROR: WAD '" + path + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.addWAD(name, path) == null)
		{
			err.println("ERROR: WAD '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		out.println("Created WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
