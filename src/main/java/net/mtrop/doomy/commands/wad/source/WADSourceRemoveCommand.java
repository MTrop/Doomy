package net.mtrop.doomy.commands.wad.source;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that removes an existing WAD.
 * @author Matthew Tropiano
 */
public class WADSourceRemoveCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD to remove.");
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
		
		if (!mgr.setWADSourceURL(name, null))
		{
			handler.errln("ERROR: WAD '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}

		handler.outln("Removed WAD Source for '" + name + "'.");
		return ERROR_NONE;
	}

}
