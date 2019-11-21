package net.mtrop.doomy.commands.wad.source;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
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
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager manager = WADManager.get();
		
		if (!manager.setWADSourceURL(name, url))
		{
			err.println("ERROR: Could not set WAD Source URL for '" + name + "'.");
			return ERROR_NOT_UPDATED;
		}
		else
		{
			WAD readValue = manager.getWAD(name);
			out.println("WAD Source for '" + name + "' is now '" + readValue.sourceURL + "'");
		}
		
		return ERROR_NONE;
	}

}
