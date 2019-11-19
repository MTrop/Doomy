package net.mtrop.doomy.commands.iwad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;

/**
 * A command that renames an IWAD.
 * @author Matthew Tropiano
 */
public class IWADRenameCommand implements DoomyCommand
{
	private String name;
	private String newName;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of IWAD.");
		newName = args.pollFirst();
		if (newName == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		IWADManager mgr = IWADManager.get();
		
		if (!mgr.containsIWAD(name))
		{
			err.println("ERROR: IWAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsIWAD(newName))
		{
			err.println("ERROR: Source IWAD '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameIWAD(name, newName))
		{
			err.println("ERROR: IWAD '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		out.println("Renamed IWAD '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
