package net.mtrop.doomy.commands.wad;

import java.io.File;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		WADManager mgr = WADManager.get();
		
		if (mgr.containsWAD(name))
		{
			handler.errln("ERROR: WAD '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (!(new File(path)).exists())
		{
			handler.errln("ERROR: WAD '" + path + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.addWAD(name, path) == null)
		{
			handler.errln("ERROR: WAD '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		handler.outln("Created WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
