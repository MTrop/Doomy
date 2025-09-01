package net.mtrop.doomy.commands.wad;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.struct.util.FileUtils;

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

	private static final String[] FILETYPES = {"PK3", "PK7", "PKE", "WAD", "ZIP"};
	
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
						throw new BadArgumentException("Invalid switch: " + args.peekFirst());
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
	public int call(IOHandler handler)
	{
		return execute(handler, path, prefix, recurse, force);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param path the path to scan.
	 * @param prefix the prefix to add to each name. 
	 * @param recurse if true, recurse down the path.
	 * @param force if true, update existing IWADs.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String path, String prefix, boolean recurse, boolean force)
	{
		File startDir = new File(path);
		
		if (!startDir.exists())
		{
			handler.errln("ERROR: Path '" + startDir.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}
		
		AtomicInteger added = new AtomicInteger(0);
		AtomicInteger updated = new AtomicInteger(0);
		
		// Get count.
		handler.out("Finding WADs.... 0");
		final List<File> filesToAdd = new LinkedList<>();
		
		DoomyCommon.scanAndListen(startDir, recurse, WADFILTER, (file) -> 
		{
			WADManager manager = WADManager.get();
			String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
			if (manager.containsWAD(name) && force)
			{
				handler.out("\rFinding WADs.... " + updated.incrementAndGet());
				filesToAdd.add(file);
			}
			else if (!manager.containsWAD(name))
			{
				handler.out("\rFinding WADs.... " + added.incrementAndGet());
				filesToAdd.add(file);
			}
		});
		handler.outln();
		
		int totalCount = added.get() + updated.get();

		if (totalCount > 0)
		{
			// Do update.
			AtomicInteger count = new AtomicInteger(0);
			final String format = "\rAdding WADs: %-" + (int)(Math.log10(totalCount) + 1.0) + "d of " + totalCount + " (%3d%%)...";

			handler.outf(format, 0, 0);
			filesToAdd.forEach((file) -> 
			{
				WADManager manager = WADManager.get();
				String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
				if (manager.containsWAD(name) && force)
				{
					manager.setWADPath(name, file.getPath());
					int c = count.incrementAndGet();
					handler.outf(format, c, (c * 100 / totalCount));
				}
				else if (!manager.containsWAD(name))
				{
					manager.addWAD(name, file.getPath());
					int c = count.incrementAndGet();
					handler.outf(format, c, (c * 100 / totalCount));
				}
			});
			handler.outln();
		}
		
		if (added.get() > 0)
			handler.outln("Added " + added + " WADs.");
		if (updated.get() > 0)
			handler.outln("Updated " + updated + " WADs.");

		if (added.get() == 0 && updated.get() == 0)
			handler.outln("No WADs added/updated.");

		return ERROR_NONE;
	}

}
