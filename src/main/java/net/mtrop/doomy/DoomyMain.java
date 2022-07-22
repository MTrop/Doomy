package net.mtrop.doomy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import net.mtrop.doomy.DoomyCommand.BadArgumentException;
import net.mtrop.doomy.DoomyCommand.BadCommandException;
import net.mtrop.doomy.managers.DatabaseManager;

/**
 * Main class for Doomy.
 * @author Matthew Tropiano
 */
public final class DoomyMain
{
	/** Doomy Version */
	public static final String VERSION = DoomyCommon.getVersionString("doomy");
	
	/** Start REPL in script mode. */
	public static final String SWITCH_SCRIPT = "--script";

	/** Exit command. */
	public static final String COMMAND_EXIT = "exit";

	private static int executeCommand(Deque<String> arguments, PrintStream out, PrintStream err, BufferedReader in)
	{
		DoomyCommand command;
		try {
			command = DoomyCommand.getCommand(arguments);
		} catch (BadCommandException e) {
			err.println("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_COMMAND;
		}
		
		int retval;
		
		try {
			command.init(arguments);
			retval = command.call(out, err, in);
		} catch (BadArgumentException e) {
			err.println("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_ARGUMENT;
		} catch (Exception e) {
			err.println("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_ARGUMENT;
		}
		
		return retval;
	}
	
	private static Deque<String> parseInput(String input)
	{
		Deque<String> out = new LinkedList<>();
		
		final int STATE_START = 0;
		final int STATE_TOKEN = 1;
		final int STATE_INDQUOTE = 2;
		final int STATE_INSQUOTE = 3;
		int state = STATE_START;
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);
			switch (state)
			{
				default:
					throw new RuntimeException("Bad state.");
				case STATE_START:
				{
					if (c == '\'')
					{
						state = STATE_INSQUOTE;
					}
					else if (c == '"')
					{
						state = STATE_INDQUOTE;
					}
					else if (!Character.isWhitespace(c))
					{
						state = STATE_TOKEN;
						sb.append(c);
					}
				}
				break;
				
				case STATE_TOKEN:
				{
					if (Character.isWhitespace(c))
					{
						state = STATE_START;
						out.add(sb.toString());
						sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
				break;
				
				case STATE_INSQUOTE:
				{
					if (c == '\'')
					{
						state = STATE_START;
						out.add(sb.toString());
						sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
				break;

				case STATE_INDQUOTE:
				{
					if (c == '"')
					{
						state = STATE_START;
						out.add(sb.toString());
						sb.delete(0, sb.length());
					}
					else
					{
						sb.append(c);
					}
				}
				break;
			}
		}
		
		if (sb.length() != 0)
			out.add(sb.toString());
		
		return out;
	}
	
	private static int doShellLoop(PrintStream out, PrintStream err, BufferedReader in)
	{
		String line;
		DoomyCommon.splash(out, VERSION);
		out.println("Type '" + COMMAND_EXIT + "' to exit.");
		int returnValue = DoomyCommand.ERROR_NONE;
		while ((line = DoomyCommon.prompt(out, in, "Doomy>")) != null)
		{
			if (COMMAND_EXIT.equalsIgnoreCase(line))
				break;
			if (!line.isEmpty())
				returnValue = executeCommand(parseInput(line), out, err, in);
		}
		return returnValue;
	}
	
	private static int doScriptLoop(PrintStream out, PrintStream err, BufferedReader in)
	{
		String line;
		int returnValue = DoomyCommand.ERROR_NONE;
		try {
			while ((line = in.readLine()) != null && returnValue == DoomyCommand.ERROR_NONE)
			{
				line = line.trim();
				if (COMMAND_EXIT.equalsIgnoreCase(line))
					break;
				if (!line.isEmpty())
					returnValue = executeCommand(parseInput(line), out, err, in);
			}
		} catch (IOException e) {
			returnValue = DoomyCommand.ERROR_IO_ERROR;
		}
		return returnValue;
	}
	
	private static int runShell(PrintStream out, PrintStream err, BufferedReader in)
	{
		// Pre-warm DB connection.
		DatabaseManager.get();
		return doShellLoop(out, err, in);
	}
	
	public static void main(String[] args) 
	{
		if (!DatabaseManager.databaseExists())
		{
			System.out.println("Preparing for first use...");
			System.out.println("Creating database...");
			DatabaseManager.get();
			System.out.println("Done.");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		int returnValue = DoomyCommand.ERROR_NONE;
		Deque<String> arguments = new LinkedList<String>(Arrays.asList(args));
		
		if (arguments.isEmpty())
		{
			returnValue = runShell(System.out, System.err, in);
		}
		else if (DoomyCommand.matchArgument(arguments, SWITCH_SCRIPT))
		{
			// Pre-warm DB connection.
			DatabaseManager.get();
			returnValue = doScriptLoop(System.out, System.err, in);
		}
		else
		{
			String arg = arguments.pollFirst();
			if (arg == null)
				returnValue = runShell(System.out, System.err, in);
			else
				returnValue = executeCommand(arguments, System.out, System.err, in);
		}
		
		System.exit(returnValue);
	}

}
