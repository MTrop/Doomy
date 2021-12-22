package net.mtrop.doomy.commands.idgames.search;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.managers.DownloadManager;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileContent;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.ObjectUtils;

/**
 * A common implementation of all idGames search functions.
 * @author Matthew Tropiano
 */
public abstract class IdGamesCommonSearchCommand implements DoomyCommand
{
	protected static final int MAX_LIMIT = 20;
	
	private static final String SWITCH_NAME1 = "--name";
	private static final String SWITCH_NAME2 = "-n";
	private static final String SWITCH_LIMIT1 = "--limit";
	private static final String SWITCH_LIMIT2 = "-l";
	private static final String SWITCH_DOWNLOAD1 = "--download";
	private static final String SWITCH_DOWNLOAD2 = "-d";
	private static final String SWITCH_TEXT1 = "--text";
	private static final String SWITCH_TEXT2 = "-t";

	/** Search query. */
	private String query;
	/** Download name. If null, ask. */
	private String name;
	/** Non-null means a limit was set. */
	private Integer limit;
	/** Non-null means a result was pre-selected. If null, prompt user. */
	private Integer resultNumber;
	/** If true, do download, if false, print text file. */
	private Boolean download;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		query = args.pollFirst();
		if (query == null)
			throw new BadArgumentException("Expected query.");

		name = null;
		limit = null;
		resultNumber = null;
		download = null;
		
		final int STATE_START = 0;
		final int STATE_NAME = 1;
		final int STATE_LIMIT = 2;
		final int STATE_DOWNLOAD = 3;
		final int STATE_TEXT = 4;
		int state = STATE_START;
		while (!args.isEmpty())
		{
			switch (state)
			{
				case STATE_START:
				{
					if (matchArgument(args, SWITCH_NAME1) || matchArgument(args, SWITCH_NAME2))
						state = STATE_NAME;
					else if (matchArgument(args, SWITCH_LIMIT1) || matchArgument(args, SWITCH_LIMIT2))
						state = STATE_LIMIT;
					else if (matchArgument(args, SWITCH_DOWNLOAD1) || matchArgument(args, SWITCH_DOWNLOAD2))
						state = STATE_DOWNLOAD;
					else if (matchArgument(args, SWITCH_TEXT1) || matchArgument(args, SWITCH_TEXT2))
						state = STATE_TEXT;
					else
						throw new BadArgumentException("Invalid switch: " + args.peekFirst());
				}
				break;
				
				case STATE_NAME:
				{
					if ((name = args.pollFirst()) == null)
						throw new BadArgumentException("Expected name after name switch.");
					state = STATE_START;
				}
				break;
				
				case STATE_LIMIT:
				{
					try {
						limit = Integer.parseInt(args.pollFirst());
						state = STATE_START;
					} catch (NumberFormatException e) {
						throw new BadArgumentException("Expected number after limit switch.");
					}
				}
				break;
				
				case STATE_DOWNLOAD:
				{
					try {
						resultNumber = Integer.parseInt(args.pollFirst());
						download = true;
						state = STATE_START;
					} catch (NumberFormatException e) {
						throw new BadArgumentException("Expected number after download switch.");
					}
				}
				break;
				
				case STATE_TEXT:
				{
					try {
						limit = Integer.parseInt(args.pollFirst());
						download = false;
						state = STATE_START;
					} catch (NumberFormatException e) {
						throw new BadArgumentException("Expected number after text switch.");
					}
				}
				break;
			}
		}
		
		if (state == STATE_NAME)
			throw new BadArgumentException("Expected name after switch.");
		if (state == STATE_LIMIT)
			throw new BadArgumentException("Expected limit after switch.");
		if (state == STATE_DOWNLOAD)
			throw new BadArgumentException("Expected number after switch.");
		if (state == STATE_TEXT)
			throw new BadArgumentException("Expected number after switch.");

	}

	/**
	 * Calls a search and grabs the resulting file content.
	 * @param query the query to send to the idGames service.
	 * @param limit the result limit.
	 * @return a list of file results, or null on service error.
	 * @throws SocketTimeoutException if the request times out.
	 * @throws IOException if a read error occurs.
	 */
	public abstract IdGamesSearchResponse search(String query, int limit) throws SocketTimeoutException, IOException;
	
	/**
	 * Downloads a file.
	 * @param out the standard out stream.
	 * @param err the error out stream.
	 * @param in the buffered input stream reader to use.
	 * @param idGamesFileId the idGames file id.
	 * @return command return code.
	 */
	protected int download(final PrintStream out, final PrintStream err, final BufferedReader in, long idGamesFileId) 
	{
		IdGamesManager idgm = IdGamesManager.get();
		
		IdGamesFileResponse response;
		try {
			response = idgm.getById(idGamesFileId);
		} catch (SocketTimeoutException e) {
			err.println("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			err.println("ERROR: I/O error on call to idGames.");
			return ERROR_IO_ERROR;
		}
		
		if (response.error != null)
		{
			err.println("ERROR: Error from idGames: " + response.error);
			return ERROR_SERVICE_ERROR;
		}
		
		// fetch text file and print.
		if (!download)
		{
			out.println(response.content.textfile.replace("\t", "       "));
			return ERROR_NONE;
		}
		
		WADManager wadmgr = WADManager.get();

		if (name == null)
		{
			String basename = FileUtils.getFileNameWithoutExtension(response.content.filename);
			name = basename;
			
			int next = 1;
			while (wadmgr.containsWAD(name))
				name = basename + (next++);
			
			String input = DoomyCommon.prompt(out, in, "Add to WAD database as (press ENTER for \"" + name +"\"):");
			if (!input.isEmpty())
				name = input;
		}
		
		if (wadmgr.containsWAD(name))
		{
			err.println("ERROR: WAD entry '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}

		String uri = response.content.dir + response.content.filename;
		String downloadTarget = DoomyEnvironment.getDownloadDirectoryPath() + File.separator + uri.replace('/', File.separatorChar);
		File downloadTargetFile = new File(downloadTarget);
		String downloadTempTarget = downloadTarget + ".temp";
		
		if (downloadTargetFile.exists())
		{
			out.println("The target file, '" + downloadTarget + "', already exists.");
			if (!"y".equals(DoomyCommon.prompt(out, in, "Overwrite (Y/N)?")))
			{
				out.println("Aborted add.");
				return ERROR_NONE;
			}
		}
		
		out.println("Connecting to idGames Mirror (" + idgm.getMirrorURL() + ")...");

		final long refdate = System.currentTimeMillis();

		Instance<File> instance = idgm.download(uri, downloadTempTarget, DownloadManager.intervalListener(125L, (cur, len, pct) -> 
		{
			long timeMillis = System.currentTimeMillis() - refdate;
			long speed = timeMillis > 0L ? cur / timeMillis * 1000L / 1024 : 0;
			if (len < 0)
				out.printf("\rDownloading: %d (%d KB/s)...", cur, speed);
			else
				out.printf("\rDownloading: %-" + (int)(Math.log10(len) + 1.0) + "d of " + len + " (%3d%%, %d KB/s)...", cur, pct, speed);
		}));

		if (instance.getException() != null)
		{
			Throwable e = instance.getException();
			err.println("ERROR: File download: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return ERROR_IO_ERROR;
		}

		out.println("\nAdding to database as '" + name + "'...");

		File downloadedFile = instance.result();

		if (wadmgr.addWAD(name, downloadTarget, idgm.getMirrorURL() + uri) == null)
		{
			err.println("ERROR: Could not add WAD entry '" + name + "'.");
			downloadedFile.delete(); // cleanup
			return ERROR_NOT_ADDED;
		}

		if (downloadTargetFile.exists())
		{
			out.println("Removing old file...");
			if (!downloadTargetFile.delete())
			{
				err.println("ERROR: Could not delete old file.");
				downloadedFile.delete(); // cleanup
				wadmgr.removeWAD(name);
				return ERROR_NOT_ADDED;
			}
		}

		out.println("Finalizing download...");

		if (!downloadedFile.renameTo(downloadTargetFile))
		{
			err.println("ERROR: Could not move downloaded file.");
			wadmgr.removeWAD(name); // cleanup
			return ERROR_NOT_ADDED;
		}

		out.println("Done.");
		return ERROR_NONE;
	}

	@Override
	public int call(final PrintStream out, final PrintStream err, final BufferedReader in) throws Exception
	{
		int selectedLimit = Math.min(limit != null ? limit : MAX_LIMIT, MAX_LIMIT);
		IdGamesSearchResponse searchResponse = search(query, selectedLimit);

		if (searchResponse.error != null)
			throw new IOException("idGames returned error: " + searchResponse.error.type + ": " + searchResponse.error.message);

		if (searchResponse.warning != null)
		{
			out.println("idGames: " + searchResponse.warning.type + ": " + searchResponse.warning.message);
			if (searchResponse.content == null)
				return ERROR_NONE;
		}

		IdGamesFileContent[] results;
		if (searchResponse.content.files.length > selectedLimit)
			results = Arrays.copyOfRange(searchResponse.content.files, 0, selectedLimit);
		else
			results = searchResponse.content.files;

		if (results == null)
			return ERROR_IO_ERROR;
		
		if (resultNumber == null)
		{
			int titlelen = 1; 
			int pathlen = 1;
			
			for (int i = 0; i < results.length; i++)
			{
				titlelen = Math.max(ObjectUtils.isNull(results[i].title, "").length(), titlelen);
				pathlen = Math.max((results[i].dir + results[i].filename).length(), titlelen);
			}

			String format = "%2d | %-" + titlelen + "s | %-" + pathlen + "s\n";
			
			for (int i = 0; i < results.length; i++)
				out.printf(format, i + 1, ObjectUtils.isNull(results[i].title, ""), results[i].dir + results[i].filename);
			
			if (results.length > 1)
			{
				String input = DoomyCommon.prompt(out, in, "Select which result (1-" + results.length + ")?");
				if (input.isEmpty())
				{
					err.println("ERROR: No input.");
					return ERROR_BAD_ARGUMENT;
				}
				try {
					resultNumber = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					err.println("ERROR: Not a number: " + input);
					return ERROR_BAD_ARGUMENT;
				}
			}
			else
			{
				String input = DoomyCommon.prompt(out, in, "One result. Is this OK (Y/N)?");
				if (input != null && !input.substring(0, 1).equalsIgnoreCase("y"))
				{
					out.println("Aborted add.");
					return ERROR_NONE;
				}
				resultNumber = 1;
			}
		}
		
		if (resultNumber < 1 || resultNumber > results.length)
		{
			err.println("ERROR: Chosen result value out of range (1 - " + results.length + ").");
			return ERROR_BAD_ARGUMENT;
		}
		
		IdGamesFileContent selected = results[resultNumber - 1];

		if (download == null)
		{
			String input = DoomyCommon.prompt(out, in, "Download or Text (D/T)?");
			if (input == null || input.isEmpty())
			{
				err.println("ERROR: No input.");
				return ERROR_BAD_ARGUMENT;
			}
			
			input = input.substring(0, 1).toLowerCase();
			
			if ("d".equals(input))
				download = true;
			else if ("t".equals(input))
				download = false;
			else
			{
				err.println("ERROR: Not D or T.");
				return ERROR_BAD_ARGUMENT;
			}
		}

		return download(out, err, in, selected.id);
	}

}
