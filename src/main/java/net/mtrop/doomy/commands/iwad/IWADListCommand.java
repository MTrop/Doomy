package net.mtrop.doomy.commands.iwad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		IWAD[] records = IWADManager.get().getAllIWADs(phrase);
		if (records.length > 0)
		{
			int len = 4;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			handler.outf(format, "Name", "Path");
			handler.outf(format, "====", "====");
			for (int i = 0; i < records.length; i++)
				handler.outf(format, records[i].name, records[i].path);
		}
		handler.outln(records.length + " IWADs found.");
		return ERROR_NONE;
	}

}
