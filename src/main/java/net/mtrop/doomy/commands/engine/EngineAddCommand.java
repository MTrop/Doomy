package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineTemplateManager;

/**
 * A command that adds a new engine.
 * @author Matthew Tropiano
 */
public class EngineAddCommand implements DoomyCommand
{
	private String name;
	private String template;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of new engine.");
		template = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		EngineManager mgr = EngineManager.get();
		
		if (mgr.containsEngine(name))
		{
			handler.errln("ERROR: Engine '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}
		
		if (template != null)
		{
			if (!EngineTemplateManager.get().containsTemplate(template))
			{
				handler.errln("ERROR: Source engine template '" + template + "' does not exist.");
				return ERROR_NOT_FOUND;
			}
			
			if (mgr.addEngineUsingTemplate(name, template) == null)
			{
				handler.errln("ERROR: Engine '" + name + "' could not be created from template '" + template + "'.");
				return ERROR_NOT_ADDED;
			}

			handler.outln("Created engine '" + name + "' from template '" + template + "'.");
			return ERROR_NONE;
		}
		
		if (mgr.addEngine(name) == null)
		{
			handler.errln("ERROR: Engine '" + name + "' could not be created.");
			return ERROR_NOT_ADDED;
		}
		
		handler.outln("Created engine '" + name + "'.");
		return ERROR_NONE;
	}

}
