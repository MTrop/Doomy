/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;

import net.mtrop.doomy.struct.Loader;
import net.mtrop.doomy.struct.Loader.LoaderFuture;
import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.util.IOUtils;

/**
 * DoomTools GUI icon loader singleton.
 * @author Matthew Tropiano
 */
public final class IconManager 
{
	/** Logger. */
	private static final Logger LOG = LoggerManager.getLogger(IconManager.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<IconManager> INSTANCE = new SingletonProvider<>(() -> new IconManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static IconManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private Loader<ImageIcon> iconLoader;
	
	private IconManager()
	{
		this.iconLoader = new Loader<>(Loader.createResourceLoader("gui/images/", (path, in) -> {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.relay(in, bos);
				LOG.debugf("Loaded icon: %s", path);
				return new ImageIcon(bos.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException("Could not load expected resource: " + path);
			}
		}));
	}
	
	/**
	 * Gets an icon by name.
	 * @param name the name of the icon to load.
	 * @return the icon.
	 */
	public ImageIcon getImage(String name)
	{
		return iconLoader.getObject(name);
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the icon to load.
	 * @return a future to get the icon from.
	 */
	public LoaderFuture<ImageIcon> getImageAsync(String name)
	{
		return iconLoader.getObjectAsync(name);
	}
	
}
