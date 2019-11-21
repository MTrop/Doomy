package net.mtrop.doomy.commands.wad.source;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that prints all stored WADs.
 * @author Matthew Tropiano
 */
public class WADSourceListCommand implements DoomyCommand
{
	private static final String SWITCH_SHOWBLANK1 = "--show-blank";
	private static final String SWITCH_SHOWBLANK2 = "-b";

	private String phrase;
	private boolean blankOnly;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();

		while (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_SHOWBLANK1) || matchArgument(args, SWITCH_SHOWBLANK2))
				blankOnly = true;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WAD[] records = blankOnly 
			? WADManager.get().getAllWADsWithNoSource(phrase) 
			: WADManager.get().getAllWADs(phrase);
				
		if (records.length > 0)
		{
			int len = 0;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			out.printf(format, "Name", "Source");
			out.printf(format, "====", "======");
			for (int i = 0; i < records.length; i++)
				out.printf(format, records[i].name, records[i].sourceURL != null ? records[i].sourceURL : "");
		}
		out.println(records.length + " WADs found.");
		return ERROR_NONE;
	}

}
