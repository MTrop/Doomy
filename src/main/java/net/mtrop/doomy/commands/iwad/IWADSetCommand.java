package net.mtrop.doomy.commands.iwad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;;

/**
 * A command that sets an IWAD's path.
 * @author Matthew Tropiano
 */
public class IWADSetCommand implements DoomyCommand
{
	private String name;
	private String path;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of IWAD.");
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected path after IWAD name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, path);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the IWAD name.
	 * @param path the path to the IWAD.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String path)
	{
		IWADManager manager = IWADManager.get();
		
		if (!manager.setIWADPath(name, path))
		{
			handler.errln("ERROR: Could not set IWAD path for '" + name + "'.");
			return ERROR_NOT_UPDATED;
		}
		else
		{
			IWAD readValue = manager.getIWAD(name);
			handler.outln("IWAD '" + name + "' is now '" + readValue.path + "'");
		}
		
		return ERROR_NONE;
	}

}
