package net.mtrop.doomy.commands.preset;

import static net.mtrop.doomy.DoomyCommand.matchArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.FileUtils;

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
	public static int run(PrintStream out, PrintStream err, BufferedReader in, Preset preset, String[] extraArgs)
	{
		EngineManager engineManager = EngineManager.get();
		EngineConfigManager engineSettingsManager = EngineConfigManager.get();
		IWADManager iwadManager = IWADManager.get();
		WADManager wadManager = WADManager.get();
		
		Engine engine = engineManager.getEngine(preset.engineId);
		EngineSettings settings = engineSettingsManager.getEngineSettings(preset.engineId);

		if (engine == null)
			throw new RuntimeException("Internal ERROR: Engine id " + preset.engineId + " does not exist!");

		// ============ Make sure some necessary setting exist.

		if (settings.exePath == null)
		{
			err.println("ERROR: Engine '" + engine.name + "' has no main executable specified (" + EngineConfigManager.SETTING_EXEPATH + ").");
			return ERROR_NOT_FOUND;
		}

		if (settings.fileSwitch == null)
		{
			err.println("ERROR: Engine '" + engine.name + "' has no file switch specified (" + EngineConfigManager.SETTING_FILESWITCH + ").");
			return ERROR_NOT_FOUND;
		}

		// ============ Resolve Engine Executables

		File dosboxExecutable = null;
		File engineExecutable = null;
		File workingDirectory = null;
		
		if (settings.dosboxPath != null)
		{
			dosboxExecutable = new File(settings.dosboxPath);
			if (!dosboxExecutable.exists())
			{
				err.println("ERROR: DOSBox executable '" + dosboxExecutable.getPath() + "' cannot be found.");
				return ERROR_NOT_FOUND;
			}
		}

		engineExecutable = new File(settings.exePath);
		if (!engineExecutable.exists())
		{
			err.println("ERROR: Engine executable '" + engineExecutable.getPath() + "' cannot be found.");
			return ERROR_NOT_FOUND;
		}

		if (settings.workingDirectoryPath != null)
			workingDirectory = new File(settings.workingDirectoryPath);
		else
			workingDirectory = engineExecutable.getParentFile();

		if (!workingDirectory.exists())
		{
			err.println("ERROR: Working directory '" + workingDirectory.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}

		// ============ Resolve IWAD
		
		IWAD iwad = null;
		
		if (settings.iwadSwitch != null)
		{
			if (preset.iwadId == null)
			{
				err.println("ERROR: Engine '" + engine.name + "' specifies an IWAD switch, but no IWAD was provided.");
				return ERROR_NOT_FOUND;
			}
			else
			{
				iwad = iwadManager.getIWAD(preset.iwadId);
				if (iwad == null)
					throw new RuntimeException("Internal ERROR: IWAD id " + preset.iwadId + " does not exist!");
			}
		}
		
		// ============ Prepare preset directory.

		File presetDirectory = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
		
		if (!presetDirectory.isDirectory())
		{
			err.println("ERROR: Preset directory '" + presetDirectory.getPath() + "' is not a directory!");
			return ERROR_NOT_FOUND;
		}
		
		if (!FileUtils.createPath(presetDirectory.getAbsolutePath()))
		{
			err.println("ERROR: Could not create preset directory '" + presetDirectory.getPath() + "'.");
			return ERROR_IO_ERROR;
		}
		
		// ============ Prepare temp directory.

		File tempDirectory = new File(DoomyEnvironment.getTempDirectoryPath());
		
		if (!tempDirectory.isDirectory())
		{
			err.println("ERROR: Temp directory '" + tempDirectory.getPath() + "' is not a directory!");
			return ERROR_NOT_FOUND;
		}
		
		if (!FileUtils.createPath(tempDirectory.getAbsolutePath()))
		{
			err.println("ERROR: Could not create temp directory '" + tempDirectory.getPath() + "'.");
			return ERROR_IO_ERROR;
		}
				
		// ============ Resolve WADs
		
		Deque<File> unzippedWADs = new LinkedList<File>();
		
		// Unzip each WAD/DEH/BEX 
		for (long id : preset.wadIds)
		{
			WAD wad = wadManager.getWAD(id);
			// TODO: Finish this.
		}
		
		// TODO: Finish this.
		
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

		return run(out, err, in, preset, additionalArgs);
	}

}
