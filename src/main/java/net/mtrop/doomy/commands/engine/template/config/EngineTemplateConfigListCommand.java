package net.mtrop.doomy.commands.engine.template.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		EngineTemplateSettingEntry[] records = EngineTemplateConfigManager.get().getAllSettings(name, phrase);
		
		if (records == null)
		{
			handler.outln("Engine template '" + name + "' not found.");
			return ERROR_NONE;
		}

		if (records.length > 0)
		{
			int len = 4;
			for (int i = 0; i < records.length; i++)
				len = Math.max(records[i].name.length() + 1, len);
			String format = "%-" + len + "s %s\n";
			handler.outf(format, "Name", "Value");
			handler.outf(format, "====", "=====");
			for (int i = 0; i < records.length; i++)
				handler.outf(format, records[i].name, records[i].value);
		}
		handler.outln(records.length + " settings found.");
		return ERROR_NONE;
	}

}
