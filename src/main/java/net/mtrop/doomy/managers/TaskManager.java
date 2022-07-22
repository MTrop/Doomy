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
