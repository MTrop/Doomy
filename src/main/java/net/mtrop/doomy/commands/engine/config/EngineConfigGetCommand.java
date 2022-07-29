package net.mtrop.doomy.commands.engine.config;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineManager;

/**
 * A command that gets a single template setting value by name.
 * @author Matthew Tropiano
 */
public class EngineConfigGetCommand implements DoomyCommand
{
	private String engine;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected name of engine.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of setting.");
	}

	@Override
	public int call(IOHandler handler)
	{
		if (!EngineManager.get().containsEngine(engine))
		{
			handler.errln("ERROR: No such template: " + engine);
			return ERROR_NOT_FOUND;
		}

		String value = EngineConfigManager.get().getSetting(engine, name);
		
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
