package net.mtrop.doomy.commands.wad;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.DownloadManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.AsyncFactory.Instance;

/**
 * A command that adds a new WAD.
 * @author Matthew Tropiano
 */
public class WADDownloadCommand implements DoomyCommand
{
	private String url;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		url = args.pollFirst();
		if (url == null)
			throw new BadArgumentException("Expected URL of new WAD to download from.");
		name = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, url, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param url the URL of the file.
	 * @param name the WAD name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String url, String name)
	{
		URL urlPath;
		try {
			urlPath = new URL(url);
		} catch (MalformedURLException e) {
			handler.errln("ERROR: Malformed URL.");
			return ERROR_BAD_ARGUMENT;
		}
		
		if (name == null)
			name = FileUtils.getFileNameWithoutExtension(urlPath.getPath());

		WADManager wadmgr = WADManager.get();

		if (wadmgr.containsWAD(name))
		{
			handler.errln("ERROR: WAD entry '" + name + "' already exists.");
			return ERROR_NOT_ADDED;
		}

		String downloadTarget = DoomyEnvironment.getDownloadDirectoryPath() + File.separator + urlPath.getPath().replace('/', File.separatorChar);
		File downloadTargetFile = new File(downloadTarget);
		String downloadTempTarget = downloadTarget + ".temp";
		
		if (downloadTargetFile.exists())
		{
			handler.outln("The target file, '" + downloadTarget + "', already exists.");
			if (!"y".equals(handler.prompt("Overwrite (Y/N)?")))
			{
				handler.outln("Aborted add.");
				return ERROR_NONE;
			}
		}
		
		handler.outln("Connecting (" + urlPath + ")...");

		final long refdate = System.currentTimeMillis();

		Instance<File> instance = DownloadManager.get().download(url, 5000, downloadTempTarget, DownloadManager.intervalListener(125L, (cur, len, pct) -> 
		{
			long timeMillis = System.currentTimeMillis() - refdate;
			long speed = timeMillis > 0L ? cur / timeMillis * 1000L / 1024 : 0;
			if (len < 0)
				handler.outf("\rDownloading: %d (%d KB/s)...", cur, speed);
			else
				handler.outf("\rDownloading: %-" + (int)(Math.log10(len) + 1.0) + "d of " + len + " (%3d%%, %d KB/s)...", cur, pct, speed);
		}));

		if (instance.getException() != null)
		{
			Throwable e = instance.getException();
			handler.errln("ERROR: File download: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return ERROR_IO_ERROR;
		}

		handler.outln("\nAdding to database as '" + name + "'...");

		File downloadedFile = instance.result();

		if (wadmgr.addWAD(name, downloadTarget, url) == null)
		{
			handler.errln("ERROR: Could not add WAD entry '" + name + "'.");
			downloadedFile.delete(); // cleanup
			return ERROR_NOT_ADDED;
		}

		if (downloadTargetFile.exists())
		{
			handler.outln("Removing old file...");
			if (!downloadTargetFile.delete())
			{
				handler.errln("ERROR: Could not delete old file.");
				downloadedFile.delete(); // cleanup
				wadmgr.removeWAD(name);
				return ERROR_NOT_ADDED;
			}
		}

		handler.outln("Finalizing download...");

		if (!downloadedFile.renameTo(downloadTargetFile))
		{
			handler.errln("ERROR: Could not move downloaded file.");
			wadmgr.removeWAD(name); // cleanup
			return ERROR_NOT_ADDED;
		}

		handler.outln("Done.");
		return ERROR_NONE;
	}

}
