package net.mtrop.doomy.commands.iwad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		IWADManager mgr = IWADManager.get();
		
		if (!mgr.containsIWAD(name))
		{
			handler.errln("ERROR: IWAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsIWAD(newName))
		{
			handler.errln("ERROR: Source IWAD '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameIWAD(name, newName))
		{
			handler.errln("ERROR: IWAD '" + name + "' could not be created from '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		handler.outln("Renamed IWAD '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
