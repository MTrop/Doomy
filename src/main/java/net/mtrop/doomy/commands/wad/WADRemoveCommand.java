package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.util.Common;

import static net.mtrop.doomy.DoomyCommand.*;

/**
 * A command that removes an existing WAD.
 * @author Matthew Tropiano
 */
public class WADRemoveCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";
	private static final String SWITCH_FILE = "--file";
	
	private String name;
	private boolean quiet;
	private boolean removeFile;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD to remove.");
		
		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_QUIET1) || matchArgument(args, SWITCH_QUIET2))
				quiet = true;
			else if (matchArgument(args, SWITCH_FILE))
				removeFile = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
		
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager mgr = WADManager.get();
		
		if (!mgr.containsWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}
		
		if (!quiet)
		{
			if (!"y".equalsIgnoreCase(Common.prompt(out, in, "Are you sure that you want to remove WAD '" + name +"' (Y/N)?")))
				return ERROR_NONE;
		}
		
		WAD wad = mgr.getWAD(name);
		
		if (!mgr.removeWAD(name))
		{
			err.println("ERROR: WAD '" + name + "' could not be removed.");
			return ERROR_NOT_REMOVED;
		}

		if (removeFile && wad.path != null)
		{
			if ((new File(wad.path)).delete())
				out.println("Removed WAD file.");
		}

		out.println("Removed WAD '" + name + "'.");
		return ERROR_NONE;
	}

}
