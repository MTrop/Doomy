package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommand.BadArgumentException;

/**
 * A command that creates a new preset.
 * @author Matthew Tropiano
 */
public class PresetCreateCommand implements DoomyCommand
{
	private static final String[] NO_WADS = new String[0];

	private static final String SWITCH_WADS1 = "--wads";
	private static final String SWITCH_WADS2 = "-w";
	private static final String SWITCH_IWAD1 = "--iwads";
	private static final String SWITCH_IWAD2 = "-i";
	private static final String SWITCH_NAME1 = "--name";
	private static final String SWITCH_NAME2 = "-n";

	private String name;
	private String engine;
	private String iwad;
	private String[] wads;
	
	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		engine = args.pollFirst();
		if (engine == null)
			throw new BadArgumentException("Expected engine name.");

		name = null;
		iwad = null;
		wads = NO_WADS;
		
		List<String> wadlist = new LinkedList<>();
		
		final int STATE_START = 0;
		final int STATE_IWAD = 1;
		final int STATE_NAME = 2;
		final int STATE_WADS = 3;
		int state = STATE_START;
		while (!args.isEmpty())
		{
			switch (state)
			{
				case STATE_START:
				{
					if (matchArgument(args, SWITCH_IWAD1) || matchArgument(args, SWITCH_IWAD2))
						state = STATE_IWAD;
					else if (matchArgument(args, SWITCH_NAME1) || matchArgument(args, SWITCH_NAME2))
						state = STATE_NAME;
					else if (matchArgument(args, SWITCH_WADS1) || matchArgument(args, SWITCH_WADS2))
						state = STATE_WADS;
					else
						throw new BadArgumentException("Expected path to scan for WADs.");
				}
				break;

				case STATE_IWAD:
				{
					iwad = args.pollFirst();
					state = STATE_START;
				}
				break;

				case STATE_NAME:
				{
					name = args.pollFirst();
					state = STATE_START;
				}
				break;

				case STATE_WADS:
				{
					if (matchArgument(args, SWITCH_IWAD1) || matchArgument(args, SWITCH_IWAD2))
						state = STATE_IWAD;
					else if (matchArgument(args, SWITCH_NAME1) || matchArgument(args, SWITCH_NAME2))
						state = STATE_NAME;
					else if (matchArgument(args, SWITCH_WADS1) || matchArgument(args, SWITCH_WADS2))
						state = STATE_WADS;
					else
						wadlist.add(args.pollFirst());
				}
				break;
			}
		}
		
		if (state == STATE_IWAD)
			throw new BadArgumentException("Expected IWAD name after switch.");
		if (state == STATE_NAME)
			throw new BadArgumentException("Expected preset name after switch.");
		
		wads = new String[wadlist.size()];
		wadlist.toArray(wads);
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		// TODO: Finish this.
		return ERROR_NONE;
	}

}
