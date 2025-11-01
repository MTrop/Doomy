/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;

import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.ClipboardUtils;

import java.awt.BorderLayout;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * A DoomMake panel for About info.
 * @author Matthew Tropiano
 */
public class AboutJavaPanel extends JPanel
{
	private static final long serialVersionUID = 4657463268466500171L;

	private static final String VERSION_TEXT = (new StringBuilder())
		.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n")
		.append("Java Vendor URL: ").append(System.getProperty("java.vendor.url")).append("\n")
		.append("Java Version: ").append(System.getProperty("java.version")).append("\n")
		.append("Java Home: ").append(System.getProperty("java.home")).append("\n")
		.append("OS Name: ").append(System.getProperty("os.name")).append("\n")
		.append("OS Version: ").append(System.getProperty("os.version")).append("\n")
		.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n")
	.toString();
	
	/**
	 * Creates the About panel.
	 */
	public AboutJavaPanel()
	{
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<b>Java Vendor:</b> ").append(System.getProperty("java.vendor")).append("<br/>");
		sb.append("<b>Java Vendor URL:</b> ").append(System.getProperty("java.vendor.url")).append("<br/>");
		sb.append("<b>Java Version:</b> ").append(System.getProperty("java.version")).append("<br/>");
		sb.append("<b>Java Home:</b> ").append(System.getProperty("java.home")).append("<br/>");
		sb.append("<b>OS Name:</b> ").append(System.getProperty("os.name")).append("<br/>");
		sb.append("<b>OS Version:</b> ").append(System.getProperty("os.version")).append("<br/>");
		sb.append("<b>OS Architecture:</b> ").append(System.getProperty("os.arch")).append("<br/>");
		sb.append("</html>");
		
		containerOf(this, 
			node(BorderLayout.CENTER, label(sb.toString())),
			node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.TRAILING),
				node(button(LanguageManager.get().getText("about.copy"), (b) -> {
					ClipboardUtils.sendStringToClipboard(VERSION_TEXT);
				}))
			))
		);
	}
	
}
