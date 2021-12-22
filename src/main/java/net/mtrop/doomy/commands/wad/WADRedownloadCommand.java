package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.DownloadManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.AsyncFactory.Instance;

/**
 * A command that adds a new WAD.
 * @author Matthew Tropiano
 */
public class WADRedownloadCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WADManager wadmgr = WADManager.get();

		WAD wad = wadmgr.getWAD(name);
		
		if (wad == null)
		{
			err.println("ERROR: WAD entry '" + name + "' not found.");
			return ERROR_NOT_FOUND;
		}

		if (wad.sourceUrl == null)
		{
			err.println("ERROR: WAD entry '" + name + "' does not have a remote address.");
			return ERROR_NOT_FOUND;
		}
		
		String downloadTarget = wad.path;
		File downloadTargetFile = new File(wad.path);
		String downloadTempTarget = downloadTarget + ".temp";
		
		out.println("Connecting (" + wad.sourceUrl + ")...");

		final long refdate = System.currentTimeMillis();

		Instance<File> instance = DownloadManager.get().download(wad.sourceUrl, 5000, downloadTempTarget, DownloadManager.intervalListener(125L, (cur, len, pct) -> 
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

		File downloadedFile = instance.result();

		out.println();

		if (downloadTargetFile.exists())
		{
			out.println("Removing old file...");
			if (!downloadTargetFile.delete())
			{
				err.println("ERROR: Could not delete old file.");
				downloadedFile.delete(); // cleanup
				return ERROR_NOT_ADDED;
			}
		}

		out.println("Finalizing download...");

		if (!downloadedFile.renameTo(downloadTargetFile))
		{
			err.println("ERROR: Could not move downloaded file.");
			return ERROR_NOT_ADDED;
		}

		out.println("Done.");
		return ERROR_NONE;
	}

}
