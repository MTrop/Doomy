package net.mtrop.doomy.commands.iwad;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.util.Common;

/**
 * A command that prints all stored IWADs.
 * @author Matthew Tropiano
 */
public class IWADCleanCommand implements DoomyCommand
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
		IWADManager manager = IWADManager.get();
		
		IWAD[] records = manager.getAllIWADs(null);
		
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
			String response = Common.prompt(out, in, "Found " + count + " IWADs with missing files. Remove them, or display (Y/N/D)?");
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
				manager.removeIWAD(records[i].name);
		}
		
		out.println(count + " IWADs removed.");
		return ERROR_NONE;
	}

}
