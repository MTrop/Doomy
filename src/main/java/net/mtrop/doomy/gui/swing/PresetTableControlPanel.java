/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.LauncherManager;
import net.mtrop.doomy.managers.MessengerManager;
import net.mtrop.doomy.managers.LauncherManager.LaunchException;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.Preset;
import net.mtrop.doomy.managers.PresetManager.PresetInfo;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.util.IOUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * The preset table control panel.
 * @author Matthew Tropiano
 */
public class PresetTableControlPanel extends JPanel
{
	private static final long serialVersionUID = -5512536206650970521L;
	
	private final MessengerManager messenger;
	private final GUIManager gui;
	private final TaskManager taskManager;
	private final LanguageManager language;
	private final PresetManager presetManager;
	private final LauncherManager launcherManager;
	
	private PresetTablePanel presetTable;
	private Action createAction;
	private Action removeAction;
	private Action openFolderAction;
	private Action cleanupFoldersAction;
	private Action launchAction;

	public PresetTableControlPanel()
	{
		this.messenger = MessengerManager.get();
		this.gui = GUIManager.get();
		this.taskManager = TaskManager.get();
		this.language = LanguageManager.get();
		this.presetManager = PresetManager.get();
		this.launcherManager = LauncherManager.get();
		
		this.presetTable = new PresetTablePanel(SelectionPolicy.MULTIPLE_INTERVAL, (model, event) -> onSelection(), (event) -> onLaunch());

		this.createAction = actionItem(language.getText("preset.create"), (e) -> onCreate());
		this.removeAction = actionItem(language.getText("preset.remove"), (e) -> onRemove());
		this.openFolderAction = actionItem(language.getText("preset.open"), (e) -> onOpen());
		this.cleanupFoldersAction = actionItem(language.getText("preset.cleanup"), (e) -> onCleanup());
		this.launchAction = actionItem(language.getHTML("preset.launch"), (e) -> onLaunch());

		onSelection();

		this.messenger.subscribe(MessengerManager.CHANNEL_PRESETS_CHANGED, (message) -> presetTable.refreshPresets());
		
		containerOf(this, borderLayout(8, 0),
			node(BorderLayout.CENTER, presetTable),
			node(BorderLayout.EAST, containerOf(dimension(language.getInteger("preset.actions.width"), 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1, 0, 2),
					node(button(createAction)),
					node(button(removeAction)),
					node(button(openFolderAction)),
					node(button(cleanupFoldersAction)),
					node(button(launchAction))
				)),
				node(BorderLayout.CENTER, containerOf())
			))
		);
	}
	
	private Engine browseEngine(Engine selected)
	{
		final EngineTablePanel engineTablePanel = new EngineTablePanel(SelectionPolicy.SINGLE, (model, event) -> {}, (event) -> {});
		
		Boolean ok = modal(this, language.getText("engine.select"), 
			containerOf(dimension(350, 200), borderLayout(),
				node(BorderLayout.CENTER, engineTablePanel)
			),
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return selected;
		
		return engineTablePanel.getSelectedEngines().get(0);
	}
	
	private IWAD browseIWAD(IWAD selected)
	{
		final IwadTablePanel iwadTablePanel = new IwadTablePanel(SelectionPolicy.SINGLE, (model, event) -> {}, (event) -> {});
		
		Boolean ok = modal(this, language.getText("iwads.select"), 
			containerOf(dimension(350, 200), borderLayout(),
				node(BorderLayout.CENTER, iwadTablePanel)
			),
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return selected;
		
		return iwadTablePanel.getSelectedIWADs().get(0);
	}
	
	private void onCreate() 
	{
		final JFormField<Engine> engineField = valueBrowseTextField("...", this::browseEngine, new JValueConverter<Engine>() {

			@Override
			public Engine getValueFromText(String text) 
			{
				if (ObjectUtils.isEmpty(text))
					return null;
				return EngineManager.get().getEngine(text);
			}

			@Override
			public String getTextFromValue(Engine value)
			{
				return value != null ? value.name : "";
			}
			
		});
		
		final JFormField<IWAD> iwadField = valueBrowseTextField("...", this::browseIWAD, new JValueConverter<IWAD>() {

			@Override
			public IWAD getValueFromText(String text) 
			{
				if (ObjectUtils.isEmpty(text))
					return null;
				return IWADManager.get().getIWAD(text);
			}

			@Override
			public String getTextFromValue(IWAD value)
			{
				return value != null ? value.name : "";
			}
			
		});
		
		final PresetWadsSelectionPanel presetWadsSelectionPanel = new PresetWadsSelectionPanel();
		
		final JFormField<String> presetNameField = stringField(true, true);
		
		Boolean ok = modal(this, language.getText("preset.create.title"), 
			ObjectUtils.apply(new JPanel(), (panel) -> containerOf(panel, dimension(350, 200), boxLayout(panel, BoxAxis.Y_AXIS),
				node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("preset.create.labelwidth")),
					gui.formField("preset.create.name", presetNameField),
					gui.formField("preset.create.engine", engineField),
					gui.formField("preset.create.iwad", iwadField)
				)),
				node(presetWadsSelectionPanel)
			)),
			gui.createChoiceFromLanguageKey("preset.create.choice", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		// build preset.
		
		Engine selectedEngine = engineField.getValue();
		
		if (selectedEngine == null)
		{
			SwingUtils.error(language.getText("preset.create.error.noengine"));
			return;
		}

		IWAD selectedIWAD = iwadField.getValue();

		List<WAD> selectedWads = presetWadsSelectionPanel.getObjects();

		String presetName;
		if (presetNameField.getValue() != null)
		{
			presetName = presetNameField.getValue();
		}
		else
		{
			if (selectedWads.isEmpty())
				presetName = selectedEngine.name;
			else
				presetName = selectedWads.get(0).name;
		}
		
		int i = 0;
		long[] wadIds = new long[selectedWads.size()];
		for (WAD wad : selectedWads)
			wadIds[i++] = wad.id;
		
		if (presetManager.containsPreset(presetName))
		{
			SwingUtils.error(this, language.getText("preset.create.error.exists", presetName));
			return;
		}
		
		presetManager.addPreset(presetName, selectedEngine.id, selectedIWAD != null ? selectedIWAD.id : null, wadIds);
		presetTable.refreshPresets();
	}

	private void onRemove() 
	{
		final List<PresetInfo> selected = presetTable.getSelectedPresets();
		if (SwingUtils.noTo(this, language.getText("preset.remove.message", selected.size())))
			return;

		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
	
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("engine.remove.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
			
		SwingUtils.invoke(() -> {
			progressBar.setMinimum(0);
			progressBar.setIndeterminate(true);
			progressBar.setMaximum(selected.size());
		});
	
		taskManager.spawn(() -> 
		{
			signal.poll();
			int c = 0;
			for (PresetInfo engine : selected)
			{
				if (cancelSwitch.get())
					return;
	
				final int count = c;
				SwingUtils.invoke(() -> {
					progressBar.setValue(count);
					progressBar.setIndeterminate(false);
					progressLabel.setText((int)((float)count / selected.size() * 100) + "%");
				});
				
				presetManager.deletePresetByName(engine.name);
				c++;
			}
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);

		presetTable.refreshPresets();
	}

	private void onOpen() 
	{
		PresetInfo source = presetTable.getSelectedPresets().get(0);
		File presetDir = new File(DoomyEnvironment.getPresetDirectoryPath(source.hash));
		DoomyCommon.openInSystemBrowser(presetDir);
	}

	private void onCleanup()
	{
		if (SwingUtils.noTo(this, language.getText("preset.cleanup.message")))
			return;
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
	
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("preset.cleanup.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
			
		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(true);
		});
	
		AtomicInteger progressIndex = new AtomicInteger(0);

		taskManager.spawn(() -> 
		{
			signal.poll();
			
			PresetInfo[] allPresets = presetManager.getAllPresets();
			String presetParentPath = DoomyEnvironment.getPresetDirectoryPath();
			Set<String> hashSet = new HashSet<>();
			SortedSet<File> dirSet = new TreeSet<>();
			
			for (PresetInfo info : allPresets)
				hashSet.add(info.hash);
			
			for (File dir : (new File(presetParentPath)).listFiles((f) -> f.isDirectory()))
			{
				if (cancelSwitch.get())
					return;

				if (!hashSet.contains(dir.getName()))
					dirSet.add(dir);
			}

			if (cancelSwitch.get())
				return;
			
			if (!dirSet.isEmpty())
			{
				for (File presetDir : dirSet)
				{
					SwingUtils.invoke(() -> {
						progressBar.setIndeterminate(false);
						progressBar.setMinimum(0);
						progressBar.setMaximum(dirSet.size());
						progressLabel.setText((int)((float)progressIndex.get() / dirSet.size() * 100) + "%");
					});
					
					for (File f : presetDir.listFiles())
						f.delete();
					
					if (presetDir.delete())
						progressIndex.incrementAndGet();
				}
				
				SwingUtils.invoke(() -> {
					progressBar.setMinimum(1);
					progressBar.setMaximum(1);
					progressLabel.setText("100%");
				});
				
			}

			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);

		if (progressIndex.get() == 0)
			SwingUtils.info(this, language.getText("preset.cleanup.nofolders"));
		else
			SwingUtils.info(this, language.getText("preset.cleanup.folders", progressIndex.get()));
	}
	
	private void onLaunch() 
	{
		List<PresetInfo> selectedPresets = presetTable.getSelectedPresets();
		if (selectedPresets.isEmpty())
			return;

		final TextOutputPanel textOutputPanel = new TextOutputPanel();
		final PrintStream outStream = textOutputPanel.getPrintStream();
		final PrintStream errStream = textOutputPanel.getErrorPrintStream();
		IOHandler ioHandler = new IOHandler() 
		{
			@Override
			public void close() throws Exception 
			{
				// Do nothing.
			}
			
			@Override
			public void relay(InputStream in) throws IOException
			{
				IOUtils.relay(in, outStream);
			}
			
			@Override
			public String readLine()
			{
				return null;
			}
			
			@Override
			public String prompt(String prompt) 
			{
				return "";
			}
			
			@Override
			public void out(Object message) 
			{
				outStream.print(message);
			}
			
			@Override
			public void err(Object message)
			{
				errStream.print(message);
			}
		};
		
		final Modal<Void> outputModal = modal(this, language.getText("preset.launch.title"), containerOf(borderLayout(),
			node(BorderLayout.CENTER, textOutputPanel)
		));
		
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();

		taskManager.spawn(() -> 
		{
			signal.poll();
			Preset preset = presetManager.getPreset(selectedPresets.get(0).id); 
			try {
				launcherManager.run(ioHandler, preset, new String[]{}, false);
			} catch (LaunchException e) {
				SwingUtils.error(this, e.getLocalizedMessage());
			} finally {
				outputModal.dispose();
			}
		});
		
		signal.offer(true);
		outputModal.openThenDispose();
	}

	private void onSelection()
	{
		List<PresetInfo> presets = presetTable.getSelectedPresets();
		removeAction.setEnabled(!presets.isEmpty());
		launchAction.setEnabled(presets.size() == 1);
		openFolderAction.setEnabled(presets.size() == 1);
	}

}
