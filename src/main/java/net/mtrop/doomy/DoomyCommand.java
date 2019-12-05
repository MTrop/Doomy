package net.mtrop.doomy;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.commands.ConfigCommand;
import net.mtrop.doomy.commands.EngineCommand;
import net.mtrop.doomy.commands.HelpCommand;
import net.mtrop.doomy.commands.IWADCommand;
import net.mtrop.doomy.commands.IdGamesCommand;
import net.mtrop.doomy.commands.PresetCommand;
import net.mtrop.doomy.commands.UsageCommand;
import net.mtrop.doomy.commands.VersionCommand;
import net.mtrop.doomy.commands.WADCommand;
import net.mtrop.doomy.commands.config.ConfigGetCommand;
import net.mtrop.doomy.commands.config.ConfigListCommand;
import net.mtrop.doomy.commands.config.ConfigRemoveCommand;
import net.mtrop.doomy.commands.config.ConfigSetCommand;
import net.mtrop.doomy.commands.engine.EngineAddCommand;
import net.mtrop.doomy.commands.engine.EngineConfigCommand;
import net.mtrop.doomy.commands.engine.EngineCopyCommand;
import net.mtrop.doomy.commands.engine.EngineListCommand;
import net.mtrop.doomy.commands.engine.EngineRemoveCommand;
import net.mtrop.doomy.commands.engine.EngineRenameCommand;
import net.mtrop.doomy.commands.engine.EngineSetupCommand;
import net.mtrop.doomy.commands.engine.EngineTemplateCommand;
import net.mtrop.doomy.commands.engine.config.EngineConfigGetCommand;
import net.mtrop.doomy.commands.engine.config.EngineConfigListCommand;
import net.mtrop.doomy.commands.engine.config.EngineConfigRemoveCommand;
import net.mtrop.doomy.commands.engine.config.EngineConfigSetCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateAddCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateConfigCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateListCommand;
import net.mtrop.doomy.commands.engine.template.EngineTemplateRemoveCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigGetCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigListCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigRemoveCommand;
import net.mtrop.doomy.commands.engine.template.config.EngineTemplateConfigSetCommand;
import net.mtrop.doomy.commands.idgames.IdGamesAboutCommand;
import net.mtrop.doomy.commands.idgames.IdGamesComicCommand;
import net.mtrop.doomy.commands.idgames.IdGamesPingCommand;
import net.mtrop.doomy.commands.idgames.IdGamesSearchCommand;
import net.mtrop.doomy.commands.idgames.search.IdGamesSearchAuthorCommand;
import net.mtrop.doomy.commands.idgames.search.IdGamesSearchFileCommand;
import net.mtrop.doomy.commands.idgames.search.IdGamesSearchTextFileCommand;
import net.mtrop.doomy.commands.idgames.search.IdGamesSearchTitleCommand;
import net.mtrop.doomy.commands.iwad.IWADAddCommand;
import net.mtrop.doomy.commands.iwad.IWADCleanCommand;
import net.mtrop.doomy.commands.iwad.IWADGetCommand;
import net.mtrop.doomy.commands.iwad.IWADListCommand;
import net.mtrop.doomy.commands.iwad.IWADRemoveCommand;
import net.mtrop.doomy.commands.iwad.IWADRenameCommand;
import net.mtrop.doomy.commands.iwad.IWADScanCommand;
import net.mtrop.doomy.commands.iwad.IWADSetCommand;
import net.mtrop.doomy.commands.preset.PresetCreateCommand;
import net.mtrop.doomy.commands.preset.PresetListCommand;
import net.mtrop.doomy.commands.preset.PresetNameCommand;
import net.mtrop.doomy.commands.preset.PresetRemoveCommand;
import net.mtrop.doomy.commands.wad.WADAddCommand;
import net.mtrop.doomy.commands.wad.WADCleanCommand;
import net.mtrop.doomy.commands.wad.WADDependencyCommand;
import net.mtrop.doomy.commands.wad.WADGetCommand;
import net.mtrop.doomy.commands.wad.WADListCommand;
import net.mtrop.doomy.commands.wad.WADRedownloadCommand;
import net.mtrop.doomy.commands.wad.WADRemoveCommand;
import net.mtrop.doomy.commands.wad.WADRenameCommand;
import net.mtrop.doomy.commands.wad.WADScanCommand;
import net.mtrop.doomy.commands.wad.WADSetCommand;
import net.mtrop.doomy.commands.wad.WADSourceCommand;
import net.mtrop.doomy.commands.wad.WADTextCommand;
import net.mtrop.doomy.commands.wad.dependency.WADDependencyAddCommand;
import net.mtrop.doomy.commands.wad.dependency.WADDependencyClearCommand;
import net.mtrop.doomy.commands.wad.dependency.WADDependencyListCommand;
import net.mtrop.doomy.commands.wad.dependency.WADDependencyRemoveCommand;
import net.mtrop.doomy.commands.wad.source.WADSourceGetCommand;
import net.mtrop.doomy.commands.wad.source.WADSourceListCommand;
import net.mtrop.doomy.commands.wad.source.WADSourceRemoveCommand;
import net.mtrop.doomy.commands.wad.source.WADSourceSetCommand;

/**
 * Commands factory for Doomy.
 */
public interface DoomyCommand
{
	static final int ERROR_NONE = 			0;
	static final int ERROR_IO_ERROR = 		1;
	static final int ERROR_BAD_COMMAND = 	2;
	static final int ERROR_BAD_ARGUMENT = 	3;
	static final int ERROR_NOT_FOUND = 		4;
	static final int ERROR_NOT_ADDED = 		5;
	static final int ERROR_NOT_REMOVED = 	6;
	static final int ERROR_NOT_RENAMED = 	7;
	static final int ERROR_NOT_UPDATED = 	8;
	static final int ERROR_BAD_EXE = 		9;
	static final int ERROR_SOCKET_TIMEOUT = 10;
	static final int ERROR_SERVICE_ERROR =  11;

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
	static final String COPY = "copy";
	static final String REMOVE = "remove";
	static final String RENAME = "rename";
	static final String INFO = "info";
	static final String SETUP = "setup";
	static final String SCAN = "scan";
	static final String TEXT = "text";
	static final String CLEAN = "clean";
	static final String CLEAR = "clear";
	static final String CREATE = "create";
	static final String DOWNLOAD = "download";
	static final String REDOWNLOAD = "redownload";
	static final String SOURCE = "source";
	static final String DEPENDENCY1 = "dependency";
	static final String DEPENDENCY2 = "dep";
	static final String PING = "ping";
	static final String ABOUT = "about";
	static final String COMIC = "comic";
	static final String SEARCH = "search";
	static final String ID = "search";
	static final String FILE = "file";
	static final String TITLE = "title";
	static final String AUTHOR = "author";

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
	 * @return a command to run.
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
			if (matchArgument(args, LIST))
				return new EngineListCommand();
			else if (matchArgument(args, ADD))
				return new EngineAddCommand();
			else if (matchArgument(args, COPY))
				return new EngineCopyCommand();
			else if (matchArgument(args, REMOVE))
				return new EngineRemoveCommand();
			else if (matchArgument(args, RENAME))
				return new EngineRenameCommand();
			else if (matchArgument(args, SETUP))
				return new EngineSetupCommand();
			else if (matchArgument(args, CONFIG))
			{
				if (matchArgument(args, LIST))
					return new EngineConfigListCommand();
				else if (matchArgument(args, GET))
					return new EngineConfigGetCommand();
				else if (matchArgument(args, SET))
					return new EngineConfigSetCommand();
				else if (matchArgument(args, REMOVE))
					return new EngineConfigRemoveCommand();
				else
					return new EngineConfigCommand();
			}
			else if (matchArgument(args, TEMPLATE))
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
					return new EngineTemplateCommand();
			}
			else
				return new EngineCommand();
		}
		else if (matchArgument(args, IWAD))
		{
			if (matchArgument(args, LIST))
				return new IWADListCommand();
			else if (matchArgument(args, ADD))
				return new IWADAddCommand();
			else if (matchArgument(args, REMOVE))
				return new IWADRemoveCommand();
			else if (matchArgument(args, CLEAN))
				return new IWADCleanCommand();
			else if (matchArgument(args, RENAME))
				return new IWADRenameCommand();
			else if (matchArgument(args, GET))
				return new IWADGetCommand();
			else if (matchArgument(args, SET))
				return new IWADSetCommand();
			else if (matchArgument(args, SCAN))
				return new IWADScanCommand();
			else
				return new IWADCommand();
		}
		else if (matchArgument(args, WAD))
		{
			if (matchArgument(args, LIST))
				return new WADListCommand();
			else if (matchArgument(args, ADD))
				return new WADAddCommand();
			else if (matchArgument(args, REMOVE))
				return new WADRemoveCommand();
			else if (matchArgument(args, CLEAN))
				return new WADCleanCommand();
			else if (matchArgument(args, RENAME))
				return new WADRenameCommand();
			else if (matchArgument(args, GET))
				return new WADGetCommand();
			else if (matchArgument(args, SET))
				return new WADSetCommand();
			else if (matchArgument(args, SCAN))
				return new WADScanCommand();
			else if (matchArgument(args, TEXT))
				return new WADTextCommand();
			else if (matchArgument(args, REDOWNLOAD))
				return new WADRedownloadCommand();
			else if (matchArgument(args, SOURCE))
			{
				if (matchArgument(args, LIST))
					return new WADSourceListCommand();
				else if (matchArgument(args, GET))
					return new WADSourceGetCommand();
				else if (matchArgument(args, SET))
					return new WADSourceSetCommand();
				else if (matchArgument(args, REMOVE))
					return new WADSourceRemoveCommand();
				else
					return new WADSourceCommand();
			}
			else if (matchArgument(args, DEPENDENCY1) || matchArgument(args, DEPENDENCY2))
			{
				if (matchArgument(args, LIST))
					return new WADDependencyListCommand();
				else if (matchArgument(args, ADD))
					return new WADDependencyAddCommand();
				else if (matchArgument(args, REMOVE))
					return new WADDependencyRemoveCommand();
				else if (matchArgument(args, CLEAR))
					return new WADDependencyClearCommand();
				else
					return new WADDependencyCommand();
			}
			else
				return new WADCommand();
		}
		else if (matchArgument(args, PRESET))
		{
			if (matchArgument(args, LIST))
				return new PresetListCommand();
			else if (matchArgument(args, RENAME))
				return new PresetNameCommand();
			else if (matchArgument(args, CREATE))
				return new PresetCreateCommand();
			else if (matchArgument(args, REMOVE))
				return new PresetRemoveCommand();
			else
				return new PresetCommand();
		}
		else if (matchArgument(args, IDGAMES))
		{
			if (matchArgument(args, PING))
				return new IdGamesPingCommand();
			else if (matchArgument(args, ABOUT))
				return new IdGamesAboutCommand();
			else if (matchArgument(args, COMIC))
				return new IdGamesComicCommand();
			else if (matchArgument(args, SEARCH))
			{
				if (matchArgument(args, FILE))
					return new IdGamesSearchFileCommand();
				else if (matchArgument(args, TITLE))
					return new IdGamesSearchTitleCommand();
				else if (matchArgument(args, AUTHOR))
					return new IdGamesSearchAuthorCommand();
				else if (matchArgument(args, TEXT))
					return new IdGamesSearchTextFileCommand();
				else
					return new IdGamesSearchCommand();
			}
			else
				return new IdGamesCommand();
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
	int call(PrintStream out, PrintStream err, BufferedReader in) throws Exception;
	
}
