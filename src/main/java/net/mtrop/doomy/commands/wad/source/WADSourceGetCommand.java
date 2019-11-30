package net.mtrop.doomy.commands.wad.source;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
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
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WAD wad = WADManager.get().getWAD(name);
		
		if (wad == null)
		{
			err.println("ERROR: No such WAD: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			out.println(wad.sourceUrl);
		}
		
		return ERROR_NONE;
	}

}
