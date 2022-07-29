package net.mtrop.doomy.commands.preset;

import java.io.File;
import java.util.Arrays;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
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
				handler.errln("ERROR: Hash '" + name + "' matches more than one preset.");
				return ERROR_NOT_FOUND;
			}
		}
		else
		{
			handler.errln("ERROR: '" + name + "' does not match a preset name nor hash.");
			return ERROR_NOT_FOUND;
		}

		handler.outln("Preset name:   " + preset.name);
		handler.outln("Preset hash:   " + preset.hash);
		handler.outln("Preset engine: " + preset.engineName);
		if (preset.iwadName != null)
			handler.outln("Preset IWAD:   " + preset.iwadName);
		if (preset.wads.length > 0)
		{
			String wadliststr = Arrays.toString(preset.wads);
			handler.outln("Preset WADs:   " + wadliststr.substring(1, wadliststr.length() - 1));
		}
		
		File presetDir = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
		
		handler.outln("    Directory: " + presetDir.getAbsolutePath());
		
		if (presetDir.exists()) for (File f : presetDir.listFiles())
		{
			handler.outln("        " + f.getPath() + ", " + f.length() + " bytes");
		}
		
		return ERROR_NONE;
	}

}
