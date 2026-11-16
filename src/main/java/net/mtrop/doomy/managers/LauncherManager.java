/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
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

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.ExecutionPlanManager.ExecutionPlan;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.struct.ProcessCallable;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.UnzipSet;
import net.mtrop.doomy.struct.InstancedFuture;
import net.mtrop.doomy.struct.util.ArrayUtils;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.IOUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

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

	private static final LanguageManager language = LanguageManager.get();
	

	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<LauncherManager> INSTANCE = new SingletonProvider<>(() -> new LauncherManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static LauncherManager get()
	{
		return INSTANCE.get();
	}

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
		private IOHandler handler;
		private Preset preset;
		
		private File iwadFile;
		private Deque<File> wads;
		private Deque<File> dehs;
		private Deque<UnzipSet> zipSets;
		private Deque<File> cleanup;
		
		private File saveDir;
		private File screenshotDir;
	
		public LaunchContext()
		{
			this.handler = null;
			this.preset = null;
			
			this.iwadFile = null;
			this.wads = new LinkedList<File>();
			this.dehs = new LinkedList<File>();
			this.zipSets = new LinkedList<UnzipSet>();
			this.cleanup = new LinkedList<File>();

			this.saveDir = null;
			this.screenshotDir = null;
		}
	}

	@FunctionalInterface
	public interface LaunchFileFunction<T, U>
	{
		U apply(T t) throws LaunchException;
	}
	
	public interface PlanExecutable
	{
		void checkValidity(Engine engine, ExecutionPlan plan) throws LaunchException;
		void setupRuntimeFiles(LaunchContext context, Preset preset, File tempDirectory) throws LaunchException;
		ProcessCallable createSetupProcess(ExecutionPlan plan) throws LaunchException;
		ProcessCallable createClientProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException;
		ProcessCallable createServerProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException;
	}

	/**
	 * PlanTypes for execution.
	 * <p>
	 * <b> DO NOT CHANGE THE ORDER OF THESE ENUMS!</b>
	 * The ordinals are saved to the database! 
	 */
	public enum PlanType implements PlanExecutable
	{
		NORMAL
		{
			@Override
			public void checkValidity(Engine engine, ExecutionPlan plan) throws LaunchException
			{
				if (ObjectUtils.isEmpty(plan.workDir))
					throw new LaunchException(language.getText("engine.setup.noworkdir"));
				
				if (!new File(plan.workDir).exists())
					throw new LaunchException(language.getText("engine.setup.noexist"));
			}
			
			@Override
			public void setupRuntimeFiles(LaunchContext context, Preset preset, File tempDirectory) throws LaunchException
			{
				Long iwadId = preset.iwadId;
				long[] wadIds = preset.wadIds;
				setUpWADs(context, iwadId, wadIds, tempDirectory,
					(iwadFile) -> 
					{
						if (fileIsRemote(iwadFile))
						{
							context.handler.outln("Copying "+iwadFile.getName()+"...");
							return tempFileCopy(iwadFile, tempDirectory, context.cleanup, false);
						}
						else
						{
							return iwadFile;
						}
					},
					(wadFile) -> 
					{
						if (fileIsRemote(wadFile))
						{
							context.handler.outln("Copying "+wadFile.getName()+"...");
							return tempFileCopy(wadFile, tempDirectory, context.cleanup, false);
						}
						else
							return wadFile;
					},
					(unzippedFile) -> unzippedFile 
				);
			}

			@Override
			public ProcessCallable createSetupProcess(ExecutionPlan plan) throws LaunchException
			{
				if (ObjectUtils.isEmpty(plan.setupName))
					throw new LaunchException(language.getText("engine.setup.nosetup"));
				
				File path = new File(plan.workDir + File.separator + plan.setupName);
				ProcessCallable proc = ProcessCallable.create(path).setWorkingDirectory(new File(plan.workDir));
				
				return proc;
			}

			@Override
			public ProcessCallable createClientProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				if (ObjectUtils.isEmpty(settings.plan.clientName))
					throw new LaunchException("Engine has no main executable specified.");

				File tempDirectory = new File(DoomyEnvironment.getSystemTempPath());
				String[] cmdlineArray = createCommandLineArray(context, settings, tempDirectory, extraArgs);

				ProcessCallable callable = ProcessCallable.create(settings.plan.clientName);
				callable
					.args(cmdlineArray)
					.setWorkingDirectory(new File(settings.plan.workDir));
				
				return callable;
			}
	
			@Override
			public ProcessCallable createServerProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				File tempDirectory = new File(DoomyEnvironment.getSystemTempPath());
				String[] cmdlineArray = createCommandLineArray(context, settings, tempDirectory, extraArgs);

				File path = new File(settings.plan.workDir + File.separator + settings.plan.serverName);
				ProcessCallable callable = ProcessCallable.create(path.getPath());
				callable
					.args(cmdlineArray)
					.setWorkingDirectory(new File(settings.plan.workDir));
				
				return callable;
			}
		},
		
		DOSBOX
		{
			@Override
			public void checkValidity(Engine engine, ExecutionPlan plan) throws LaunchException
			{
				if (ObjectUtils.isEmpty(plan.packageName))
					throw new LaunchException(language.getText("engine.setup.nodosbox"));
			
				if (ObjectUtils.isEmpty(plan.workDir))
					throw new LaunchException(language.getText("engine.setup.noworkdir"));
				
				if (!new File(plan.workDir).exists())
					throw new LaunchException(language.getText("engine.setup.noexist"));
			}

			@Override
			public void setupRuntimeFiles(LaunchContext context, Preset preset, File tempDirectory) throws LaunchException
			{
				Long iwadId = preset.iwadId;
				long[] wadIds = preset.wadIds;
				setUpWADs(context, iwadId, wadIds, tempDirectory,
					(iwadFile) -> 
					{
						context.handler.outln("Copying "+iwadFile.getName()+"...");
						return tempFileCopy(iwadFile, tempDirectory, context.cleanup, true);
					},
					(wadFile) -> 
					{
						context.handler.outln("Copying "+wadFile.getName()+"...");
						return tempFileCopy(wadFile, tempDirectory, context.cleanup, true);
					},
					(unzippedFile) -> new File(DOSBOX_TEMP_MOUNT + "\\" + unzippedFile.getName())
				);
			}

			@Override
			public ProcessCallable createSetupProcess(ExecutionPlan plan) throws LaunchException
			{
				if (ObjectUtils.isEmpty(plan.setupName))
					throw new LaunchException(language.getText("engine.setup.nosetup"));

				File path = new File(plan.packageName);
				
				List<String> commandList = new LinkedList<>();
				commandList.add("-c");
					commandList.add("mount C: " + "'" + plan.workDir + "'");
				commandList.add("-c");
					commandList.add("C:");
				commandList.add("-c");
					commandList.add(plan.setupName);
				commandList.add("-c");
					commandList.add("exit");
				if (plan.packageCommandLine != null) for (String s : DoomyCommon.parseInput(plan.packageCommandLine))
					commandList.add(s);
				
				ProcessCallable proc = ProcessCallable.create(path.getPath())
					.setWorkingDirectory(path.getParentFile())
					.args(commandList.toArray(new String[commandList.size()]));
				
				return proc;
			}

			@Override
			public ProcessCallable createClientProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				File tempDirectory = new File(DoomyEnvironment.getSystemTempPath());
				File cmdlineFile = createCommandLineFile(context, settings, tempDirectory, extraArgs, true);
				context.cleanup.add(cmdlineFile);

				List<String> commandList = new LinkedList<>();
				commandList.add("-c");
					commandList.add("mount C: " + "'" + (new File(settings.plan.clientName)).getParentFile() + "'");
				commandList.add("-c");
					commandList.add("mount " + DOSBOX_PRESET_MOUNT + " " + "'" + checkPresetDirectory(context.preset).getPath() + "'");
				commandList.add("-c");
					commandList.add("mount " + DOSBOX_TEMP_MOUNT + " " + "'" + tempDirectory.getPath() + "'");
				commandList.add("-c");
					commandList.add("C:");
				commandList.add("-c");
					commandList.add(settings.plan.clientName + " @" + cmdlineFile.getPath());
				commandList.add("-c");
					commandList.add("exit");
				if (settings.plan.packageCommandLine != null)
				{
					Deque<String> cmdl = DoomyCommon.parseInput(settings.plan.packageCommandLine);
					while (!cmdl.isEmpty())
						commandList.add(cmdl.pollFirst());
				}
				
				ProcessCallable callable = ProcessCallable.create(settings.plan.packageName, commandList.toArray(new String[commandList.size()]))
					.setWorkingDirectory(new File(settings.plan.workDir));
				
				return callable;
			}
	
			@Override
			public ProcessCallable createServerProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				File tempDirectory = new File(DoomyEnvironment.getSystemTempPath());
				File cmdlineFile = createCommandLineFile(context, settings, tempDirectory, extraArgs, true);
				context.cleanup.add(cmdlineFile);

				List<String> commandList = new LinkedList<>();
				commandList.add("-c");
					commandList.add("mount C: " + "'" + (new File(settings.plan.clientName)).getParentFile() + "'");
				commandList.add("-c");
					commandList.add("mount " + DOSBOX_PRESET_MOUNT + " " + "'" + checkPresetDirectory(context.preset).getPath() + "'");
				commandList.add("-c");
					commandList.add("mount " + DOSBOX_TEMP_MOUNT + " " + "'" + tempDirectory.getPath() + "'");
				commandList.add("-c");
					commandList.add("C:");
				commandList.add("-c");
					commandList.add(settings.plan.serverName + " @" + cmdlineFile.getPath());
				commandList.add("-c");
					commandList.add("exit");
				if (settings.plan.packageCommandLine != null)
				{
					Deque<String> cmdl = DoomyCommon.parseInput(settings.plan.packageCommandLine);
					while (!cmdl.isEmpty())
						commandList.add(cmdl.pollFirst());
				}
				
				ProcessCallable callable = ProcessCallable.create(settings.plan.packageName, commandList.toArray(new String[commandList.size()]))
					.setWorkingDirectory(new File(settings.plan.workDir));
				
				return callable;
			}
		},
		
		FLATPAK
		{
			@Override
			public void checkValidity(Engine engine, ExecutionPlan plan) throws LaunchException
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void setupRuntimeFiles(LaunchContext context, Preset preset, File tempDirectory) throws LaunchException
			{
				// TODO Auto-generated method stub
			}

			@Override
			public ProcessCallable createSetupProcess(ExecutionPlan plan) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ProcessCallable createClientProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}
	
			@Override
			public ProcessCallable createServerProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}
		},
		
		APPIMAGE
		{
			@Override
			public void checkValidity(Engine engine, ExecutionPlan plan) throws LaunchException 
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void setupRuntimeFiles(LaunchContext context, Preset preset, File tempDirectory) throws LaunchException
			{
				// TODO Auto-generated method stub
			}

			@Override
			public ProcessCallable createSetupProcess(ExecutionPlan plan) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ProcessCallable createClientProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}
	
			@Override
			public ProcessCallable createServerProcess(LaunchContext context, EngineSettings settings, String[] extraArgs) throws LaunchException
			{
				// TODO Auto-generated method stub
				return null;
			}
		},
		;
	
	}

	// =======================================================================

	private LauncherManager() {}

	private FileFilter createFileNamePatternFilter(final Pattern pattern)
	{
		return (file) -> pattern.matcher(file.getName()).matches();
	}
	
	private static File checkTempDirectory() throws LaunchException
	{
		File tempDirectory = new File(DoomyEnvironment.getApplicationCachePath());
		
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

	private static File tempFileCopy(File sourcePath, File tempDirectory, Deque<File> cleanupList, boolean dosBox) throws LaunchException
	{
		File outFile = new File(tempDirectory + File.separator + sourcePath.getName());
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

	private static boolean fileIsRemote(File f)
	{
		String path = f.getPath();
		if (path.startsWith("//") || path.startsWith("\\\\")) // remote machine
			return true;
		return false;
	}
	
	private static void setUpWADs(LaunchContext context, Long iwadId, long[] wadIds, File tempDirectory, 
		LaunchFileFunction<File, File> iwadResolverFunction, 
		LaunchFileFunction<File, File> wadResolverFunction,
		LaunchFileFunction<File, File> unzippedFileDecoratorFunction
	) throws LaunchException
	{
		if (iwadId != null)
		{
			File iwadPath = new File(IWADManager.get().getIWAD(iwadId).path);
			context.iwadFile = iwadResolverFunction.apply(iwadPath);
		}
		
		for (long id : wadIds)
		{
			File wadPath = new File(WADManager.get().getWAD(id).path);
			String ext = FileUtils.getFileExtension(wadPath);
			
			if (ext.equalsIgnoreCase("wad") || ext.equalsIgnoreCase("pk3") || ext.equalsIgnoreCase("pke") || ext.equalsIgnoreCase("pk7"))
			{
				context.wads.add(wadResolverFunction.apply(wadPath));
			}
			else if (ext.equalsIgnoreCase("deh") || ext.equalsIgnoreCase("bex"))
			{
				context.dehs.add(wadResolverFunction.apply(wadPath));
			}
			else if (ext.equalsIgnoreCase("zip"))
			{
				try 
				{
					UnzipSet uzs = new UnzipSet(wadPath, tempDirectory);
					List<File> unzipped; 
					
					context.handler.outln("Extract WADs...");
					unzipped = uzs.unzipAll(WAD_ZIPFILTER);
					for (File f : unzipped)
					{
						context.handler.outln("Extracted WAD: " + f.getName());
						context.wads.add(unzippedFileDecoratorFunction.apply(f));
					}
					
					context.handler.outln("Extract DEHs...");
					unzipped = uzs.unzipAll(DEH_ZIPFILTER);
					for (File f : unzipped)
					{
						context.handler.outln("Extracted DEH: " + f.getName());
						context.dehs.add(unzippedFileDecoratorFunction.apply(f));
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
	
	private static File createCommandLineFile(LaunchContext context, EngineSettings settings, File tempDirectory, String[] extraArgs, boolean isDosBox) throws LaunchException
	{
		File cmdlineFile = new File(tempDirectory + File.separator + CMDLINE_FILE);
		try (PrintWriter cmdWriter = new PrintWriter(cmdlineFile))
		{
			writeCommandLine(context, settings, extraArgs, cmdWriter);
		} 
		catch (FileNotFoundException e) 
		{
			throw new LaunchException("Could not create file " + cmdlineFile.getPath() + "!");
		}
		
		if (isDosBox)
			return new File(DOSBOX_TEMP_MOUNT + "\\" + CMDLINE_FILE);
		else
			return cmdlineFile;
	}

	private static String[] createCommandLineArray(LaunchContext context, EngineSettings settings, File tempDirectory, String[] extraArgs) throws LaunchException
	{
		List<String> stringList = new LinkedList<>();
		if (settings.iwadSwitch != null && context.iwadFile != null)
		{
			stringList.add(settings.iwadSwitch);
			stringList.add(context.iwadFile.getPath());
		}
		
		if (!context.wads.isEmpty()) 
		{
			stringList.add(settings.fileSwitch);
			for (File w : context.wads)
				stringList.add(w.getPath());
		}
		
		if (settings.dehackedSwitch != null && !context.dehs.isEmpty())
		{
			stringList.add(settings.dehackedSwitch);
			stringList.add(context.dehs.getLast().getPath());
		}
		else if (settings.dehlumpSwitch != null)
		{
			stringList.add(settings.dehlumpSwitch);
		}

		if (context.saveDir != null)
		{
			stringList.add(settings.saveDirectorySwitch);
			stringList.add(context.saveDir.getPath());
		}
		
		if (context.screenshotDir != null)
		{
			stringList.add(settings.screenshotDirectorySwitch);
			stringList.add(context.screenshotDir.getPath());
		}

		if (settings.plan.commandLine != null)
		{
			Deque<String> cmdl = DoomyCommon.parseInput(settings.plan.commandLine);
			while (!cmdl.isEmpty())
				stringList.add(cmdl.pollFirst());
		}

		for (String arg : extraArgs)
			stringList.add(arg);
		
		return stringList.toArray(new String[stringList.size()]);
	}

	private static void writeCommandLine(LaunchContext context, EngineSettings settings, String[] extraArgs, PrintWriter cmdWriter)
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

		if (settings.plan.commandLine != null)
		{
			Deque<String> cmdl = DoomyCommon.parseInput(settings.plan.commandLine);
			while (!cmdl.isEmpty())
				cmdWriter.append(quoteEscape(cmdl.pollFirst())).print(' ');
		}

		for (String arg : extraArgs)
			cmdWriter.append(quoteEscape(arg)).print(' ');
		
		cmdWriter.flush();
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
	public int run(IOHandler handler, Preset preset, String[] extraArgs, boolean skipCleanup) throws LaunchException
	{
		EngineManager engineManager = EngineManager.get();
		EngineConfigManager engineSettingsManager = EngineConfigManager.get();
		
		Engine engine = engineManager.getEngine(preset.engineId);
		EngineSettings settings = engineSettingsManager.getEngineSettings(preset.engineId);
	
		if (engine == null)
			throw new LaunchException("Internal ERROR: Engine id " + preset.engineId + " does not exist!");
	
		File tempDirectory = checkTempDirectory();
		File presetDirectory = checkPresetDirectory(preset);
		settings.plan.planTypeId.checkValidity(engine, settings.plan);
		checkPresetValidity(preset, engine, settings);
	
		LaunchContext context = new LaunchContext();
		context.handler = handler;
		context.preset = preset;
		
		handler.outln("Prepare WADS...");
		settings.plan.planTypeId.setupRuntimeFiles(context, preset, tempDirectory);
		
		int retval;
		
		try {
			for (UnzipSet uzs : context.zipSets)
				IOUtils.close(uzs);

			File engineDir = new File(settings.plan.workDir);
			
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
			
			// Combine arguments.
			
			if (preset.commandLine.trim().length() > 0)
			{
				Deque<String> cmdl = DoomyCommon.parseInput(preset.commandLine);
				String[] cmdlarray = cmdl.toArray(new String[cmdl.size()]);
				extraArgs = ArrayUtils.joinArrays(cmdlarray, extraArgs);
			}
			
			// Launch.
			handler.outln("Launching...");
			InstancedFuture<Integer> process; 
			ProcessCallable callable;

			callable = settings.plan.planTypeId.createClientProcess(context, settings, extraArgs);
			process = TaskManager.get().spawn(callable);
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
