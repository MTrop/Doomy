package net.mtrop.doomy.commands.wad.source;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that gets an WAD Source URL by name.
 * @author Matthew Tropiano
 */
public class WADSourceGetCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
	}

	@Override
	public int call(IOHandler handler)
	{
		WAD wad = WADManager.get().getWAD(name);
		
		if (wad == null)
		{
			handler.errln("ERROR: No such WAD: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			handler.outln(wad.sourceUrl);
		}
		
		return ERROR_NONE;
	}

}
