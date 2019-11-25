package net.mtrop.doomy.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.AsyncFactory.Cancellable;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.HTTPUtils;

/**
 * Download manager singleton.
 * @author Matthew Tropiano
 */
public final class DownloadManager
{
	// Singleton instance.
	private static DownloadManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static DownloadManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new DownloadManager(TaskManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Task manager. */
	private TaskManager taskManager;

	private DownloadManager(TaskManager taskManager)
	{
		this.taskManager = taskManager;
	}
	
	/**
	 * Starts a file download and returns a reference to the running task.
	 * @param url the URL to download from.
	 * @param timeoutMillis the timeout in milliseconds.
	 * @param targetFile the target file to write.
	 * @param listener a listener interface to monitor download progress.
	 * @return a handle to the download task that returns the file written.
	 */
	public Instance<File> download(final String url, int timeoutMillis, final String targetFile, final FileDownloadListener listener)
	{
		return taskManager.spawn(new FileDownloadTask(url, timeoutMillis, targetFile, listener));
	}

	// =======================================================================

	/**
	 * File download listener.
	 */
	@FunctionalInterface
	public interface FileDownloadListener
	{
		/**
		 * Called when file download progress changes.
		 * @param current the current amount of bytes.
		 * @param total the total amount of bytes to download.
		 * @param percent the percent complete.
		 */
		void onProgress(long current, long total, long percent);
	}
	
	private static class FileDownloadTask extends Cancellable<File>
	{
		private String url;
		private int timeoutMillis;
		private String targetFile;
		private FileDownloadListener listener;

		private FileDownloadTask(String url, int timeoutMillis, String targetFile, FileDownloadListener listener)
		{
			this.url = url;
			this.timeoutMillis = timeoutMillis;
			this.targetFile = targetFile;
			this.listener = listener;
		}
		
		@Override
		public File call() throws Exception
		{
			return HTTPUtils.httpGet(url, timeoutMillis, response ->
			{
				File target = new File(targetFile);
				if (!FileUtils.createPathForFile(target))
					return null;
				
				int len = response.getLength();
				
				listener.onProgress(0, len, 0 / len);
				
				byte[] buffer = new byte[8192];
				InputStream in = response.getInputStream();
				
				try (FileOutputStream fos = new FileOutputStream(target))
				{
					int buf = 0;
					int cur = 0;
					while (!isCancelled() && (buf = in.read(buffer)) > 0)
					{
						fos.write(buffer, 0, buf);
						cur += buf;
						listener.onProgress(cur, len, cur * 100 / len);
					}
				}
				finally
				{
					if (isCancelled())
						target.delete();
				}
				
				listener.onProgress(len, len, 100);
				return isCancelled() ? null : target;
			});
		}
		
	}

}
