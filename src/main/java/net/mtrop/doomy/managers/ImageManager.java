/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.mtrop.doomy.struct.Loader;
import net.mtrop.doomy.struct.Loader.LoaderFuture;
import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Doomy GUI image loader singleton.
 * @author Matthew Tropiano
 */
public final class ImageManager 
{
	/** Logger. */
	private static final Logger LOG = LoggerManager.getLogger(ImageManager.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<ImageManager> INSTANCE = new SingletonProvider<>(() -> new ImageManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static ImageManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private Loader<BufferedImage> imageLoader;
	
	private ImageManager()
	{
		this.imageLoader = new Loader<>(Loader.createResourceLoader("gui/images/", (path, in) -> {
			try {
				BufferedImage out = ImageIO.read(in);
				LOG.debugf("Loaded image: %s", path);
				return out;
			} catch (IOException e) {
				throw new RuntimeException("Could not load expected resource: " + path);
			}
		}));
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the image to load.
	 * @return the image.
	 */
	public BufferedImage getImage(String name)
	{
		return imageLoader.getObject(name);
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the image to load.
	 * @return a future to get the image from.
	 */
	public LoaderFuture<BufferedImage> getImageAsync(String name)
	{
		return imageLoader.getObjectAsync(name);
	}
	
}
