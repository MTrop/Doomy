package net.mtrop.doomy.commands.engine.template.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineTemplateConfigManager;
import net.mtrop.doomy.managers.EngineTemplateManager;

/**
 * A command that sets a single template value.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigSetCommand implements DoomyCommand
{
	private String template;
	private String name;
	private String value;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		template = args.pollFirst();
		if (template == null)
			throw new BadArgumentException("Expected name of template.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
		value = args.pollFirst();
		if (value == null)
			throw new BadArgumentException("Expected value after name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		if (!EngineTemplateManager.get().containsTemplate(template))
		{
			handler.errln("ERROR: No such template: " + template);
			return ERROR_NOT_FOUND;
		}

		EngineTemplateConfigManager config = EngineTemplateConfigManager.get();
		
		String readValue;
		if (!config.setSetting(template, name, value))
		{
			handler.errln("ERROR: Could not set: " + name);
			return ERROR_NOT_ADDED;
		}
		else
		{
			readValue = config.getSetting(template, name);
			handler.outln("'" + name + "' is '" + readValue + "'");
		}
		
		return ERROR_NONE;
	}

}
