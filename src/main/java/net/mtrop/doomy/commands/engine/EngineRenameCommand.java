package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
			throw new BadArgumentException("Expected name of engine.");
		newName = args.pollFirst();
		if (newName == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		EngineManager mgr = EngineManager.get();
		
		if (!mgr.containsEngine(name))
		{
			handler.errln("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.containsEngine(newName))
		{
			handler.errln("ERROR: Engine '" + newName + "' already exists. Choose a different name.");
			return ERROR_NOT_FOUND;
		}
		
		if (!mgr.renameEngine(name, newName))
		{
			handler.errln("ERROR: Engine '" + name + "' could not be renamed to '" + newName + "'.");
			return ERROR_NOT_RENAMED;
		}

		handler.outln("Renamed engine '" + name + "' to '" + newName + "'.");
		return ERROR_NONE;
	}

}
