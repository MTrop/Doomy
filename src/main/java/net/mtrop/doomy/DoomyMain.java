package net.mtrop.doomy;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import net.mtrop.doomy.DoomyCommand.BadArgumentException;
import net.mtrop.doomy.DoomyCommand.BadCommandException;
import net.mtrop.doomy.managers.DatabaseManager;
import net.mtrop.doomy.struct.Common;

/**
 * Main class for Doomy.
 * @author Matthew Tropiano
 */
public final class DoomyMain
{
	public static final String VERSION = Common.getVersionString("doomy");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_COMMAND = 1;
	private static final int ERROR_BAD_ARGUMENT = 2;

	public static void main(String[] args) 
	{
		if (!DatabaseManager.databaseExists())
		{
			Common.splash(System.out, VERSION);
			System.out.println("Preparing for first use...");
			System.out.println("Creating database...");
			DatabaseManager.get();
			System.out.println("Done.");
		}

		Deque<String> arguments = new LinkedList<String>(Arrays.asList(args));

		if (arguments.isEmpty())
		{
			Common.splash(System.out, VERSION);
			Common.usage(System.out);
			System.exit(ERROR_NONE);
			return;
		}
		
		DoomyCommand command;
		try {
			command = DoomyCommand.getCommand(arguments);
		} catch (BadCommandException e) {
			System.err.println("ERROR: " + e.getMessage());
			System.exit(ERROR_BAD_COMMAND);
			return;
		}
		
		int retval;
		
		try {
			command.init(arguments);
			retval = command.call(System.out, System.err, System.in);
		} catch (BadArgumentException e) {
			System.err.println("ERROR: " + e.getMessage());
			System.exit(ERROR_BAD_ARGUMENT);
			return;
		}
		
		System.exit(retval);
	}

}
