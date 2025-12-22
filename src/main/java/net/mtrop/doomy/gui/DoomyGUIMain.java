/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui;

import net.mtrop.doomy.managers.DatabaseManager;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.LoggerManager;
import net.mtrop.doomy.struct.LoggingFactory.Logger;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.OSUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;
import net.mtrop.doomy.struct.util.StringUtils;

import java.awt.Dialog.ModalityType;
import java.awt.GraphicsEnvironment;

import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * Main entry for Doomy GUI.
 * @author Matthew Tropiano
 */
public final class DoomyGUIMain
{
	private static final Logger LOG = LoggerManager.getLogger(DoomyGUIMain.class);
	
	/**
	 * Sets the preferred Look And Feel.
	 */
	public static void setLAF() 
	{
		if (OSUtils.isOSX())
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		SwingUtils.setLAF(GUIManager.get().getThemeType().className);
	}
	
	public static void main(String[] args)
	{
		if (GraphicsEnvironment.isHeadless())
		{
			System.err.println("ERROR: Can't initialize GUI. Environment is headless!");
			System.exit(1);
		}
		
		setLAF();
		setExceptionHandler();
		
		if (!DatabaseManager.databaseExists())
		{
			Modal<Void> dbModal = modal("Preparing for First Use", ModalityType.MODELESS, containerOf(dimension(256, 32), borderLayout(),
				node(BorderLayout.CENTER, label("Creating database..."))
			));
			dbModal.setVisible(true);
			DatabaseManager.get();
			dbModal.dispose();
		}

		(new DoomyGUIMainWindow()).setVisible(true);
	}

	// Sets the exception handler.
	private static void setExceptionHandler()
	{
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			String threadName = thread.getName();
			LOG.errorf(exception, "Thread [%s] threw an uncaught exception!", threadName);
			
			LanguageManager language = LanguageManager.get();
			
			JScrollPane exceptionPane = scroll(ObjectUtils.apply(textArea(StringUtils.getJREExceptionString(exception), 20, 80), (area) -> {
				area.setEditable(false);
			}));
			
			Toolkit.getDefaultToolkit().beep();
			Boolean choice = modal(language.getText("doomy.exception.title", threadName),
				containerOf(borderLayout(),
					node(BorderLayout.NORTH, label(language.getText("doomy.exception.content"))),
					node(BorderLayout.CENTER, exceptionPane)
				),
				choice(language.getText("doomy.exception.continue"), Boolean.FALSE),
				choice(language.getText("doomy.exception.shutdown"), Boolean.TRUE)
			).openThenDispose();
			
			if (choice == Boolean.TRUE)
			{
				LOG.info("Forcing JVM shutdown...");
				System.exit(2);
			}
			
		});
	}

}
