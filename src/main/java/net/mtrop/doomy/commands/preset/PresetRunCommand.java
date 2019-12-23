package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.LauncherManager;
import net.mtrop.doomy.managers.LauncherManager.LaunchException;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;

/**
 * A command that shows detailed info about a preset.
 * @author Matthew Tropiano
 */
public class PresetRunCommand implements DoomyCommand
{
	private static final String SWITCH_ARGS = "--";
	private static final String SWITCH_NOCLEANUP = "--no-cleanup";

	private String name;
	private String[] additionalArgs;
	private boolean skipCleanup;

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
		else
		{
			additionalArgs = new String[0];
		}
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

		try {
			return LauncherManager.run(out, err, preset, additionalArgs, skipCleanup);
		} catch (LaunchException e) {
			err.println("ERROR: " + e.getMessage());
			return ERROR_LAUNCH_ERROR;
		}
	}
	
}
