package net.mtrop.doomy.commands.engine.template;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyMain;
import net.mtrop.doomy.util.Common;

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
	public int call(PrintStream out, PrintStream err, InputStream in)
	{
		if (badCommand != null)
			err.println("ERROR: Unknown command: " + badCommand);
			
		if (badCommand == null)
			Common.splash(out, DoomyMain.VERSION);
		Common.help(out, ENGINE);
		return ERROR_NONE;
	}

}
