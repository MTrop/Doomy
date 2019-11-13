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
