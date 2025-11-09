/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy;

/**
 * Exception thrown on Doomy setup or initialization. Very serious.
 * @author Matthew Tropiano
 */
public class DoomySetupException extends RuntimeException 
{
	private static final long serialVersionUID = -439808919777179010L;

	public DoomySetupException(String message) 
	{
		super(message);
	}

	public DoomySetupException(String message, Throwable cause) 
	{
		super(message, cause);
	}
}
