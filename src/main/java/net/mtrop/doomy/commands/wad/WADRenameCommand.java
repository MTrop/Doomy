package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that renames an WAD.
 * @author Matthew Tropiano
 */
public class WADRenameCommand implements DoomyCommand
{
	private String name;
	private String newName;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		newName = args.pollFirst();
		if (newName == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager mgr = WADManager.get();
		
		if (!mgr.containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsWAD(newName))
		{
			err.println("ERROR: Source WAD '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameWAD(name, newName))
		{
			err.println("ERROR: WAD '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		out.println("Renamed WAD '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
