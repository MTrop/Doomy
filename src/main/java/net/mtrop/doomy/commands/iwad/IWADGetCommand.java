package net.mtrop.doomy.commands.iwad;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;

/**
 * A command that gets an IWAD path by name.
 * @author Matthew Tropiano
 */
public class IWADGetCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of IWAD.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		IWAD iwad = IWADManager.get().getIWAD(name);
		
		if (iwad == null)
		{
			err.println("ERROR: No such IWAD: " + name);
			return ERROR_NOT_FOUND;
		}
		else
		{
			out.println(iwad.path);
		}
		
		return ERROR_NONE;
	}

}
