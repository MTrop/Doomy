package net.mtrop.doomy.commands.preset;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.PresetManager;

/**
 * A command that names a preset.
 * @author Matthew Tropiano
 */
public class PresetNameCommand implements DoomyCommand
{
	private String hash;
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		hash = args.pollFirst();
		if (hash == null)
			throw new BadArgumentException("Expected preset hash.");
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected new name.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		PresetManager mgr = PresetManager.get();
		
		if (mgr.containsPreset(name))
		{
			err.println("ERROR: Preset '" + name + "' already exists. Choose a different name.");
			return ERROR_NOT_UPDATED;
		}
		
		if (mgr.countPreset(hash) < 1)
		{
			err.println("ERROR: No matching presets by hash '" + name + "'.");
			return ERROR_NOT_FOUND;
		}

		if (mgr.countPreset(hash) > 1)
		{
			err.println("ERROR: Too many preset hashes start with '" + name + "'.");
			return ERROR_NOT_FOUND;
		}
		
		if (mgr.setPresetName(hash, name))
		{
			err.println("ERROR: Preset could not be updated.");
			return ERROR_NOT_UPDATED;
		}
		
		out.println("Named preset '" + name + "'.");
		return ERROR_NONE;
	}

}
