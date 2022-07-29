package net.mtrop.doomy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.mtrop.doomy.struct.IOUtils;

/**
 * An Input-Output handler for messages sent and read from commands.
 * @author Matthew Tropiano
 */
public interface IOHandler extends AutoCloseable
{
	/**
	 * Prints a standard out message.
	 * @param message the message to print.
	 */
	void out(Object message);
	
	/**
	 * Prints a standard out message with a newline.
	 * @param message the message to print.
	 */
	default void outln(Object message)
	{
		out(message);
		out('\n');
	}
	
	/**
	 * Prints a newline.
	 */
	default void outln()
	{
		out('\n');
	}
	
	/**
	 * Prints a formatted standard out message.
	 * @param message the formatting message.
	 * @param params the parameters.
	 */
	default void outf(String message, Object ... params)
	{
		out(String.format(message, params));
	}

	/**
	 * Prints a standard error message.
	 * @param message the message to print.
	 */
	void err(Object message);
	
	/**
	 * Prints a newline to standard error.
	 */
	default void errln()
	{
		err('\n');
	}
	
	/**
	 * Prints a standard error message with a newline.
	 * @param message the message to print.
	 */
	default void errln(Object message)
	{
		err(message);
		err('\n');
	}
	
	/**
	 * Prints a formatted standard error message.
	 * @param message the formatting message.
	 * @param params the parameters.
	 */
	default void errf(String message, Object ... params)
	{
		out(String.format(message, params));
	}

	/**
	 * Reads a line from input.
	 * @return the line read, or null on error or end.
	 */
	String readLine();
	
	/**
	 * Prompts the user for input.
	 * @param prompt the prompt to display.
	 * @return the string read (and trimmed).
	 */
	String prompt(String prompt);

	/**
	 * Sends an input stream to out.
	 * @param in the input stream to read from.
	 * @throws IOException 
	 */
	void relay(InputStream in) throws IOException;

	static IOHandler stdio()
	{
		return new IOHandler()
		{
			private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			
			@Override
			public void out(Object message) 
			{
				System.out.print(message);
			}

			@Override
			public void err(Object message) 
			{
				System.err.print(message);
			}

			@Override
			public String prompt(String prompt) 
			{
				try {
					out(prompt + " ");
					String line = in.readLine();
					return line != null ? line.trim() : null;
				} catch (IOException e) {
					return null;
				}
			}
			
			@Override
			public String readLine() 
			{
				try {
					return in.readLine();
				} catch (IOException e) {
					return null;
				}
			}
			
			@Override
			public void close() throws Exception 
			{
				// Do nothing.
			}

			@Override
			public void relay(InputStream in) throws IOException
			{
				IOUtils.relay(in, System.out, 8192);
			}

		};
	}
	
	static IOHandler stdscript(File scriptFile) throws IOException
	{
		@SuppressWarnings("resource") // closed in IOHandler#close()
		final InputStream fileIn = new FileInputStream(scriptFile);
		
 		return new IOHandler()
		{
			private BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));
			
			@Override
			public void out(Object message) 
			{
				System.out.print(message);
			}

			@Override
			public void err(Object message) 
			{
				System.err.print(message);
			}

			@Override
			public String prompt(String prompt) 
			{
				try {
					out(prompt + " ");
					String line = in.readLine();
					return line != null ? line.trim() : null;
				} catch (IOException e) {
					return null;
				}
			}
			
			@Override
			public String readLine() 
			{
				try {
					return in.readLine();
				} catch (IOException e) {
					return null;
				}
			}
			
			@Override
			public void relay(InputStream in) throws IOException
			{
				IOUtils.relay(in, System.out, 8192);
			}

			@Override
			public void close() throws Exception 
			{
				in.close();
			}

		};
	}
	
}
