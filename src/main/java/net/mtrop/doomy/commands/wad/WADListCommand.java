package net.mtrop.doomy.commands.wad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		return execute(handler, phrase);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param phrase the WAD name or phrase to search for.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String phrase)
	{
		WAD[] records = WADManager.get().getAllWADs(phrase);
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
		handler.outln(records.length + " WADs found.");
		return ERROR_NONE;
	}

}
