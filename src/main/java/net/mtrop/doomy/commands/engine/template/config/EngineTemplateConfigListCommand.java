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
	private String template;
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		template = args.pollFirst();
		if (template == null)
			throw new BadArgumentException("Expected name of template.");
		phrase = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, template, phrase);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param template the template name.
	 * @param phrase the name or phrase of the config entry to search for.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String template, String phrase)
	{
		EngineTemplateSettingEntry[] records = EngineTemplateConfigManager.get().getAllSettings(template, phrase);
		
		if (records == null)
		{
			handler.outln("Engine template '" + template + "' not found.");
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
