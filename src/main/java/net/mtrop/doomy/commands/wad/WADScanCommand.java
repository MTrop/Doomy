package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.struct.FileUtils;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

/**
 * A command that scans a directory for WADs.
 * @author Matthew Tropiano
 */
public class WADScanCommand implements DoomyCommand
{
	private static final String SWITCH_RECURSE1 = "--recurse";
	private static final String SWITCH_RECURSE2 = "-r";
	private static final String SWITCH_PREFIX1 = "--prefix";
	private static final String SWITCH_PREFIX2 = "-p";
	private static final String SWITCH_FORCEADD = "--force-add-existing";

	private static final String[] FILETYPES = {"PK3", "PK7", "WAD", "ZIP"};
	
	private static final FileFilter WADFILTER = (file) ->
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
			throw new BadArgumentException("Expected path to scan for WADs.");

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
						throw new BadArgumentException("Expected path to scan for WADs.");
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
	public int call(final PrintStream out, PrintStream err, BufferedReader in)
	{
		File startDir = new File(path);
		
		if (!startDir.exists())
		{
			err.println("ERROR: Path '" + startDir.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		AtomicInteger added = new AtomicInteger(0);
		AtomicInteger updated = new AtomicInteger(0);
		
		// Get count.
		out.print("Finding WADs.... 0");
		DoomyCommon.scanAndListen(startDir, recurse, WADFILTER, (file) -> 
		{
			WADManager manager = WADManager.get();
			String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
			if (manager.containsWAD(name) && force)
				out.print("\rFinding WADs.... " + updated.incrementAndGet());
			else if (!manager.containsWAD(name))
				out.print("\rFinding WADs.... " + added.incrementAndGet());
		});
		out.println();
		
		int totalCount = added.get() + updated.get();

		if (totalCount > 0)
		{
			// Do update.
			AtomicInteger count = new AtomicInteger(0);
			final String format = "\rAdding WADs: %-" + (int)(Math.log10(totalCount) + 1.0) + "d of " + totalCount + " (%3d%%)...";

			out.printf(format, 0, 0);
			DoomyCommon.scanAndListen(startDir, recurse, WADFILTER, (file) -> 
			{
				WADManager manager = WADManager.get();
				String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
				if (manager.containsWAD(name) && force)
				{
					manager.setWADPath(name, file.getPath());
					int c = count.incrementAndGet();
					out.printf(format, c, (c * 100 / totalCount));
				}
				else if (!manager.containsWAD(name))
				{
					manager.addWAD(name, file.getPath());
					int c = count.incrementAndGet();
					out.printf(format, c, (c * 100 / totalCount));
				}
			});
			out.println();
		}
		
		if (added.get() > 0)
			out.println("Added " + added + " WADs.");
		if (updated.get() > 0)
			out.println("Updated " + updated + " WADs.");

		if (added.get() == 0 && updated.get() == 0)
			out.println("No WADs added/updated.");

		return ERROR_NONE;
	}

}
