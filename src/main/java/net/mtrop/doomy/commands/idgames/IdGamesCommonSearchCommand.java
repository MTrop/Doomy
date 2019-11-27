package net.mtrop.doomy.commands.idgames;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileContent;

/**
 * A common implementation of all idGames search functions.
 * @author Matthew Tropiano
 */
public abstract class IdGamesCommonSearchCommand implements DoomyCommand
{
	/** Search query. */
	protected String query;
	/** Non-null means a limit was set. */
	protected Integer limit;
	/** Non-null means a result was pre-selected. If null, prompt user. */
	protected Integer resultNumber;
	/** If true, do download, if false, print text file. */
	protected boolean download;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// TODO: Finish.
	}

	/**
	 * Calls a search and  
	 * @param query
	 * @return
	 */
	public abstract IdGamesFileContent[] search(String query);
	
	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		// TODO: Finish.
		DoomyCommon.help(out, IDGAMES);
		return ERROR_NONE;
	}

}
