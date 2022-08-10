package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * A command that creates a new preset.
 * @author Matthew Tropiano
 */
public class PresetCreateCommand implements DoomyCommand
{
	private static final String[] NO_WADS = new String[0];

	private static final String SWITCH_WADS1 = "--wads";
	private static final String SWITCH_WADS2 = "-w";
	private static final String SWITCH_IWAD1 = "--iwad";
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
	public int call(IOHandler handler)
	{
		return execute(handler, name, engine, iwad, wads);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the preset name.
	 * @param engine the engine name.
	 * @param iwad the IWAD name.
	 * @param wads the names of the WADs to use, in order.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name, String engine, String iwad, String[] wads)
	{
		long engineId;
		Long iwadId;
		long[] wadIds;
		
		Engine e;
		if ((e = EngineManager.get().getEngine(engine)) == null)
		{
			handler.errln("ERROR: Engine '" + engine + "' not found.");
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
				handler.errln("ERROR: IWAD '" + iwad + "' not found.");
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
				handler.errln("ERROR: WAD '" + wn + "' not found.");
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
		
		String hash = PresetManager.calculatePresetHash(engineId, iwadId, wadIds);
		
		if (presetManager.getPresetByHash(hash).length > 0)
		{
			handler.errln("ERROR: Preset already exists for this combination of engine/IWAD/WADs.");
			return ERROR_NOT_ADDED;
		}
		
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
			handler.errln("ERROR: Preset already exists called '" + name + "'.");
			return ERROR_NOT_ADDED;
		}
		
		if (presetManager.addPreset(name, engineId, iwadId, wadIds) == null)
		{
			handler.errln("ERROR: Could not add preset.");
			return ERROR_NOT_ADDED;
		}
		
		handler.outln("Created preset named '" + name + "'.");
		return ERROR_NONE;
	}

}
