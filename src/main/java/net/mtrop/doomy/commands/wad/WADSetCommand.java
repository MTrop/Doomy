package net.mtrop.doomy.commands.wad;

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
public class WADSetCommand implements DoomyCommand
{
	private String name;
	private String path;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected path after WAD name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager manager = WADManager.get();
		
		if (!manager.setWADPath(name, path))
		{
			err.println("ERROR: Could not set WAD path for '" + name + "'.");
			return ERROR_NOT_UPDATED;
		}
		else
		{
			WAD readValue = manager.getWAD(name);
			out.println("WAD '" + name + "' is now '" + readValue.path + "'");
		}
		
		return ERROR_NONE;
	}

}
