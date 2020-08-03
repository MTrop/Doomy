package net.mtrop.doomy.commands.run;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.LauncherManager;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.LauncherManager.LaunchException;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that creates a new preset.
 * @author Matthew Tropiano
 */
public class RunCommand implements DoomyCommand
{
	private static final String[] NO_WADS = new String[0];

	private static final String SWITCH_WADS1 = "--wads";
	private static final String SWITCH_WADS2 = "-w";
	private static final String SWITCH_IWAD1 = "--iwad";
	private static final String SWITCH_IWAD2 = "-i";
	private static final String SWITCH_NAME1 = "--name";
	private static final String SWITCH_NAME2 = "-n";
	private static final String SWITCH_ARGS = "--";

	private String name;
	private String engine;
	private String iwad;
	private String[] wads;
	private String[] additionalArgs;
	private boolean skipCleanup;

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
		List<String> moreArgs = new LinkedList<>();
		
		final int STATE_START = 0;
		final int STATE_IWAD = 1;
		final int STATE_NAME = 2;
		final int STATE_WADS = 3;
		final int STATE_ARGS = 4;
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
					else if (matchArgument(args, SWITCH_ARGS))
						state = STATE_ARGS;
					else
						throw new BadArgumentException("Invalid switch: " + args.peekFirst());
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
					else if (matchArgument(args, SWITCH_ARGS))
						state = STATE_ARGS;
					else
						wadlist.add(args.pollFirst());
				}
				break;
				
				case STATE_ARGS:
				{
					moreArgs.add(args.pollFirst());
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
		additionalArgs = new String[moreArgs.size()];
		moreArgs.toArray(additionalArgs);
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		long engineId;
		Long iwadId;
		long[] wadIds;
		
		Engine e;
		if ((e = EngineManager.get().getEngine(engine)) == null)
		{
			err.println("ERROR: Engine '" + engine + "' not found.");
			return ERROR_NOT_FOUND;
		}
		else
		{
			engineId = e.id;
		}
		
		IWAD iw;
		if (iwad != null)
		{
			if ((iw = IWADManager.get().getIWAD(iwad)) == null)
			{
				err.println("ERROR: IWAD '" + iwad + "' not found.");
				return ERROR_NOT_FOUND;
			}
			else
			{
				iwadId = iw.id;
			}
		}
		else
		{
			iwadId = null;
		}

		List<Long> wids = new LinkedList<>();
		for (String wn : wads)
		{
			WAD w;
			if ((w = WADManager.get().getWAD(wn)) == null)
			{
				err.println("ERROR: WAD '" + wn + "' not found.");
				return ERROR_NOT_FOUND;
			}
			else
			{
				wids.add(w.id);
			}
		}
		wadIds = new long[wids.size()];
		int x = 0;
		for (Long l : wids)
			wadIds[x++] = l;
		
		PresetManager presetManager = PresetManager.get();
		
		Preset preset;
		String hash = PresetManager.calculatePresetHash(engineId, iwadId, wadIds);
		
		// check if combo exists - if not, add.
		if (presetManager.countPreset(hash) == 1)
		{
			preset = presetManager.getPresetByHash(hash)[0];
		}
		else
		{
			if (name == null)
			{
				String base;
				if (wads != null && wads.length > 0)
					base = wads[wads.length - 1];
				else if (iwad != null)
					base = iwad;
				else
					base = engine;
				
				int next = 1;
				name = base;
				while (presetManager.containsPreset(name))
					name = base + (next++);
			}

			if (presetManager.getPresetByName(name) != null)
			{
				err.println("ERROR: Preset already exists called '" + name + "'.");
				return ERROR_NOT_ADDED;
			}
			
			Long pid;
			if ((pid = presetManager.addPreset(name, engineId, iwadId, wadIds)) == null)
			{
				err.println("ERROR: Could not add preset.");
				return ERROR_NOT_ADDED;
			}
			
			preset = presetManager.getPreset(pid);
			
			out.println("Created preset named '" + name + "'.");
		}
		
		try {
			return LauncherManager.run(out, err, preset, additionalArgs, skipCleanup);
		} catch (LaunchException ex) {
			err.println("ERROR: " + ex.getMessage());
			return ERROR_LAUNCH_ERROR;
		}
	}

}
