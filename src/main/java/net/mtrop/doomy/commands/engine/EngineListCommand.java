package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineManager.Engine;

/**
 * A command that prints all stored templates.
 * @author Matthew Tropiano
 */
public class EngineListCommand implements DoomyCommand
{
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		Engine[] records = EngineManager.get().getAllEngines(phrase);
		for (int i = 0; i < records.length; i++)
			handler.outln(records[i].name);
		handler.outln(records.length + " engines found.");
		return ERROR_NONE;
	}

}
