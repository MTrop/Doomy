package net.mtrop.doomy.managers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.IOUtils;
import net.mtrop.doomy.struct.ProcessCallable;
import net.mtrop.doomy.struct.UnzipSet;
import net.mtrop.doomy.struct.AsyncFactory.Instance;

/**
 * Launcher manager.
 * @author Matthew Tropiano
 */
public final class LauncherManager
{
	private static final String DOSBOX_TEMP_MOUNT = "T:";
	private static final String DOSBOX_PRESET_MOUNT = "P:";
	private static final String CMDLINE_FILE = "CMDLINE.TXT";
	
	private static final UnzipSet.ZipEntryFilter WAD_ZIPFILTER = (entry) ->
	{
		String ext = FileUtils.getFileExtension(entry.getName(), ".");
		return ext.equalsIgnoreCase("wad")
			|| ext.equalsIgnoreCase("pk3")
			|| ext.equalsIgnoreCase("pke")
			|| ext.equalsIgnoreCase("pk7");
	};
	
	private static final UnzipSet.ZipEntryFilter DEH_ZIPFILTER = (entry) ->
	{
		String ext = FileUtils.getFileExtension(entry.getName(), ".");
		return ext.equalsIgnoreCase("deh")
			|| ext.equalsIgnoreCase("bex");
	};

	public static class LaunchException extends Exception
	{
		private static final long serialVersionUID = 429097110371212050L;
		LaunchException(String message)
		{
			super(message);
		}
	}

	private static class LaunchContext
	{
		private File dosboxExecutable;
		private File engineExecutable;
		private File workingDirectory;
	
		private File iwadFile;
		private Deque<File> wads;
		private Deque<File> dehs;
		private Deque<UnzipSet> zipSets;
		private Deque<File> cleanup;
		
		private File saveDir;
		private File screenshotDir;
	
		public LaunchContext()
		{
			this.dosboxExecutable = null;
			this.engineExecutable = null;
			this.workingDirectory = null;
			
			this.iwadFile = null;
			this.wads = new LinkedList<File>();
			this.dehs = new LinkedList<File>();
			this.zipSets = new LinkedList<UnzipSet>();
			this.cleanup = new LinkedList<File>();

			this.saveDir = null;
			this.screenshotDir = null;
		}
	}

	private static FileFilter createFileNamePatternFilter(final Pattern pattern)
	{
		return (file) -> pattern.matcher(file.getName()).matches();
	}
	
	private static File checkTempDirectory() throws LaunchException
	{
		File tempDirectory = new File(DoomyEnvironment.getTempDirectoryPath());
		
		if (!FileUtils.createPath(tempDirectory.getAbsolutePath()))
			throw new LaunchException("Could not create temp directory '" + tempDirectory.getPath() + "'.");

		if (!tempDirectory.isDirectory())
			throw new LaunchException("Temp directory '" + tempDirectory.getPath() + "' is not a directory!");
		
		return tempDirectory;
	}

	private static File checkPresetDirectory(Preset preset) throws LaunchException
	{
		File presetDirectory = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
		
		if (!FileUtils.createPath(presetDirectory.getAbsolutePath()))
			throw new LaunchException("Could not create preset directory '" + presetDirectory.getPath() + "'.");
		
		if (!presetDirectory.isDirectory())
			throw new LaunchException("Preset directory '" + presetDirectory.getPath() + "' is not a directory!");
		
		return presetDirectory;
	}

	private static void checkPresetValidity(Preset preset, Engine engine, EngineSettings settings) throws LaunchException
	{
		if (settings.exePath == null)
			throw new LaunchException("Engine '" + engine.name + "' has no main executable specified (" + EngineConfigManager.SETTING_EXEPATH + ").");
	
		if (settings.fileSwitch == null)
			throw new LaunchException("Engine '" + engine.name + "' has no file switch specified (" + EngineConfigManager.SETTING_FILESWITCH + ").");
	
		if (settings.iwadSwitch != null)
		{
			if (preset.iwadId == null)
				throw new LaunchException("Engine '" + engine.name + "' specifies an IWAD switch, but no IWAD was provided.");
			else if (IWADManager.get().getIWAD(preset.iwadId) == null)
				throw new LaunchException("Internal ERROR: IWAD id " + preset.iwadId + " does not exist!");
		}
	}

	private static void setExecutables(LaunchContext context, EngineSettings settings) throws LaunchException
	{
		if (settings.dosboxPath != null)
		{
			context.dosboxExecutable = new File(settings.dosboxPath);
			if (!context.dosboxExecutable.exists())
				throw new LaunchException("DOSBox executable '" + context.dosboxExecutable.getPath() + "' cannot be found.");
		}
	
		context.engineExecutable = new File(settings.exePath);
		if (!context.engineExecutable.exists())
			throw new LaunchException("Engine executable '" + context.engineExecutable.getPath() + "' cannot be found.");
	
		if (settings.workingDirectoryPath != null)
			context.workingDirectory = new File(settings.workingDirectoryPath);
		else
			context.workingDirectory = context.engineExecutable.getParentFile();
	
		if (!context.workingDirectory.exists())
			throw new LaunchException("Working directory '" + context.workingDirectory.getPath() + "' not found.");
	}

	private static void setDirectories(LaunchContext context, Preset preset, EngineSettings settings) throws LaunchException
	{
		// DOSBox
		if (context.dosboxExecutable != null)
		{
			if (settings.saveDirectorySwitch != null)
				context.saveDir = new File(DOSBOX_PRESET_MOUNT + "\\");
			if (settings.screenshotDirectorySwitch != null)
				context.screenshotDir = new File(DOSBOX_PRESET_MOUNT + "\\");
		}
		else
		{
			if (settings.saveDirectorySwitch != null)
				context.saveDir = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
			if (settings.screenshotDirectorySwitch != null)
				context.screenshotDir = new File(DoomyEnvironment.getPresetDirectoryPath(preset.hash));
		}
	}

	private static File tempFileCopy(IOHandler handler, File sourcePath, File tempDirectory, Deque<File> cleanupList, boolean dosBox) throws LaunchException
	{
		File outFile = new File(tempDirectory + File.separator + sourcePath.getName());
		handler.outln("Copying "+sourcePath.getName()+"...");
		try (FileInputStream fis = new FileInputStream(sourcePath); FileOutputStream fos = new FileOutputStream(outFile))
		{
			IOUtils.relay(fis, fos, 8192);
		} 
		catch (IOException e) 
		{
			throw new LaunchException("Could not copy remote file " + sourcePath.getPath() + " to " + outFile.getPath() + ".");
		}
		
		cleanupList.add(outFile);
		
		// if DOSBox, the file to add via a switch is in the mounted temp dir.
		if (dosBox)
			return new File(DOSBOX_TEMP_MOUNT + "\\" + sourcePath.getName());
		else
			return outFile;
	}

	private static List<File> copyMatchingFiles(File sourceDirectory, File targetDirectory, boolean cleanup, FileFilter filter) throws LaunchException
	{
		List<File> outList = new LinkedList<>();
		for (File matchingFile : sourceDirectory.listFiles(filter))
		{
			File outFile = new File(targetDirectory + File.separator + matchingFile.getName());
			try (FileInputStream fis = new FileInputStream(matchingFile); FileOutputStream fos = new FileOutputStream(outFile))
			{
				IOUtils.relay(fis, fos, 8192);
			} 
			catch (IOException e) 
			{
				throw new LaunchException("Could not copy remote file " + matchingFile.getPath() + " to " + outFile.getPath() + ".");
			}
			outList.add(outFile);
			
			if (cleanup)
				matchingFile.delete();
		}
		return outList;
	}

	private static void setUpWADs(IOHandler handler, LaunchContext context, Long iwadId, long[] wadIds, File tempDirectory) throws LaunchException
	{
		if (iwadId != null)
		{
			File iwadPath = new File(IWADManager.get().getIWAD(iwadId).path);
			// Check if we need to copy to temp - a remote file or a need to move to a mounted directory.
			if (iwadPath.isAbsolute() || context.dosboxExecutable != null)
				context.iwadFile = tempFileCopy(handler, iwadPath, tempDirectory, context.cleanup, context.dosboxExecutable != null);
			else
				context.iwadFile = iwadPath;
		}
		
		for (long id : wadIds)
		{
			WAD wad = WADManager.get().getWAD(id);
			File wadPath = new File(wad.path);
			String ext = FileUtils.getFileExtension(wadPath);
			
			if (ext.equalsIgnoreCase("wad") || ext.equalsIgnoreCase("pk3") || ext.equalsIgnoreCase("pke") || ext.equalsIgnoreCase("pk7"))
			{
				// Check if we need to copy to temp - a remote file or a need to move to a mounted directory.
				if (wadPath.isAbsolute() || context.dosboxExecutable != null)
					context.wads.add(tempFileCopy(handler, wadPath, tempDirectory, context.cleanup, context.dosboxExecutable != null));
				else
					context.wads.add(wadPath);
			}
			else if (ext.equalsIgnoreCase("deh") || ext.equalsIgnoreCase("bex"))
			{
				// Check if we need to copy to temp - a remote file or a need to move to a mounted directory.
				if (wadPath.isAbsolute() || context.dosboxExecutable != null)
					context.dehs.add(tempFileCopy(handler, wadPath, tempDirectory, context.cleanup, context.dosboxExecutable != null));
				else
					context.dehs.add(wadPath);
			}
			else if (ext.equalsIgnoreCase("zip"))
			{
				try 
				{
					UnzipSet uzs = new UnzipSet(wadPath, tempDirectory);
					List<File> unzipped; 
					
					handler.outln("Extract WADs...");
					unzipped = uzs.unzipAll(WAD_ZIPFILTER);
					for (File f : unzipped)
					{
						if (context.dosboxExecutable != null)
							context.wads.add(new File(DOSBOX_TEMP_MOUNT + "\\" + f.getName()));
						else
							context.wads.add(f);
					}
					
					handler.outln("Extract DEHs...");
					unzipped = uzs.unzipAll(DEH_ZIPFILTER);
					for (File f : unzipped)
					{
						if (context.dosboxExecutable != null)
							context.dehs.add(new File(DOSBOX_TEMP_MOUNT + "\\" + f.getName()));
						else
							context.dehs.add(f);
					}
					
					context.zipSets.add(uzs);
				} 
				catch (IOException e) 
				{
					throw new LaunchException("Could not open Zip file " + wadPath.getPath() + "!");
				}
			}
		}
	}

	private static String quoteEscape(String input)
	{
		return input.contains(" ") ? '"' + input + '"' : input;
	}
	
	private static File createCommandLineFile(LaunchContext context, EngineSettings settings, File tempDirectory, String[] extraArgs) throws LaunchException
	{
		File cmdlineFile = new File(tempDirectory + File.separator + CMDLINE_FILE);
		try (PrintWriter cmdWriter = new PrintWriter(cmdlineFile))
		{
			if (settings.iwadSwitch != null && context.iwadFile != null)
				cmdWriter.append(settings.iwadSwitch).append(' ').append(quoteEscape(context.iwadFile.getPath())).print(' ');
			
			if (!context.wads.isEmpty()) 
			{
				cmdWriter.append(settings.fileSwitch).print(' ');
				for (File w : context.wads)
					cmdWriter.append(quoteEscape(w.getPath())).print(' ');
			}
			
			if (settings.dehackedSwitch != null && !context.dehs.isEmpty())
				cmdWriter.append(settings.dehackedSwitch).append(' ').append(quoteEscape(context.dehs.getLast().getPath())).print(' ');
			else if (settings.dehlumpSwitch != null)
				cmdWriter.append(settings.dehlumpSwitch).print(' ');
	
			if (context.saveDir != null)
				cmdWriter.append(settings.saveDirectorySwitch).append(' ').append(quoteEscape(context.saveDir.getPath())).print(' ');
			if (context.screenshotDir != null)
				cmdWriter.append(settings.screenshotDirectorySwitch).append(' ').append(quoteEscape(context.screenshotDir.getPath())).print(' ');

			if (settings.commandLine != null)
				cmdWriter.append(settings.commandLine).print(' ');

			for (String arg : extraArgs)
				cmdWriter.append(quoteEscape(arg)).print(' ');
			
			cmdWriter.flush();
		} 
		catch (FileNotFoundException e) 
		{
			throw new LaunchException("Could not create file " + cmdlineFile.getPath() + "!");
		}
		
		if (context.dosboxExecutable != null)
			return cmdlineFile = new File(DOSBOX_TEMP_MOUNT + "\\" + CMDLINE_FILE);
		else
			return cmdlineFile;
	}

	/**
	 * Runs a preset.
	 * @param handler the handler to use.
	 * @param preset the preset to run.
	 * @param extraArgs the extra literal args to pass.
	 * @param skipCleanup if true, skip temp directory cleanup.
	 * @return the return code from the program.
	 * @throws LaunchException 
	 */
	public static int run(IOHandler handler, Preset preset, String[] extraArgs, boolean skipCleanup) throws LaunchException
	{
		EngineManager engineManager = EngineManager.get();
		EngineConfigManager engineSettingsManager = EngineConfigManager.get();
		
		Engine engine = engineManager.getEngine(preset.engineId);
		EngineSettings settings = engineSettingsManager.getEngineSettings(preset.engineId);
	
		if (engine == null)
			throw new LaunchException("Internal ERROR: Engine id " + preset.engineId + " does not exist!");
	
		File tempDirectory = checkTempDirectory();
		File presetDirectory = checkPresetDirectory(preset);
		checkPresetValidity(preset, engine, settings);

		LaunchContext context = new LaunchContext();
		setExecutables(context, settings);
		setDirectories(context, preset, settings);
		handler.outln("Prepare WADS...");
		setUpWADs(handler, context, preset.iwadId, preset.wadIds, tempDirectory);
		
		int retval;
		
		try {
			for (UnzipSet uzs : context.zipSets)
				IOUtils.close(uzs);

			File cmdlineFile = createCommandLineFile(context, settings, tempDirectory, extraArgs);
			context.cleanup.add(cmdlineFile);
			
			File engineDir = context.engineExecutable.getParentFile();
			
			// Pre-Launch (copy screenshots, demos)
			if (settings.saveDirectorySwitch == null && settings.saveGameRegex != null)
			{
				handler.outln("Copying save files...");
				copyMatchingFiles(presetDirectory, engineDir, false, createFileNamePatternFilter(settings.saveGameRegex));
			}
			if (settings.screenshotDirectorySwitch == null && settings.screenshotRegex != null)
			{
				handler.outln("Copying screenshot files...");
				copyMatchingFiles(presetDirectory, engineDir, false, createFileNamePatternFilter(settings.screenshotRegex));
			}
			if (settings.demoRegex != null)
			{
				handler.outln("Copying demo files...");
				copyMatchingFiles(presetDirectory, engineDir, false, createFileNamePatternFilter(settings.demoRegex));
			}
			
			// Launch.
			handler.outln("Launching...");
			Instance<Integer> process; 
			if (context.dosboxExecutable != null)
			{
				List<String> commandList = new LinkedList<>();
				commandList.add("-c");
				commandList.add("mount C: " + "'" + context.engineExecutable.getParentFile() + "'");
				commandList.add("-c");
				commandList.add("mount " + DOSBOX_PRESET_MOUNT + " " + "'" + presetDirectory.getPath() + "'");
				commandList.add("-c");
				commandList.add("mount " + DOSBOX_TEMP_MOUNT + " " + "'" + tempDirectory.getPath() + "'");
				commandList.add("-c");
				commandList.add("C:");
				commandList.add("-c");
				commandList.add(context.engineExecutable.getName() + " @" + cmdlineFile.getPath());
				commandList.add("-c");
				commandList.add("exit");
				if (settings.dosboxCommandLine != null) for (String s : settings.dosboxCommandLine.split("\\s+"))
					commandList.add(s);
				
				ProcessCallable callable = ProcessCallable.create(context.dosboxExecutable.getPath(), commandList.toArray(new String[commandList.size()]))
					.setWorkingDirectory(context.dosboxExecutable.getParentFile());
				process = TaskManager.get().spawn(callable);
			}
			else
			{
				ProcessCallable callable = ProcessCallable.create(context.engineExecutable.getPath(), "@" + cmdlineFile.getPath())
					.setWorkingDirectory(context.workingDirectory);
				process = TaskManager.get().spawn(callable);
			}
			
			retval = process.result();
		
			// Post-Launch (copy screenshots, demos)
			if (settings.saveDirectorySwitch == null && settings.saveGameRegex != null)
			{
				handler.outln("Copying save files...");
				copyMatchingFiles(engineDir, presetDirectory, true, createFileNamePatternFilter(settings.saveGameRegex));
			}
			if (settings.screenshotDirectorySwitch == null && settings.screenshotRegex != null)
			{
				handler.outln("Copying screenshot files...");
				copyMatchingFiles(engineDir, presetDirectory, true, createFileNamePatternFilter(settings.screenshotRegex));
			}
			if (settings.demoRegex != null)
			{
				handler.outln("Copying demo files...");
				copyMatchingFiles(engineDir, presetDirectory, true, createFileNamePatternFilter(settings.demoRegex));
			}
		}
		finally
		{
			// Temp Cleanup.
			if (!skipCleanup)
			{
				handler.outln("Cleaning up temp...");
				for (UnzipSet uzs : context.zipSets)
					uzs.cleanUp();
				for (File f : context.cleanup)
					f.delete();
			}
		}

		return retval;
	}

}
