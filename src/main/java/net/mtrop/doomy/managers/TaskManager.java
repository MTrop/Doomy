package net.mtrop.doomy.managers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.AsyncFactory;
import net.mtrop.doomy.struct.AsyncFactory.Cancellable;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.struct.AsyncFactory.Monitorable;

/**
 * Task manager singleton.
 * @author Matthew Tropiano
 */
public final class TaskManager
{
	// Singleton instance.
	private static TaskManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static TaskManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new TaskManager();
		return INSTANCE;
	}

	// =======================================================================
	
	/** Thread pool. */
	private AsyncFactory async;
	
	private TaskManager()
	{
		this.async = new AsyncFactory(1, 10, 2, TimeUnit.SECONDS);
	}

	/**
	 * Spawns a Process with the attached streams, returning its return value.
	 * <p>This will spawn a Runnable for each provided stream, which will each be responsible for piping data into the process and
	 * reading from it. The runnables terminate when the streams close. The streams also do not attach if the I/O is redirected
	 * ({@link Process#getInputStream()}, {@link Process#getErrorStream()}, or {@link Process#getOutputStream()} return <code>null</code>).
	 * <p>If the end of the provided input stream is reached or an error occurs, the pipe into the process is closed.
	 * @param process the process to monitor - it should already be started.
	 * @return the new instance.
	 */
	public Instance<Integer> spawn(Process process)
	{
		return async.spawn(process);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public Instance<Void> spawn(Runnable runnable)
	{
		return async.spawn(runnable);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param result the result to set on completion.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(T result, Runnable runnable)
	{
		return async.spawn(result, runnable);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(Callable<T> callable)
	{
		return async.spawn(callable);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Cancellable}.
	 * <p>Note: {@link Monitorable}s are also Cancellables.
	 * @param cancellable the cancellable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(Cancellable<T> cancellable)
	{
		return async.spawn(cancellable);
	}
	
}
