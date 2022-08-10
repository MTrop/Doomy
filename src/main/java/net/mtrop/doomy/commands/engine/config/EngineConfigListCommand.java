package net.mtrop.doomy.commands.engine.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettingEntry;

/**
 * A command that prints all stored settings for an engine.
 * @author Matthew Tropiano
 */
public class EngineConfigListCommand implements DoomyCommand
{
	private String engine;
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of engine.");
		phrase = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, engine, phrase);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param engine the engine name.
	 * @param phrase the setting name or phrase to search for.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String engine, String phrase)
	{
		EngineSettingEntry[] records = EngineConfigManager.get().getAllSettings(engine, phrase);
		
		if (records == null)
		{
			handler.outln("Engine '" + engine + "' not found.");
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
