package net.mtrop.doomy.commands.wad;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that prints all stored IWADs.
 * @author Matthew Tropiano
 */
public class WADCleanCommand implements DoomyCommand
{
	private static final String SWITCH_QUIET1 = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";

	private boolean quiet;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		if (!args.isEmpty())
		{
			if (!matchArgument(args, SWITCH_QUIET1) && !matchArgument(args, SWITCH_QUIET2))
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
			else
				quiet = true;
		}
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager manager = WADManager.get();
		
		WAD[] records = manager.getAllWADs(null);
		
		int count = 0;
		int len = 0;
		for (int i = 0; i < records.length; i++)
		{
			len = Math.max(records[i].name.length() + 1, len);
			File file = new File(records[i].path);
			if (!file.exists())
				count++;
		}

		if (count == 0)
		{
			out.println("No IWADs missing.");
			return ERROR_NONE;
		}

		String format = "%-" + len + "s %s\n";
		
		if (!quiet)
		{
			String response = DoomyCommon.prompt(out, in, "Found " + count + " WADs with missing files. Remove them, or display (Y/N/D)?");
			if ("d".equalsIgnoreCase(response))
			{
				out.printf(format, "Name", "Path");
				out.printf(format, "====", "====");
				for (int i = 0; i < records.length; i++)
				{
					File file = new File(records[i].path);
					if (!file.exists())
						out.printf(format, records[i].name, records[i].path);
				}
				return ERROR_NONE;
			}
			else if (!"y".equalsIgnoreCase(response))
				return ERROR_NONE;
		}

		for (int i = 0; i < records.length; i++)
		{
			File file = new File(records[i].path);
			if (!file.exists())
				manager.removeWAD(records[i].name);
		}
		
		out.println(count + " WADs removed.");
		return ERROR_NONE;
	}

}
