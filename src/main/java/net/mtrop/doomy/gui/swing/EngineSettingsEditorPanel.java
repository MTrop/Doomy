/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.io.File;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JPanel;

import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.EngineTemplateConfigManager;
import net.mtrop.doomy.managers.EngineTemplateConfigManager.EngineTemplateSettingEntry;
import net.mtrop.doomy.managers.EngineTemplateManager.EngineTemplate;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.FileChooserFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;

/**
 * An editor panel for Engine settings.
 * @author Matthew Tropiano
 */
public class EngineSettingsEditorPanel extends JPanel 
{
	private static final long serialVersionUID = -5188664754463400720L;

	private final GUIManager gui;
	private final LanguageManager language;
	private final EngineTemplateConfigManager engineTemplateConfigManager;
	
	private EngineSettings settings;
	
	/** EXE Path. */
	public JFormField<File> exePathField;
	/** SETUP EXE name. */
	public JFormField<String> setupFileNameField;
	/** Server EXE name. */
	public JFormField<String> serverFileNameField;
	/** Working directory override for engine. */
	public JFormField<File> workingDirectoryPathField;
	/** IWAD switch. */
	public JFormField<String> iwadSwitchField;
	/** File switch. */
	public JFormField<String> fileSwitchField;
	/** Dehacked switch. */
	public JFormField<String> dehackedSwitchField;
	/** Use DEHACKED lump switch. */
	public JFormField<String> dehlumpSwitchField;
	/** Savegame directory switch. */
	public JFormField<String> saveDirectorySwitchField;
	/** Screenshot directory switch. */
	public JFormField<String> screenshotDirectorySwitchField;
	/** RegEx for savegame cleanup. */
	public JFormField<String> saveGameRegexField;
	/** RegEx for screenshot cleanup. */
	public JFormField<String> screenshotRegexField;
	/** RegEx for demo cleanup. */
	public JFormField<String> demoRegexField;
	/** Extra command line options (passed in before literal options). */
	public JFormField<String> commandLineField;

	/** DOSBOX Path. */
	public JFormField<File> dosboxPathField;
	/** DOSBOX command line. */
	public JFormField<String> dosboxCommandLineField;

	/**
	 * Creates a new panel.
	 * @param startingSettings the settings to pre-populate with.
	 */
	public EngineSettingsEditorPanel(EngineSettings startingSettings)
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		this.engineTemplateConfigManager = EngineTemplateConfigManager.get();
		
		this.settings = new EngineSettings(startingSettings);
		this.exePathField = fileField(settings.exePath != null ? new File(settings.exePath) : null, "...", 
			(current) -> {
				File chosen = chooseFile(this, language.getText("file.browse.file.title"), current, language.getText("file.browse.select"), gui.createExecutableFilter());
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null)
				{
					settings.exePath = selected.getAbsolutePath();
					workingDirectoryPathField.setValue(selected.getParentFile());
				}
				else
				{
					settings.exePath = null;
				}
			}
		);
		this.setupFileNameField = stringField(settings.setupFileName, true, true, (v) -> settings.setupFileName = v);
		this.serverFileNameField = stringField(settings.serverFileName, true, true, (v) -> settings.serverFileName = v);
		this.workingDirectoryPathField = fileField(settings.workingDirectoryPath != null ? new File(settings.workingDirectoryPath) : null, "...", 
			(current) -> {
				File chosen = chooseDirectory(this, language.getText("file.browse.file.title"), current, language.getText("file.browse.select"), gui.createDirectoryFilter());
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null)
					settings.workingDirectoryPath = selected.getAbsolutePath();
				else
					settings.workingDirectoryPath = null;
			}
		);
		
		Predicate<String> REGEX_VALIDITY_TESTER = (regexPattern) -> {
			if (ObjectUtils.isEmpty(regexPattern))
				return true;
			try {
				Pattern.compile(regexPattern);
			} catch (PatternSyntaxException e) {
				SwingUtils.error(language.getText("pattern.error.badpattern"));
				return false;
			}
			return true;
		};
		
		this.iwadSwitchField = stringField(settings.iwadSwitch, true, true, (v) -> settings.iwadSwitch = v);
		this.fileSwitchField = stringField(settings.fileSwitch, true, true, (v) -> settings.fileSwitch = v);
		this.dehackedSwitchField = stringField(settings.dehackedSwitch, true, true, (v) -> settings.dehackedSwitch = v);
		this.dehlumpSwitchField = stringField(settings.dehlumpSwitch, true, true, (v) -> settings.dehlumpSwitch = v);
		this.saveDirectorySwitchField = stringField(settings.saveDirectorySwitch, true, true, (v) -> settings.saveDirectorySwitch = v);
		this.screenshotDirectorySwitchField = stringField(settings.screenshotDirectorySwitch, true, true, (v) -> settings.screenshotDirectorySwitch = v);
		
		this.saveGameRegexField = stringField(settings.saveGameRegex != null ? settings.saveGameRegex.pattern() : "", false, true, (v) -> settings.saveGameRegex = 
			REGEX_VALIDITY_TESTER.test(v) 
				? !ObjectUtils.isEmpty(v) ? Pattern.compile(v) : null 
				: null
		);
		this.screenshotRegexField = stringField(settings.screenshotRegex != null ? settings.screenshotRegex.pattern() : "", false, true, (v) -> settings.screenshotRegex = 
			REGEX_VALIDITY_TESTER.test(v) 
				? !ObjectUtils.isEmpty(v) ? Pattern.compile(v) : null 
				: null
		);
		this.demoRegexField = stringField(settings.demoRegex != null ? settings.demoRegex.pattern() : "", false, true, (v) -> settings.demoRegex = 
			REGEX_VALIDITY_TESTER.test(v) 
				? !ObjectUtils.isEmpty(v) ? Pattern.compile(v) : null 
				: null
		);

		this.commandLineField = stringField(settings.commandLine, true, true, (v) -> settings.commandLine = v);
		
		this.dosboxPathField = fileField(settings.dosboxPath != null ? new File(settings.dosboxPath) : null, "...", 
			(current) -> {
				File chosen = chooseFile(this, language.getText("file.browse.file.title"), current, language.getText("file.browse.select"), gui.createExecutableFilter());
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null) 
					settings.dosboxPath = selected.getAbsolutePath();
				else
					settings.dosboxPath = null;
			}
		);
		this.dosboxCommandLineField = stringField(settings.dosboxCommandLine, true, true, (v) -> settings.dosboxCommandLine = v);

		final int labelWidth = language.getInteger("engine.settings.labelwidth", 128);
		
		containerOf(this, borderLayout(),
			node(BorderLayout.NORTH, ObjectUtils.apply(new JPanel(), (panel) -> containerOf(panel, boxLayout(panel, BoxAxis.Y_AXIS),
				node(gui.createTitlePanel(language.getText("engine.settings.paths.title"),
					containerOf(
						node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
							gui.formField("engine.settings.exepath", exePathField),
							gui.formField("engine.settings.workingdir", workingDirectoryPathField),
							gui.formField("engine.settings.setupfile", setupFileNameField),
							gui.formField("engine.settings.serverfile", serverFileNameField)
						))
					)
				)),
				node(gui.createTitlePanel(language.getText("engine.settings.switches.title"),
					containerOf(
						node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
							gui.formField("engine.settings.iwadswitch", iwadSwitchField),
							gui.formField("engine.settings.fileswitch", fileSwitchField),
							gui.formField("engine.settings.dehswitch", dehackedSwitchField),
							gui.formField("engine.settings.dehlumpswitch", dehlumpSwitchField),
							gui.formField("engine.settings.savedirswitch", saveDirectorySwitchField),
							gui.formField("engine.settings.shotdirswitch", screenshotDirectorySwitchField)
						))
					)
				)),
				node(gui.createTitlePanel(language.getText("engine.settings.patterns.title"),
					containerOf(
						node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
							gui.formField("engine.settings.savepattern", saveGameRegexField),
							gui.formField("engine.settings.shotpattern", screenshotRegexField),
							gui.formField("engine.settings.demopattern", demoRegexField)
						))
					)
				)),
				node(gui.createTitlePanel(language.getText("engine.settings.other.title"),
					containerOf(
						node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
							gui.formField("engine.settings.commandline", commandLineField)
						))
					)
				)),
				node(gui.createTitlePanel(language.getText("engine.settings.dosbox.title"),
					containerOf(
						node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
							gui.formField("engine.settings.dosboxpath", dosboxPathField),
							gui.formField("engine.settings.dosboxcommandline", dosboxCommandLineField)
						))
					)
				)),
				node(gui.createTitlePanel(language.getText("engine.settings.templates.title"),
					containerOf(
						node(button(language.getText("engine.settings.usetemplate"), (b) -> applyTemplate()))
					)
				))
			)))
		);
	}
	
	private void clearFields()
	{
		exePathField.setValue(null);
		setupFileNameField.setValue(null);
		serverFileNameField.setValue(null);
		workingDirectoryPathField.setValue(null);
		iwadSwitchField.setValue(null);
		fileSwitchField.setValue(null);
		dehackedSwitchField.setValue(null);
		dehlumpSwitchField.setValue(null);
		saveDirectorySwitchField.setValue(null);
		screenshotDirectorySwitchField.setValue(null);
		saveGameRegexField.setValue(null);
		screenshotRegexField.setValue(null);
		demoRegexField.setValue(null);
		commandLineField.setValue(null);
		dosboxPathField.setValue(null);
		dosboxCommandLineField.setValue(null);
	}
	
	private void applyTemplate()
	{
		EngineTemplate template = browseEngineTemplate();
		if (template != null)
		{
			clearFields();
			EngineTemplateSettingEntry[] entries = engineTemplateConfigManager.getAllSettings(template.name, "");
			for (EngineTemplateSettingEntry entry : entries)
			{
				switch (entry.name)
				{
					case EngineConfigManager.SETTING_EXEPATH:
						exePathField.setValue(new File(entry.value));
						break;
					case EngineConfigManager.SETTING_DOSBOXPATH:
						dosboxPathField.setValue(new File(entry.value));
						break;
					case EngineConfigManager.SETTING_DOSBOXCOMMANDLINE:
						dosboxCommandLineField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SETUPFILENAME:
						setupFileNameField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SERVERFILENAME:
						serverFileNameField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_WORKDIRPATH:
						workingDirectoryPathField.setValue(new File(entry.value));
						break;
					case EngineConfigManager.SETTING_FILESWITCH:
						fileSwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_IWADSWITCH:
						iwadSwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_DEHSWITCH:
						dehackedSwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_DEHLUMPSWITCH:
						dehlumpSwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SAVEDIRSWITCH:
						saveDirectorySwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SHOTDIRSWITCH:
						screenshotDirectorySwitchField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SAVEPATTERN:
						saveGameRegexField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_SHOTPATTERN:
						screenshotRegexField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_DEMOPATTERN:
						demoRegexField.setValue(entry.value);
						break;
					case EngineConfigManager.SETTING_COMMANDLINE:
						commandLineField.setValue(entry.value);
						break;
				}
			}
		}
	}
	
	private EngineTemplate browseEngineTemplate()
	{
		final EngineTemplateTablePanel engineTemplateTablePanel = new EngineTemplateTablePanel(SelectionPolicy.SINGLE, (model, event) -> {}, (event) -> {});
		
		Boolean ok = modal(this, language.getText("template.select"), 
			containerOf(dimension(350, 200), borderLayout(),
				node(BorderLayout.CENTER, engineTemplateTablePanel)
			),
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return null;
		
		return engineTemplateTablePanel.getSelectedTemplates().get(0);
	}
	
	/**
	 * @return the current engine settings. 
	 */
	public EngineSettings getSettings()
	{
		return settings;
	}
	
}
