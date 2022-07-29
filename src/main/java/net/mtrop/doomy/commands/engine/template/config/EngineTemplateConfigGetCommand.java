package net.mtrop.doomy.commands.engine.template.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineTemplateConfigManager;
import net.mtrop.doomy.managers.EngineTemplateManager;

/**
 * A command that gets a single template setting value by name.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigGetCommand implements DoomyCommand
{
	private String template;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		template = args.pollFirst();
		if (template == null)
			throw new BadArgumentException("Expected name of template.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(IOHandler handler)
	{
		if (!EngineTemplateManager.get().containsTemplate(template))
		{
			handler.errln("ERROR: No such template: " + template);
			return ERROR_NOT_FOUND;
		}

		String value = EngineTemplateConfigManager.get().getSetting(template, name);
		
		if (value == null)
		{
			handler.errln("ERROR: No such setting: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			handler.outln(value);
		}
		
		return ERROR_NONE;
	}

}
