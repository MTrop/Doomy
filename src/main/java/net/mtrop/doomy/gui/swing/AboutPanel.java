/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;

import net.mtrop.doomy.DoomyCommon;
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
public class AboutPanel extends JPanel
{
	private static final long serialVersionUID = 5773470204586955071L;

	private static final String VERSION = DoomyCommon.getVersionString("doomy");
	private static final String VERSION_JSON = DoomyCommon.getVersionString("json");
	private static final String VERSION_SQL = DoomyCommon.getVersionString("sql");
	private static final String VERSION_SQLITE = DoomyCommon.getVersionString("sqlite");
	private static final String VERSION_FLATLAF = DoomyCommon.getVersionString("flatlaf");

	private static final String VERSION_TEXT = (new StringBuilder())
		.append("Doomy v").append(VERSION).append("\n")
		.append("\n")
		.append("Black Rook JSON v").append(VERSION_JSON).append("\n")
		.append("Black Rook SQL v").append(VERSION_SQL).append("\n")
		.append("SQLite JDBC v").append(VERSION_SQLITE).append("\n")
		.append("FlatLaf v").append(VERSION_FLATLAF).append("\n")
	.toString();
	
	/**
	 * Creates the About panel.
	 */
	public AboutPanel()
	{
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<h2>Doomy v").append(VERSION).append("</h2>");
		sb.append("by Matt Tropiano (see AUTHORS.TXT)").append("<br/>");
		sb.append("<br/>");
		sb.append("Using <b>Black Rook JSON v").append(VERSION_JSON).append("</b>").append("<br/>");
		sb.append("Using <b>Black Rook SQL v").append(VERSION_SQL).append("</b>").append("<br/>");
		sb.append("Using <b>SQLite JDBC v").append(VERSION_SQLITE).append("</b>").append("<br/>");
		sb.append("Using <b>FlatLaf v").append(VERSION_FLATLAF).append("</b>").append("<br/>");
		sb.append("<br/>");
		sb.append("<b>FlatLaf</b> Look And Feel (C) 2003-2022 FormDev Software GmbH").append("<br/>");
		sb.append("<br/>");
		sb.append("All third-party licenses are available in the \"licenses\" folder in the documentation folder.").append("<br/>");
		sb.append("<br/>");
		sb.append("<b>Thank you for using Doomy!</b>");
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
