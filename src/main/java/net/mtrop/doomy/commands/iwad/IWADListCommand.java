package net.mtrop.doomy.commands.iwad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;

/**
 * A command that prints all stored IWADs.
 * @author Matthew Tropiano
 */
public class IWADListCommand implements DoomyCommand
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
		IWAD[] records = IWADManager.get().getAllIWADs(phrase);
		if (records.length > 0)
		{
			int len = 0;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			out.printf(format, "Name", "Path");
			out.printf(format, "====", "====");
			for (int i = 0; i < records.length; i++)
				out.printf(format, records[i].name, records[i].path);
		}
		out.println(records.length + " IWADs found.");
		return ERROR_NONE;
	}

}
