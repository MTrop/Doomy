/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Doomy GUI pub-sub layer.
 * @author Matthew Tropiano
 */
public final class MessengerManager 
{
	public static final String CHANNEL_WADS_CHANGED = "wads.changed";
	public static final String CHANNEL_IWADS_CHANGED = "iwads.changed";
	public static final String CHANNEL_ENGINES_CHANGED = "engines.changed";
	public static final String CHANNEL_PRESETS_CHANGED = "presets.changed";
	
	/** Logger. */
	private static final Logger LOG = LoggerManager.getLogger(MessengerManager.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<MessengerManager> INSTANCE = new SingletonProvider<>(() -> new MessengerManager());

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static MessengerManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	/** Subscriber list map for broadcasting message object. */
	private Map<String, List<Consumer<Object>>> subscriberListMap;
	/** Broadcasting lock. */
	private ReentrantReadWriteLock.ReadLock readLock;
	/** Subscriber lock. */
	private ReentrantReadWriteLock.WriteLock writeLock;
	
	private MessengerManager()
	{
		this.subscriberListMap = new HashMap<>();
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.readLock = lock.readLock();
		this.writeLock = lock.writeLock();
	}

	/**
	 * Subscribes a consumer to a channel.
	 * @param channel the channel name.
	 * @param listener the listener to add.
	 */
	public void subscribe(String channel, Consumer<Object> listener)
	{
		try {
			writeLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) == null)
				subscriberListMap.put(channel, list = new LinkedList<>());
			list.add(listener);
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Unsubscribes a consumer from a channel.
	 * @param channel the channel name.
	 * @param listener the listener to remove.
	 */
	public void unsubscribe(String channel, Consumer<Object> listener)
	{
		try {
			writeLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) != null)
			{
				list.remove(listener);
				if (list.isEmpty())
					subscriberListMap.remove(channel);
			}
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Publishes a message to a channel.
	 * @param channel the target channel.
	 * @param message the message to publish.
	 */
	public void publish(String channel, Object message)
	{
		try {
			readLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) != null)
			{
				for (Consumer<Object> consumer : list)
				{
					try {
						consumer.accept(message);
					} catch (Throwable t) {
						LOG.error(t, "A message receiver threw an exception.");
					}
				}
			}
		} finally {
			readLock.unlock();
		}
	}
	
}
