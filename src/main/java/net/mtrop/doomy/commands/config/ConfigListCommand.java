package net.mtrop.doomy.commands.config;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.ConfigManager.ConfigSettingEntry;

/**
 * A command that prints all stored settings.
 * @author Matthew Tropiano
 */
public class ConfigListCommand implements DoomyCommand
{
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();
	}

	@Override
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		ConfigSettingEntry[] records = ConfigManager.get().getAllValues(phrase);
		if (records.length > 0)
		{
			int len = 0;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			out.printf(format, "-Name", "-Value");
			for (int i = 0; i < records.length; i++)
				out.printf(format, records[i].name, records[i].value);
		}
		out.println(records.length + " settings found.");
		return ERROR_NONE;
	}

}
