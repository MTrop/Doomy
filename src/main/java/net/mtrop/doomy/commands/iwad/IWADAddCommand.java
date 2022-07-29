package net.mtrop.doomy.commands.iwad;

import java.io.File;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		IWADManager mgr = IWADManager.get();
		
		if (mgr.containsIWAD(name))
		{
			handler.errln("ERROR: IWAD '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (!(new File(path)).exists())
		{
			handler.errln("ERROR: IWAD '" + path + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.addIWAD(name, path) == null)
		{
			handler.errln("ERROR: IWAD '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		handler.outln("Created IWAD '" + name + "'.");
		return ERROR_NONE;
	}

}
