package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.WADManager;

/**
 * A command that shows detailed info about a preset.
 * @author Matthew Tropiano
 */
public class PresetRunCommand implements DoomyCommand
{
	private static final String SWITCH_ARGS = "--";

	private String name;
	private String[] additionalArgs;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name/hash of preset to run.");
		
		final int STATE_START = 0;
		final int STATE_ARGS = 1;
		int state = STATE_START;
		if (!args.isEmpty())
		{
			if (matchArgument(args, SWITCH_ARGS))
				state = STATE_ARGS;
			else
				throw new BadArgumentException("Invalid switch: " + args.peekFirst());
		}
		
		if (state == STATE_ARGS)
		{
			additionalArgs = new String[args.size()];
			int i = 0;
			while (!args.isEmpty())
				additionalArgs[i++] = args.pollFirst();
		}
	}

	/**
	 * Runs a preset.
	 * @param preset the preset to run.
	 * @param extraArgs the extra literal args to pass.
	 * @return the return code from the program.
	 */
	public static int run(Preset preset, String[] extraArgs)
	{
		EngineConfigManager engineSettingsManager = EngineConfigManager.get();
		IWADManager iwadManager = IWADManager.get();
		WADManager wadManager = WADManager.get();
		
		EngineSettings engine = engineSettingsManager.getEngineSettings(preset.id);
		IWAD iwad = iwadManager.getIWAD(preset.iwadId);

		// TODO: Finish.
		
		return ERROR_NONE;
	}
	
	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		PresetManager mgr = PresetManager.get();
		
		Preset preset;
		int hashCount;
		
		if (mgr.containsPreset(name))
		{
			preset = mgr.getPresetByName(name);
		}
		else if ((hashCount = mgr.countPreset(name)) > 0)
		{
			if (hashCount == 1)
			{
				preset = mgr.getPresetByHash(name)[0];
			}
			else
			{
				err.println("ERROR: Hash '" + name + "' matches more than one preset.");
				return ERROR_NOT_FOUND;
			}
		}
		else
		{
			err.println("ERROR: '" + name + "' does not match a preset name nor hash.");
			return ERROR_NOT_FOUND;
		}

		return run(preset, additionalArgs);
	}

}
