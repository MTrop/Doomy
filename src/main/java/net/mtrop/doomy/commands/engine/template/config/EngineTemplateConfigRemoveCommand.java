package net.mtrop.doomy.commands.engine.template.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineTemplateConfigManager;
import net.mtrop.doomy.managers.EngineTemplateManager;

/**
 * A command that removes a single template config value.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigRemoveCommand implements DoomyCommand
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

		EngineTemplateConfigManager config = EngineTemplateConfigManager.get();
		
		if (!config.containsSetting(template, name))
		{
			handler.errln("ERROR: Could not remove template setting: '" + name + "'. It does not exist.");
			return ERROR_NOT_REMOVED;
		}
		
		if (!config.removeSetting(template, name))
		{
			handler.errln("ERROR: Could not remove template setting: '" + name + "'. An error may have occurred.");
			return ERROR_NOT_REMOVED;
		}
		
		handler.outln("Removed template setting: " + name);
		return ERROR_NONE;
	}

}
