package net.mtrop.doomy.commands.engine.template;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;

/**
 * A command that handles template configuration.
 * @author Matthew Tropiano
 */
public class EngineTemplateConfigCommand implements DoomyCommand
{
	private String badCommand;
	
	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		badCommand = !args.isEmpty() ? args.pop() : null;
	}

	@Override
	public int call(IOHandler handler)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.help(handler, ENGINE);
		return ERROR_NONE;
	}

}
