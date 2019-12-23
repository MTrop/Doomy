package net.mtrop.doomy.commands.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;

/**
 * A command that calls the engine setup EXE.
 * @author Matthew Tropiano
 */
public class EngineSetupCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of engine.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		EngineSettings settings = EngineConfigManager.get().getEngineSettings(name);
		
		if (settings == null)
		{
			err.println("ERROR: Engine '" + name + "' does not exist.");
			return ERROR_NOT_FOUND;
		}

		if (settings.setupFileName == null)
		{
			err.println("ERROR: Engine '" + name + "' has no setup executable specified (" + EngineConfigManager.SETTING_SETUPFILENAME + ").");
			return ERROR_NOT_FOUND;
		}

		if (settings.exePath == null)
		{
			err.println("ERROR: Engine '" + name + "' has no main executable specified (" + EngineConfigManager.SETTING_EXEPATH + "). This is used to find the setup exe.");
			return ERROR_NOT_FOUND;
		}

		String engineDir = (new File(settings.exePath)).getParent();
		
		File exe = new File(engineDir + File.separator + settings.setupFileName);

		if (!exe.exists())
		{
			err.println("ERROR: Setup EXE '" + exe.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}

		String workingDir = engineDir; 
		if (settings.workingDirectoryPath != null)
			workingDir = settings.workingDirectoryPath;

		File workingDirFile = new File(workingDir);
		
		if (!workingDirFile.exists())
		{
			err.println("ERROR: Working directory '" + workingDirFile.getPath() + "' not found.");
			return ERROR_NOT_FOUND;
		}

		Instance<Integer> process;
		
		try {
			process = TaskManager.get().spawn((new ProcessBuilder())
				.command(exe.getPath())
				.directory(workingDirFile)
				.start()
			);
		} catch (IOException e) {
			err.println("ERROR: Could not start '" + exe.getPath() + "': " + e.getMessage());
			return ERROR_BAD_EXE;
		}
		process.join();
		
		return ERROR_NONE;
	}

}
