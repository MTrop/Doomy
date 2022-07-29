package net.mtrop.doomy.commands.wad.source;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;;

/**
 * A command that sets an WAD's path.
 * @author Matthew Tropiano
 */
public class WADSourceSetCommand implements DoomyCommand
{
	private String name;
	private String url;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		url = args.pollFirst();
		if (url == null)
			throw new BadArgumentException("Expected URL after WAD name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		WADManager manager = WADManager.get();
		
		if (!manager.setWADSourceURL(name, url))
		{
			handler.errln("ERROR: Could not set WAD Source URL for '" + name + "'.");
			return ERROR_NOT_UPDATED;
		}
		else
		{
			WAD readValue = manager.getWAD(name);
			handler.outln("WAD Source for '" + name + "' is now '" + readValue.sourceUrl + "'");
		}
		
		return ERROR_NONE;
	}

}
