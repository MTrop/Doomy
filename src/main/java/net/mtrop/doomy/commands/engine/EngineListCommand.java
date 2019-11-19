package net.mtrop.doomy.commands.engine;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
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
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		Engine[] records = EngineManager.get().getAllEngines(phrase);
		for (int i = 0; i < records.length; i++)
			out.println(records[i].name);
		out.println(records.length + " engines found.");
		return ERROR_NONE;
	}

}
