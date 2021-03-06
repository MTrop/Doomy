package net.mtrop.doomy.commands.wad;

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
public class WADListCommand implements DoomyCommand
{
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WAD[] records = WADManager.get().getAllWADs(phrase);
		if (records.length > 0)
		{
			int len = 4;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			out.printf(format, "Name", "Path");
			out.printf(format, "====", "====");
			for (int i = 0; i < records.length; i++)
				out.printf(format, records[i].name, records[i].path);
		}
		out.println(records.length + " WADs found.");
		return ERROR_NONE;
	}

}
