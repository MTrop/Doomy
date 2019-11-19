package net.mtrop.doomy.commands.iwad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;

/**
 * A command that adds a new IWAD.
 * @author Matthew Tropiano
 */
public class IWADAddCommand implements DoomyCommand
{
	private String name;
	private String path;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new IWAD.");
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected IWAD path.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		IWADManager mgr = IWADManager.get();
		
		if (mgr.containsIWAD(name))
		{
			err.println("ERROR: IWAD '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (mgr.addIWAD(name, path) == null)
		{
			err.println("ERROR: IWAD '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		out.println("Created IWAD '" + name + "'.");
		return ERROR_NONE;
	}

}
