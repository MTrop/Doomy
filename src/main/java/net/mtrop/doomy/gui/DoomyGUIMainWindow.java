/*******************************************************************************
 * Copyright (c) 2019-2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomyUpdater;
import net.mtrop.doomy.gui.swing.AboutJavaPanel;
import net.mtrop.doomy.gui.swing.AboutPanel;
import net.mtrop.doomy.gui.swing.DoomFetchControlPanel;
import net.mtrop.doomy.gui.swing.EngineTableControlPanel;
import net.mtrop.doomy.gui.swing.IdGamesSearchControlPanel;
import net.mtrop.doomy.gui.swing.IwadTableControlPanel;
import net.mtrop.doomy.gui.swing.PresetTableControlPanel;
import net.mtrop.doomy.gui.swing.ProgressPanel;
import net.mtrop.doomy.gui.swing.SettingsPanel;
import net.mtrop.doomy.gui.swing.WadTableControlPanel;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.LoggerManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.struct.InstancedFuture;
import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.swing.BorderFactory.createEmptyBorder;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;


/**
 * The main window for Doomy.
 */
public class DoomyGUIMainWindow extends JFrame
{
	private static final long serialVersionUID = -7341160374312252193L;

	private static final Logger LOG = LoggerManager.getLogger(DoomyGUIMainWindow.class);
	
	/** Doomy Version */
	public static final String VERSION = DoomyCommon.getVersionString("doomy");

	private final GUIManager gui;
	private final LanguageManager language;
	
	public DoomyGUIMainWindow()
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		
		setTitle("Doomy v" + VERSION);
		setJMenuBar(createMenuBar());
		setResizable(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);
		setIconImages(gui.getWindowIcons());
		
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				attemptClose();
			}
		});
		
		setContentPane(containerOf(dimension(720, 480), borderLayout(8, 8),
			node(BorderLayout.CENTER, tabs(TabPlacement.TOP, TabLayoutPolicy.WRAP, 
				tab(language.getText("tab.presets"), containerOf(
					node(new PresetTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.engines"), containerOf(
					node(new EngineTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.iwads"), containerOf(
					node(new IwadTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.wads"), containerOf(
					node(new WadTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.idgames"), containerOf(
					node(new IdGamesSearchControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.doomfetch"), containerOf(
					node(new DoomFetchControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				))
			), (tabs) -> tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)))
		));
		
		pack();
	}
	
	private JMenuBar createMenuBar()
	{
		return menuBar(
			gui.createMenuFromLanguageKey("menu.file",
				gui.createItemFromLanguageKey("menu.file.prefs", (i) -> openSettings()),
				gui.createItemFromLanguageKey("menu.file.exit", (i) -> attemptClose())
			),
			gui.createMenuFromLanguageKey("menu.help",
				gui.createItemFromLanguageKey("menu.help.about", (i) -> onAbout()),
				gui.createItemFromLanguageKey("menu.help.about.java", (i) -> onAboutJava()),
				separator(),
				gui.createItemFromLanguageKey("menu.help.open.config", (i) -> onOpenConfigFolder()),
				separator(),
				gui.createItemFromLanguageKey("menu.help.update", (i) -> onUpdate())
			)
		);
	}
	
	private void onAbout()
	{
		modal(this, language.getText("about.title"), 
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, new AboutPanel())
			), 
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true)
		).openThenDispose();
	}
	
	private void onAboutJava()
	{
		modal(this, language.getText("about.java.title"), 
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, new AboutJavaPanel())
			), 
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true)
		).openThenDispose();
	}
	
	private void openSettings()
	{
		modal(this, language.getText("settings.title"), 
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, new SettingsPanel())
			)
		).openThenDispose();
	}
	
	private void onOpenConfigFolder()
	{
		try {
			Desktop.getDesktop().open(new File(DoomyEnvironment.getApplicationConfigPath()));
		} catch (IOException e) {
			SwingUtils.error(this, language.getText("config.open.error"));
		}
	}
	
	private void onUpdate() 
	{
		final String path; 
		try {
			path = DoomyEnvironment.getDoomyPath();
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomy.error.pathenvvar"));
			return;
		}
		
		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(language.getText("doomy.error.pathenvvar"));
			return;
		}
		
		final ProgressPanel progressPanel = new ProgressPanel(48);
		progressPanel.setActivityMessage("Please wait...");
		progressPanel.setProgressLabel("");
		progressPanel.setIndeterminate();
		
		Modal<Object> progressModal = modal(
			gui.getWindowIcons(),
			language.getText("doomy.update.title"),
			containerOf(createEmptyBorder(8, 8, 8, 8), node(BorderLayout.CENTER, progressPanel)),
			gui.createChoiceFromLanguageKey("choice.cancel")
		);

		final AtomicBoolean successful = new AtomicBoolean(false);
		
		// Listener 
		DoomyUpdater.Listener listener = new DoomyUpdater.Listener() 
		{
			@Override
			public void onMessage(String message) 
			{
				progressPanel.setActivityMessage(message);
			}

			@Override
			public void onError(String message) 
			{
				SwingUtils.error(progressModal, message);
				progressPanel.setErrorMessage(language.getText("doomy.update.failed"));
			}

			@Override
			public void onDownloadStart() 
			{
				progressPanel.setActivityMessage(language.getText("doomy.update.downloading"));
				progressPanel.setProgressLabel("0%");
			}

			@Override
			public void onDownloadTransfer(long current, Long max) 
			{
				int kbs = (int)(current / 1024L);
				if (max != null)
				{
					int maxkbs = (int)(max / 1024L);
					int pct = kbs * 100 / maxkbs;
					progressPanel.setActivityMessage(language.getText("doomy.update.downloading.amount2", kbs, maxkbs));
					progressPanel.setProgressLabel(pct + "%");
					progressPanel.setProgress(0, kbs, maxkbs);
				}
				else // length was not in response
				{
					progressPanel.setActivityMessage(language.getText("doomy.update.downloading.amount1", kbs));
					progressPanel.setProgressLabel("N/A");
				}
			}

			@Override
			public void onDownloadFinish() 
			{
				progressPanel.setActivityMessage(language.getText("doomy.update.downloading.finished"));
				progressPanel.setProgressLabel("100%");
				progressPanel.setProgress(0, 100, 100);
			}

			@Override
			public boolean shouldContinue(String versionString)
			{
				progressPanel.setActivityMessage(language.getText("doomy.update.downloading.found"));
				return SwingUtils.yesTo(progressModal, language.getText("doomy.update.continue", versionString));
			}

			@Override
			public void onUpToDate() 
			{
				progressPanel.setSuccessMessage(language.getText("doomy.update.downloading.uptodate"));
				progressPanel.setProgressLabel("100%");
				progressPanel.setProgress(0, 100, 100);
			}

			@Override
			public void onUpdateSuccessful() 
			{
				progressPanel.setSuccessMessage(language.getText("doomy.update.downloading.success"));
				successful.set(true);
			}

			@Override
			public void onUpdateAbort() 
			{
				progressPanel.setErrorMessage(language.getText("doomy.update.downloading.aborted"));
				progressPanel.setProgressLabel("");
				progressPanel.setProgress(0, 0, 100);
			}
		};
		
		try {
			
			InstancedFuture<Integer> instance = TaskManager.get().spawn(new DoomyUpdater(new File(path), listener));
			progressModal.openThenDispose(); // will hold here until closed.
			if (!instance.isDone())
				instance.cancel();
			
			if (successful.get())
				SwingUtils.info(this, language.getText("doomy.update.success"));
			
		} catch (Exception e) {
			LOG.error(e, "Uncaught error during update.");
			SwingUtils.error(this, "Uncaught error during update: " + e.getClass().getSimpleName());
		}
	}

	private void attemptClose()
	{
		if (onCloseAttempt())
		{
			setVisible(false);
			dispose();
		}
	}

	private boolean onCloseAttempt()
	{
		return true;
	}
	
}
