package net.mtrop.doomy.commands.engine.template.config;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineTemplateConfigManager;
import net.mtrop.doomy.managers.EngineTemplateConfigManager.EngineTemplateSettingEntry;

/**
 * A command that prints all stored settings for an engine template.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigListCommand implements DoomyCommand
{
	private String name;
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of template.");
		phrase = args.pollFirst();
	}

	@Override
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		EngineTemplateSettingEntry[] records = EngineTemplateConfigManager.get().getAllValues(name, phrase);
		
		if (records == null)
		{
			out.println("Engine template '" + name + "' not found.");
			return ERROR_NONE;
		}

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
