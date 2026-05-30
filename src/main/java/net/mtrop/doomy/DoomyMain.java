/*******************************************************************************
 * Copyright (c) 2019-2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import net.mtrop.doomy.DoomyCommand.BadArgumentException;
import net.mtrop.doomy.DoomyCommand.BadCommandException;
import net.mtrop.doomy.gui.DoomyGUIMain;
import net.mtrop.doomy.managers.DatabaseManager;
import net.mtrop.doomy.struct.TokenScanner;

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
	/** Start in GUI mode. */
	public static final String SWITCH_GUI = "--gui";
	/** Update Doomy. */
	public static final String SWITCH_UPDATE = "--update";

	/** Exit command. */
	public static final String COMMAND_EXIT = "exit";

	private static int executeCommand(Deque<String> arguments, IOHandler handler)
	{
		DoomyCommand command;
		try {
			command = DoomyCommand.getCommand(arguments);
		} catch (BadCommandException e) {
			handler.errln("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_COMMAND;
		}
		
		int retval;
		
		try {
			command.init(arguments);
			retval = command.call(handler);
		} catch (BadArgumentException e) {
			handler.errln("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_ARGUMENT;
		} catch (Exception e) {
			handler.errln("ERROR: " + e.getMessage());
			return DoomyCommand.ERROR_BAD_ARGUMENT;
		}
		
		return retval;
	}
	
	/**
	 * Parses an input line into a Deque of tokens.
	 * @param input the input string.
	 * @return a Deque of String tokens.
	 */
	public static Deque<String> parseInput(String input)
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
	
	private static int doShellLoop(IOHandler handler)
	{
		String line;
		DoomyCommon.splash(handler, VERSION);
		handler.outln("Type '" + COMMAND_EXIT + "' to exit.");
		int returnValue = DoomyCommand.ERROR_NONE;
		while ((line = handler.prompt("Doomy>")) != null)
		{
			if (COMMAND_EXIT.equalsIgnoreCase(line))
				break;
			if (!line.isEmpty())
				returnValue = executeCommand(parseInput(line), handler);
		}
		return returnValue;
	}
	
	private static int doScriptLoop(IOHandler handler)
	{
		String line;
		int returnValue = DoomyCommand.ERROR_NONE;
		while ((line = handler.readLine()) != null && returnValue == DoomyCommand.ERROR_NONE)
		{
			line = line.trim();
			if (COMMAND_EXIT.equalsIgnoreCase(line))
				break;
			if (!line.isEmpty())
				returnValue = executeCommand(parseInput(line), handler);
		}
		return returnValue;
	}
	
	private static String getProgressBar(long current, Long max)
	{
		final int MAXPIPS = 40;
		if (max != null && max != 0)
		{
			StringBuilder sb = new StringBuilder("[");
			
			int i;
			long pips = current / (max / MAXPIPS);
			for (i = 0; i < pips; i++)
				sb.append('.');
			for (; i < MAXPIPS; i++)
				sb.append(' ');
			sb.append("] %d KB / %d KB");
			return String.format(sb.toString(), current / 1024, max / 1024);
		}
		else
		{
			return String.format(" ... %d KB", current / 1024);
		}
	}

	private static int doUpdate(IOHandler handler)
	{
		if (DoomyEnvironment.getDoomyPath() == null)
		{
			handler.errln("ERROR: Not running from Doomy (running from an IDE?).");
			return DoomyCommand.ERROR_BAD_COMMAND;
		}
		
		try {
			return (new DoomyUpdater(new File(DoomyEnvironment.getDoomyPath()), new DoomyUpdater.Listener()
			{
				@Override
				public boolean shouldContinue(String versionString)
				{
					return true;
				}
				
				@Override
				public void onUpdateSuccessful()
				{
					handler.outln("Update successful!");
				}
				
				@Override
				public void onUpdateAbort()
				{
					handler.outln("Update aborted!");
				}
				
				@Override
				public void onUpToDate()
				{
					handler.outln("Doomy is up-to-date.");
				}
				
				@Override
				public void onMessage(String message)
				{
					handler.outln(message);
				}
				
				@Override
				public void onError(String message)
				{
					handler.errln("ERROR: " + message);
				}
				
				@Override
				public void onDownloadTransfer(long current, Long max)
				{
					handler.out("\r" + getProgressBar(current, max));
				}
				
				@Override
				public void onDownloadStart() 
				{
					// Do nothing.
				}
				
				@Override
				public void onDownloadFinish()
				{
					handler.outln();
				}
				
			})).call();
		} catch (Exception e) {
			return DoomyCommand.ERROR_BAD_COMMAND;
		}
	}
	
	private static int runShell(IOHandler handler)
	{
		// Pre-warm DB connection.
		DatabaseManager.get();
		return doShellLoop(handler);
	}
	
	/**
	 * Main entry point.
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) 
	{
		IOHandler handler = IOHandler.stdio();
		
		if (!DatabaseManager.databaseExists())
		{
			handler.outln("Preparing for first use...");
			handler.outln("Creating database...");
			DatabaseManager.get();
			handler.outln("Done.");
		}

		int returnValue = DoomyCommand.ERROR_NONE;
		Deque<String> arguments = new LinkedList<String>(Arrays.asList(args));
		
		if (arguments.isEmpty())
		{
			returnValue = runShell(handler);
		}
		else if (DoomyCommand.matchArgument(arguments, SWITCH_UPDATE))
		{
			returnValue = doUpdate(handler);
		}
		else if (DoomyCommand.matchArgument(arguments, SWITCH_SCRIPT))
		{
			// Pre-warm DB connection.
			DatabaseManager.get();
			returnValue = doScriptLoop(handler);
		}
		else if (DoomyCommand.matchArgument(arguments, SWITCH_GUI))
		{
			// Pre-warm DB connection.
			DatabaseManager.get();
			try {
				DoomyCommon.spawnJava(DoomyGUIMain.class).exec();
				returnValue = DoomyCommand.ERROR_NONE;
			} catch (IOException e) {
				handler.outln("ERROR: Could not start GUI process: " + e.getLocalizedMessage());
				returnValue = DoomyCommand.ERROR_IO_ERROR;
			}
		}
		else
		{
			String arg = arguments.peekFirst();
			if (arg == null)
				returnValue = runShell(handler);
			else
				returnValue = executeCommand(arguments, handler);
		}
		
		System.exit(returnValue);
	}

	/**
	 * Executes a command line.
	 * @param commandLine the command line.
	 * @param handler the handler to use.
	 * @return the return value.
	 */
	public static int execute(String commandLine, IOHandler handler)
	{
		try (TokenScanner scanner = new TokenScanner(commandLine))
		{
			Deque<String> args = new LinkedList<>();
			while (scanner.hasNext())
				args.add(scanner.nextString());
			return executeCommand(args, handler);
		}
	}
	
}
