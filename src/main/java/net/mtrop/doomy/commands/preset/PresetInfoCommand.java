package net.mtrop.doomy.commands.preset;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommand.BadArgumentException;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;

/**
 * A command that [DOES NOTHING].
 * @author Matthew Tropiano
 */
public class PresetInfoCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name/hash of preset.");
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

		// TODO: Print info.
		
		return ERROR_NONE;
	}

}
