/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;

import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.GUIManager.GUIThemeType;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * Settings panel for Doomy GUI.
 * @author Matthew Tropiano
 */
public class SettingsPanel extends JPanel
{
	private static final long serialVersionUID = -8580767280945724831L;

	private final ConfigManager config;
	private final GUIManager gui;
	private final LanguageManager language;

	private JFormField<GUIThemeType> guiThemeTypeField;
	private JFormField<File> initialDirectoryField;
	
	private JFormField<String> idGamesAPIURLField;
	private JFormField<String> idGamesDownloadURLField;
	private JFormField<Integer> idGamesTimeoutField;
	
	public SettingsPanel()
	{
		this.config = ConfigManager.get();
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		
		this.guiThemeTypeField = comboField(comboBox(Arrays.asList(GUIThemeType.values()), (v) -> gui.setThemeType(v)));
		this.guiThemeTypeField.setValue(gui.getThemeType());

		this.initialDirectoryField = fileField(
			config.getConvertedValue(ConfigManager.SETTING_FILECHOOSER_DEFAULT_DIR, (value) -> value != null ? new File(value) : null),
			"...",
			(current) -> {
				File chosen = gui.chooseDirectory(this, 
					language.getText("settings.browse.dir.title"), 
					language.getText("settings.browse.dir.select"), 
					() -> current != null ? current : gui.getDefaultFile(),
					gui::setDefaultFile				
				);
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				config.setValue(ConfigManager.SETTING_FILECHOOSER_DEFAULT_DIR, selected != null ? selected.getAbsolutePath() : null);
			}
		);
		
		this.idGamesAPIURLField = stringField(config.getValue(ConfigManager.SETTING_IDGAMES_API_URL), false, true, 
			(v) -> config.setValue(ConfigManager.SETTING_IDGAMES_API_URL, v) 
		);
		this.idGamesDownloadURLField = stringField(config.getValue(ConfigManager.SETTING_IDGAMES_MIRROR_BASE_URL), false, true, 
			(v) -> config.setValue(ConfigManager.SETTING_IDGAMES_MIRROR_BASE_URL, v) 
		);
		this.idGamesTimeoutField = integerField(Integer.parseInt(config.getValue(ConfigManager.SETTING_IDGAMES_TIMEOUT_MILLIS, "0")),
			(v) -> config.setValue(ConfigManager.SETTING_IDGAMES_TIMEOUT_MILLIS, String.valueOf(v)) 
		);
		
		containerOf(this,
			node(tabs(TabPlacement.LEFT, TabLayoutPolicy.SCROLL,
				tab(language.getText("settings.tab.doomy"), containerOf(borderLayout(),
					node(BorderLayout.NORTH, gui.createForm(
						form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("settings.tab.doomy.labelwidth")),
							gui.formField("settings.tab.doomy.guitheme", guiThemeTypeField), 
							gui.formField("settings.tab.doomy.defaultdir", initialDirectoryField)
						), 
						(n) -> n.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0))
					),
					node(BorderLayout.CENTER, containerOf()),
					node(BorderLayout.SOUTH, label(language.getHTML("settings.tab.doomy.message")), 
						(n) -> n.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0))
					)
				)),
				tab(language.getText("settings.tab.idgames"), containerOf(borderLayout(),
					node(BorderLayout.NORTH, gui.createForm(
						form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("settings.tab.idgames.labelwidth")),
							gui.formField("settings.tab.idgames.apiurl", idGamesAPIURLField),
							gui.formField("settings.tab.idgames.mirrorurl", idGamesDownloadURLField),
							gui.formField("settings.tab.idgames.timeout", idGamesTimeoutField)
						),
						(n) -> n.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0))
					),
					node(BorderLayout.CENTER, containerOf())
				))
			))
		);
	}
}
