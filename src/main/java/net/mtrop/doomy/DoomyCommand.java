package net.mtrop.doomy;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.commands.ConfigCommand;
import net.mtrop.doomy.commands.EngineCommand;
import net.mtrop.doomy.commands.HelpCommand;
import net.mtrop.doomy.commands.UsageCommand;
import net.mtrop.doomy.commands.VersionCommand;
import net.mtrop.doomy.commands.config.ConfigGetCommand;
import net.mtrop.doomy.commands.config.ConfigListCommand;
import net.mtrop.doomy.commands.config.ConfigRemoveCommand;
import net.mtrop.doomy.commands.config.ConfigSetCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateAddCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateConfigCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateListCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateRemoveCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigGetCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigListCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigRemoveCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigSetCommand;

/**
 * Commands factory for Doomy.
 */
public interface DoomyCommand
{
	static final int ERROR_NONE = 			0;
	static final int ERROR_BAD_COMMAND = 	1;
	static final int ERROR_BAD_ARGUMENT = 	2;
	static final int ERROR_NOT_FOUND = 		3;
	static final int ERROR_NOT_ADDED = 		4;
	static final int ERROR_NOT_REMOVED = 	5;

	static final String VERSION = "version";
	static final String HELP = "help";
	static final String CONFIG = "config";
	static final String ENGINE = "engine";
	static final String TEMPLATE = "template";
	static final String IWAD = "iwad";
	static final String WAD = "wad";
	static final String PRESET = "preset";
	static final String RUN = "run";
	static final String IDGAMES = "idgames";
	static final String WADARCHIVE = "wadarchive";
	static final String LIST = "list";
	static final String GET = "get";
	static final String SET = "set";
	static final String ADD = "add";
	static final String REMOVE = "remove";
	static final String RENAME = "rename";

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
	 * Checks if the next argument matches the target, and if so, removes it.
	 * @param arguments the queue of arguments.
	 * @param target the target argument value.
	 * @return true on match, false if not.
	 */
	public static boolean matchArgument(Deque<String> arguments, String target)
	{
		if (!currentArgument(arguments, target))
			return false;
		
		arguments.pop();
		return true;
	}

	/**
	 * Checks if the next argument matches the target.
	 * @param arguments the queue of arguments.
	 * @param target the target argument value.
	 * @return true on match, false if not.
	 */
	public static boolean currentArgument(Deque<String> arguments, String target)
	{
		if (arguments.isEmpty())
			return false;
		if (!arguments.peek().equalsIgnoreCase(target))
			return false;
		
		return true;
	}

	/**
	 * Fetches the base command to initialize and execute.
	 * @param args the deque of remaining arguments.
	 * @return
	 */
	static DoomyCommand getCommand(Deque<String> args) throws BadCommandException
	{
		if (args.isEmpty())
			return new UsageCommand();
		else if (matchArgument(args, VERSION))
			return new VersionCommand();
		else if (matchArgument(args, HELP))
			return new HelpCommand();
		else if (matchArgument(args, CONFIG))
		{
			if (matchArgument(args, LIST))
				return new ConfigListCommand();
			else if (matchArgument(args, GET))
				return new ConfigGetCommand();
			else if (matchArgument(args, SET))
				return new ConfigSetCommand();
			else if (matchArgument(args, REMOVE))
				return new ConfigRemoveCommand();
			else
				return new ConfigCommand();
		}
		else if (matchArgument(args, ENGINE))
		{
			if (matchArgument(args, TEMPLATE))
			{
				if (matchArgument(args, LIST))
					return new EngineTemplateListCommand();
				else if (matchArgument(args, ADD))
					return new EngineTemplateAddCommand();
				else if (matchArgument(args, REMOVE))
					return new EngineTemplateRemoveCommand();
				else if (matchArgument(args, CONFIG))
				{
					if (matchArgument(args, LIST))
						return new EngineTemplateConfigListCommand();
					else if (matchArgument(args, GET))
						return new EngineTemplateConfigGetCommand();
					else if (matchArgument(args, SET))
						return new EngineTemplateConfigSetCommand();
					else if (matchArgument(args, REMOVE))
						return new EngineTemplateConfigRemoveCommand();
					else
						return new EngineTemplateConfigCommand();
				}
				else
					return new EngineCommand();
			}
			else
				return new EngineCommand();
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
	int call(PrintStream out, PrintStream err, InputStream in) throws Exception;
	
}
