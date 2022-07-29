package net.mtrop.doomy.commands.wad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		WADManager mgr = WADManager.get();
		
		if (!mgr.containsWAD(name))
		{
			handler.errln("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsWAD(newName))
		{
			handler.errln("ERROR: Source WAD '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameWAD(name, newName))
		{
			handler.errln("ERROR: WAD '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		handler.outln("Renamed WAD '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
