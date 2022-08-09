package net.mtrop.doomy.managers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.AsyncFactory.Cancellable;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.struct.HTTPUtils.HTTPHeaders;
import net.mtrop.doomy.struct.HTTPUtils.HTTPRequest;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.HTTPUtils;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Download manager singleton.
 * @author Matthew Tropiano
 */
public final class DownloadManager
{
	// Singleton instance.
	private static final SingletonProvider<DownloadManager> INSTANCE = new SingletonProvider<>(() -> new DownloadManager());

	private static HTTPHeaders HEADERS = HTTPUtils.headers()
		// Some services block Java's default agent - send a browser string.
		.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static DownloadManager get()
	{
		return INSTANCE.get();
	}

	/**
	 * Creates a FileDownloadListener that calls {@link FileDownloadListener#onProgress(long, long, long)}
	 * on a timed interval instead of each downloaded chunk.
	 * @param intervalMillis the interval in milliseconds (a value less than 0 is 0).
	 * @param listener the listener to call each interval.
	 * @return a new listener.
	 */
	public static FileDownloadListener intervalListener(long intervalMillis, FileDownloadListener listener)
	{
		return new TimeIntervalProgressListener(intervalMillis, listener);
	}
	
	// =======================================================================
	
	/** Task manager. */
	private TaskManager taskManager;

	private DownloadManager()
	{
		this.taskManager = TaskManager.get();
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
		if (url.startsWith("http://") || url.startsWith("https://"))
			return taskManager.spawn(new HTTPFileDownloadTask(url, timeoutMillis, targetFile, listener));
		else
			return taskManager.spawn(new URLFileDownloadTask(url, timeoutMillis, targetFile, listener));
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
	
	private static class URLFileDownloadTask extends Cancellable<File>
	{
		private String url;
		private int timeoutMillis;
		private String targetFile;
		private FileDownloadListener listener;

		private URLFileDownloadTask(String url, int timeoutMillis, String targetFile, FileDownloadListener listener)
		{
			this.url = url;
			this.timeoutMillis = timeoutMillis;
			this.targetFile = targetFile;
			this.listener = listener;
		}

		@Override
		public File call() 
		{
			URLConnection conn;
			File target;
			
			try {
				target = new File(targetFile);
				if (!FileUtils.createPathForFile(target))
					return null;
				
				conn = (new URL(url)).openConnection();
				conn.setReadTimeout(timeoutMillis);

				long len = conn.getContentLength();
				InputStream in = new BufferedInputStream(conn.getInputStream());
				byte[] buffer = new byte[8192];
				
				try (FileOutputStream fos = new FileOutputStream(target))
				{
					if (len > 0)
					{
						int buf = 0;
						long cur = 0;
						while (!isCancelled() && (buf = in.read(buffer)) > 0)
						{
							fos.write(buffer, 0, buf);
							cur += buf;
							listener.onProgress(cur, len, len > 0 ? cur * 100 / len : -1);
						}
					}
				}
				finally
				{
					if (isCancelled())
						target.delete();
				}
				
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}

			return isCancelled() ? null : target;
		}
		
	}
	
	private static class HTTPFileDownloadTask extends Cancellable<File>
	{
		private String url;
		private int timeoutMillis;
		private String targetFile;
		private FileDownloadListener listener;

		private HTTPFileDownloadTask(String url, int timeoutMillis, String targetFile, FileDownloadListener listener)
		{
			this.url = url;
			this.timeoutMillis = timeoutMillis;
			this.targetFile = targetFile;
			this.listener = listener;
		}
		
		@Override
		public File call()
		{
			File out;
			try {
				out = HTTPRequest.get(url).setHeaders(HEADERS).timeout(timeoutMillis).send((response, cancelSwitch, monitor) ->
				{
					File target = new File(targetFile);
					if (!FileUtils.createPathForFile(target))
						return null;
					
					long len = response.getLength();
					
					listener.onProgress(0, len, len > 0 ? 0 / len : -1);
					
					byte[] buffer = new byte[8192];
					InputStream in = response.getContentStream();
					
					try (FileOutputStream fos = new FileOutputStream(target))
					{
						int buf = 0;
						long cur = 0;
						while (!isCancelled() && (buf = in.read(buffer)) > 0)
						{
							fos.write(buffer, 0, buf);
							cur += buf;
							listener.onProgress(cur, len, len > 0 ? cur * 100 / len : -1);
						}
					}
					finally
					{
						if (isCancelled())
							target.delete();
					}
					
					return isCancelled() ? null : target;
				});
				
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			
			return out;
		}
		
	}

	private static class TimeIntervalProgressListener implements FileDownloadListener
	{
		private long intervalMillis;
		private long nextTime;
		private FileDownloadListener listener;
		
		private TimeIntervalProgressListener(long intervalMillis, FileDownloadListener listener) 
		{
			this.nextTime = -1L;
			this.intervalMillis = Math.max(0, intervalMillis);
			this.listener = listener;
		}
		
		@Override
		public void onProgress(long current, long total, long percent) 
		{
			long now = System.currentTimeMillis();
			if (current == total)
			{
				listener.onProgress(current, total, percent);
			}
			else if (nextTime < 0L)
			{
				nextTime = now + intervalMillis;
				listener.onProgress(current, total, percent);
			}
			else if (now > nextTime)
			{
				nextTime = now + intervalMillis;
				listener.onProgress(current, total, percent);
			}
		}
	}
	
}
