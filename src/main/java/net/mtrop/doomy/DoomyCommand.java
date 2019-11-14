package net.mtrop.doomy;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.commands.ConfigCommand;
import net.mtrop.doomy.commands.HelpCommand;
import net.mtrop.doomy.commands.UsageCommand;
import net.mtrop.doomy.commands.VersionCommand;
import net.mtrop.doomy.util.Common;

/**
 * Commands factory for Doomy.
 */
public interface DoomyCommand
{
	static final int ERROR_NONE = 0;
	static final int ERROR_BAD_COMMAND = 1;
	static final int ERROR_BAD_ARGUMENT = 2;

	static final String VERSION = "version";
	static final String HELP = "help";
	static final String CONFIG = "config";
	static final String ENGINE = "engine";
	static final String IWAD = "iwad";
	static final String WAD = "wad";
	static final String PRESET = "preset";
	static final String RUN = "run";
	static final String IDGAMES = "idgames";
	static final String WADARCHIVE = "wadarchive";
	static final String LIST = "list";
	static final String GET = "get";
	static final String SET = "set";
	static final String EXPERT_MODE = "expert-mode";

	/**
	 * Thrown if a bad/unexpected argument is parsed on command initialize.
	 */
	static class BadArgumentException extends Exception
	{
		private static final long serialVersionUID = 5672090729067125267L;

		public BadArgumentException(String message)
		{
			super(message);
		}
	}
	
	/**
	 * Thrown if a bad/unexpected argument is parsed on command creation.
	 */
	static class BadCommandException extends Exception
	{
		private static final long serialVersionUID = 7807084009455096550L;

		public BadCommandException(String message)
		{
			super(message);
		}
	}
	
	/**
	 * Fetches the base command to initialize and execute.
	 * @param args the deque of remaining arguments.
	 * @return
	 */
	static DoomyCommand getCommand(Deque<String> args) throws BadCommandException
	{
		if (args.isEmpty())
		{
			return new UsageCommand();
		}
		else if (Common.matchArgument(args, VERSION))
		{
			return new VersionCommand();
		}
		else if (Common.matchArgument(args, HELP))
		{
			return new HelpCommand();
		}
		else if (Common.matchArgument(args, CONFIG))
		{
			if (Common.matchArgument(args, LIST))
			{
				// TODO: Finish this.
			}
			else if (Common.matchArgument(args, GET))
			{
				// TODO: Finish this.
			}
			else if (Common.matchArgument(args, SET))
			{
				// TODO: Finish this.
			}
			else if (Common.matchArgument(args, EXPERT_MODE))
			{
				// TODO: Finish this.
			}
			else
			{
				return new ConfigCommand();
			}
		}

		return new UsageCommand();
	}

	/**
	 * Initializes this command from remaining arguments. 
	 * @param args the deque of remaining arguments for parsing command types.
	 */
	void init(Deque<String> args) throws BadArgumentException;

	/**
	 * Processes this command.
	 * @param out the STDOUT stream.
	 * @param err the STDERR stream.
	 * @param in the STDIN stream.
	 * @return the return code from running the command.
	 */
	int call(PrintStream out, PrintStream err, InputStream in);
	
}
