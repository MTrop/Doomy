package net.mtrop.doomy.commands.engine.template;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineTemplateManager;
import net.mtrop.doomy.managers.EngineTemplateManager.EngineTemplate;

/**
 * A command that prints all stored templates.
 * @author Matthew Tropiano
 */
public class EngineTemplateListCommand implements DoomyCommand
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
		EngineTemplate[] records = EngineTemplateManager.get().getAllTemplates(phrase);
		for (int i = 0; i < records.length; i++)
			out.println(records[i].name);
		out.println(records.length + " templates found.");
		return ERROR_NONE;
	}

}
