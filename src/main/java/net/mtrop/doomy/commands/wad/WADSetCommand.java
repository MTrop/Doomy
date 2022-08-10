package net.mtrop.doomy.commands.wad;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;;

/**
 * A command that sets an WAD's path.
 * @author Matthew Tropiano
 */
public class WADSetCommand implements DoomyCommand
{
	private String name;
	private String path;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
		path = args.pollFirst();
		if (path == null)
			throw new BadArgumentException("Expected path after WAD name.");
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, name, path);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the WAD name.
	 * @param path the path to the WAD.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String path)
	{
		WADManager manager = WADManager.get();
		
		if (!manager.setWADPath(name, path))
		{
			handler.errln("ERROR: Could not set WAD path for '" + name + "'.");
			return ERROR_NOT_UPDATED;
		}
		else
		{
			WAD readValue = manager.getWAD(name);
			handler.outln("WAD '" + name + "' is now '" + readValue.path + "'");
		}
		
		return ERROR_NONE;
	}

}
