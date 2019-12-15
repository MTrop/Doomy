package net.mtrop.doomy.commands.preset;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.PresetInfo;

/**
 * A command that shows detailed info about a preset.
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
		
		PresetInfo preset;
		int hashCount;
		
		if (mgr.containsPreset(name))
		{
			preset = mgr.getPresetInfoByName(name)[0];
		}
		else if ((hashCount = mgr.countPreset(name)) > 0)
		{
			if (hashCount == 1)
			{
				preset = mgr.getPresetInfoByHash(name)[0];
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

		out.println("Preset name:   " + preset.name);
		out.println("Preset hash:   " + preset.hash);
		out.println("Preset engine: " + preset.engineName);
		if (preset.iwadName != null)
			out.println("Preset IWAD:   " + preset.iwadName);
		if (preset.wads.length > 0)
		{
			String wadliststr = Arrays.toString(preset.wads);
			out.println("Preset WADs:   " + wadliststr.substring(1, wadliststr.length() - 1));
		}
		
		File presetDir = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
		
		out.println("    Directory: " + presetDir.getAbsolutePath());
		
		if (presetDir.exists()) for (File f : presetDir.listFiles())
		{
			out.println("        " + f.getPath() + ", " + f.length() + " bytes");
		}
		
		return ERROR_NONE;
	}

}
