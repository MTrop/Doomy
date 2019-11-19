package net.mtrop.doomy.commands.iwad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.struct.FileUtils;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

/**
 * A command that scans a directory for IWADs.
 * @author Matthew Tropiano
 */
public class IWADScanCommand implements DoomyCommand
{
	private static final String SWITCH_RECURSE1 = "--recurse";
	private static final String SWITCH_RECURSE2 = "-r";
	private static final String SWITCH_PREFIX1 = "--prefix";
	private static final String SWITCH_PREFIX2 = "-p";
	private static final String SWITCH_FORCEADD = "--force-add-existing";

	private static final String[] FILETYPES = {"IPK3", "IPK7", "IWAD", "PK3", "PK7", "WAD"};
	
	private static final FileFilter IWADFILTER = (file) ->
	{
		if (file.isDirectory())
			return true;
		String ext = FileUtils.getFileExtension(file).toUpperCase();
		return Arrays.binarySearch(FILETYPES, ext) >= 0;
	};
	
	private String path;
	private String prefix;
	private boolean recurse;
	private boolean force;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected path to scan for IWADs.");

		prefix = "";
		recurse = false;
		force = false;
		
		final int STATE_START = 0;
		final int STATE_PREFIX = 1;
		int state = STATE_START;
		while (!args.isEmpty())
		{
			switch (state)
			{
				case STATE_START:
				{
					if (matchArgument(args, SWITCH_RECURSE1) || matchArgument(args, SWITCH_RECURSE2))
						recurse = true;
					else if (matchArgument(args, SWITCH_PREFIX1) || matchArgument(args, SWITCH_PREFIX2))
						state = STATE_PREFIX;
					else if (matchArgument(args, SWITCH_FORCEADD))
						force = true;
					else
						throw new BadArgumentException("Expected path to scan for IWADs.");
				}
				break;

				case STATE_PREFIX:
				{
					prefix = args.pollFirst();
					state = STATE_START;
				}
				break;
			}
		}
		
		if (state == STATE_PREFIX)
			throw new BadArgumentException("Expected name prefix after prefix switch.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		Deque<File> fileQueue = new LinkedList<>();
		
		File startDir = new File(path);
		
		if (!startDir.exists())
		{
			err.println("ERROR: Path '" + startDir.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		for (File f : startDir.listFiles(IWADFILTER))
			fileQueue.add(f);

		int added = 0;
		int updated = 0;
		
		IWADManager manager = IWADManager.get();
		
		while (!fileQueue.isEmpty())
		{
			File file = fileQueue.pollFirst();
			if (file.isDirectory())
			{
				if (recurse)
				{
					for (File f : file.listFiles(IWADFILTER))
						fileQueue.add(f);
				}
				continue;
			}
			
			String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
			
			if (manager.containsIWAD(name) && force)
			{
				manager.setIWADPath(name, file.getPath());
				updated++;
			}
			else if (!manager.containsIWAD(name))
			{
				manager.addIWAD(name, file.getPath());
				added++;
			}
		}

		if (added > 0)
			out.println("Added " + added + " IWADs.");
		if (updated > 0)
			out.println("Updated " + updated + " IWADs.");

		if (added == 0 && updated == 0)
			out.println("No IWADs added/updated.");

		return ERROR_NONE;
	}

}
