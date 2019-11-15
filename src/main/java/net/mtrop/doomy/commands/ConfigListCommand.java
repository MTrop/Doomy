package net.mtrop.doomy.commands;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.ConfigManager.ConfigSetting;

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
		ConfigSetting[] settings = ConfigManager.get().getAllValues(phrase);
		int len = 0;
		for (int i = 0; i < settings.length; i++)
			len = Math.max(settings[i].name.length() + 1, len);
		String format = "%-" + len + "s %s\n";
		out.printf(format, "-Name", "-Value");
		for (int i = 0; i < settings.length; i++)
			out.printf(format, settings[i].name, settings[i].value);
		out.println(settings.length + " settings found.");
		return ERROR_NONE;
	}

}
