/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import net.mtrop.doomy.managers.GUIManager.FormFieldInfo;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.LauncherManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.util.ArrayUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
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

	private JFormField<File> normalWorkingDirectoryPathField;
	private JFormField<File> normalExePathField;
	private JFormField<File> dosboxPathField;
	private JFormField<String> packageField;
	private JFormField<String> packageCommandLineField;
	private JFormField<String> clientFileNameField;
	private JFormField<String> setupFileNameField;
	private JFormField<String> serverFileNameField;
	private JFormField<String> commandLineField;
	
	/** Fields for each plan type. */
	private Map<LauncherManager.PlanType, FormFieldInfo[]> planPathFields; 
	/** The panel that holds execution plan fields. */
	private JFormField<LauncherManager.PlanType> planComboField;
	/** The panel that holds execution plan fields. */
	private Container planFieldPanel;
	
	/** IWAD switch. */
	private JFormField<String> iwadSwitchField;
	/** File switch. */
	private JFormField<String> fileSwitchField;
	/** Dehacked switch. */
	private JFormField<String> dehackedSwitchField;
	/** Use DEHACKED lump switch. */
	private JFormField<String> dehlumpSwitchField;
	/** Savegame directory switch. */
	private JFormField<String> saveDirectorySwitchField;
	/** Screenshot directory switch. */
	private JFormField<String> screenshotDirectorySwitchField;
	/** RegEx for savegame cleanup. */
	private JFormField<String> saveGameRegexField;
	/** RegEx for screenshot cleanup. */
	private JFormField<String> screenshotRegexField;
	/** RegEx for demo cleanup. */
	private JFormField<String> demoRegexField;

	/**
	 * Creates a new panel.
	 * @param startingSettings the settings to pre-populate with.
	 */
	public EngineSettingsEditorPanel(EngineSettings startingSettings)
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		this.engineTemplateConfigManager = EngineTemplateConfigManager.get();
		
		this.planPathFields = new HashMap<>();
		
		this.settings = new EngineSettings(startingSettings);
		
		this.planComboField = comboField(comboBox(Arrays.asList(LauncherManager.PlanType.values()), (v) -> switchPlanFields(v, this.settings)));
		this.planFieldPanel = containerOf();
		
		this.normalWorkingDirectoryPathField = fileField(null, "...", 
			(current) -> {
				File chosen = gui.chooseDirectory(this, 
					language.getText("file.browse.file.title"), 
					language.getText("file.browse.select"),
					() -> current != null ? current : gui.getDefaultFile(),
					gui::setDefaultFile
				);
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null)
					settings.plan.workDir = selected.getAbsolutePath();
				else
					settings.plan.workDir = null;
			}
		);
		
		this.normalExePathField = fileField(null, "...", 
			(current) -> {
				File chosen = gui.chooseFile(this, 
					language.getText("file.browse.file.title"), 
					language.getText("file.browse.select"),
					() -> current != null ? current : gui.getDefaultFile(),
					gui::setDefaultFile
				);
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null)
				{
					settings.plan.clientName = selected.getAbsolutePath();
					normalWorkingDirectoryPathField.setValue(selected.getParentFile());
				}
				else
				{
					settings.plan.clientName = null;
				}
			}
		);
		
		this.dosboxPathField = fileField(null, "...", 
			(current) -> {
				File chosen = gui.chooseFile(this, 
					language.getText("file.browse.file.title"), 
					language.getText("file.browse.select"), 
					() -> current != null ? current : gui.getDefaultFile(),
					gui::setDefaultFile
				);
				return chosen != null ? chosen : current;
			},
			(selected) -> {
				if (selected != null) 
					settings.plan.packageName = selected.getAbsolutePath();
				else
					settings.plan.packageName = null;
			}
		);
		
		this.packageField = stringField(null, true, true, (v) -> settings.plan.packageName = v);
		this.packageCommandLineField = stringField("", false, true, (v) -> settings.plan.packageCommandLine = v);
		this.clientFileNameField = stringField(null, true, true, (v) -> settings.plan.clientName = v);
		this.setupFileNameField = stringField(null, true, true, (v) -> settings.plan.setupName = v);
		this.serverFileNameField = stringField(null, true, true, (v) -> settings.plan.serverName = v);
		this.commandLineField = stringField("", false, true, (v) -> settings.plan.commandLine = v);

		createEnginePlanCards();
		
		this.iwadSwitchField = stringField(settings.iwadSwitch, true, true, (v) -> settings.iwadSwitch = v);
		this.fileSwitchField = stringField(settings.fileSwitch, true, true, (v) -> settings.fileSwitch = v);
		this.dehackedSwitchField = stringField(settings.dehackedSwitch, true, true, (v) -> settings.dehackedSwitch = v);
		this.dehlumpSwitchField = stringField(settings.dehlumpSwitch, true, true, (v) -> settings.dehlumpSwitch = v);
		this.saveDirectorySwitchField = stringField(settings.saveDirectorySwitch, true, true, (v) -> settings.saveDirectorySwitch = v);
		this.screenshotDirectorySwitchField = stringField(settings.screenshotDirectorySwitch, true, true, (v) -> settings.screenshotDirectorySwitch = v);
		
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

		final int labelWidth = language.getInteger("engine.settings.labelwidth", 128);
		
		containerOf(this, borderLayout(),
			node(BorderLayout.NORTH, ObjectUtils.apply(new JPanel(), (panel) -> containerOf(panel, boxLayout(panel, BoxAxis.Y_AXIS),
				node(containerOf(
					node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
						gui.formField("engine.settings.plan", planComboField)
					))
				)),
				node(planFieldPanel),
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
				node(gui.createTitlePanel(language.getText("engine.settings.templates.title"),
					containerOf(
						node(button(language.getText("engine.settings.usetemplate"), (b) -> applyTemplate()))
					)
				))
			)))
		);
		
		this.planComboField.setValue(startingSettings.plan.planTypeId);
	}
	
	private void createEnginePlanCards()
	{
		planPathFields.put(LauncherManager.PlanType.NORMAL, ArrayUtils.arrayOf(
			gui.formField("engine.settings.workingdir", normalWorkingDirectoryPathField),
			gui.formField("engine.settings.exepath", normalExePathField),
			gui.formField("engine.settings.setupfile", setupFileNameField),
			gui.formField("engine.settings.serverfile", serverFileNameField),
			gui.formField("engine.settings.commandline", commandLineField)
		));
		planPathFields.put(LauncherManager.PlanType.DOSBOX, ArrayUtils.arrayOf(
			gui.formField("engine.settings.dosboxpath", dosboxPathField),
			gui.formField("engine.settings.dosboxcommandline", packageCommandLineField),
			gui.formField("engine.settings.workingdir", normalWorkingDirectoryPathField),
			gui.formField("engine.settings.exepath", normalExePathField),
			gui.formField("engine.settings.setupfile", setupFileNameField),
			gui.formField("engine.settings.serverfile", serverFileNameField),
			gui.formField("engine.settings.commandline", commandLineField)
		));
		planPathFields.put(LauncherManager.PlanType.APPIMAGE, ArrayUtils.arrayOf(
			gui.formField("engine.settings.workingdir", normalWorkingDirectoryPathField),
			gui.formField("engine.settings.appimage", packageField),
			gui.formField("engine.settings.appimagecommandline", packageCommandLineField),
			gui.formField("engine.settings.clientimage", clientFileNameField),
			gui.formField("engine.settings.setupimage", setupFileNameField),
			gui.formField("engine.settings.serverimage", serverFileNameField),
			gui.formField("engine.settings.commandline", commandLineField)
		));
		planPathFields.put(LauncherManager.PlanType.FLATPAK, ArrayUtils.arrayOf(
			gui.formField("engine.settings.workingdir", normalWorkingDirectoryPathField),
			gui.formField("engine.settings.flatpak", packageField),
			gui.formField("engine.settings.flatpakcommandline", packageCommandLineField),
			gui.formField("engine.settings.clientimage", clientFileNameField),
			gui.formField("engine.settings.setupimage", setupFileNameField),
			gui.formField("engine.settings.serverimage", serverFileNameField),
			gui.formField("engine.settings.commandline", commandLineField)
		));
	}
	
	private void switchPlanFields(LauncherManager.PlanType type, EngineSettings settings)
	{
		FormFieldInfo[] fields = planPathFields.get(type);
		planFieldPanel.removeAll();
		planFieldPanel.add(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("engine.settings.labelwidth")), fields));
		
		switch (type)
		{
			default:
			case NORMAL:
				normalWorkingDirectoryPathField.setValue(settings.plan.workDir != null ? new File(settings.plan.workDir) : null);
				normalExePathField.setValue(settings.plan.clientName != null ? new File(settings.plan.clientName) : null);
				setupFileNameField.setValue(settings.plan.setupName);
				serverFileNameField.setValue(settings.plan.serverName);
				commandLineField.setValue(settings.plan.commandLine);
				break;
			case DOSBOX:
				normalWorkingDirectoryPathField.setValue(settings.plan.workDir != null ? new File(settings.plan.workDir) : null);
				dosboxPathField.setValue(settings.plan.packageName != null ? new File(settings.plan.packageName) : null);
				packageCommandLineField.setValue(settings.plan.packageCommandLine);
				normalExePathField.setValue(settings.plan.clientName != null ? new File(settings.plan.clientName) : null);
				setupFileNameField.setValue(settings.plan.setupName);
				serverFileNameField.setValue(settings.plan.serverName);
				commandLineField.setValue(settings.plan.commandLine);
				break;
			case APPIMAGE:
				normalWorkingDirectoryPathField.setValue(settings.plan.workDir != null ? new File(settings.plan.workDir) : null);
				packageField.setValue(settings.plan.packageName);
				packageCommandLineField.setValue(settings.plan.packageCommandLine);
				clientFileNameField.setValue(settings.plan.clientName);
				setupFileNameField.setValue(settings.plan.setupName);
				serverFileNameField.setValue(settings.plan.serverName);
				commandLineField.setValue(settings.plan.commandLine);
				break;
			case FLATPAK:
				normalWorkingDirectoryPathField.setValue(settings.plan.workDir != null ? new File(settings.plan.workDir) : null);
				packageField.setValue(settings.plan.packageName);
				packageCommandLineField.setValue(settings.plan.packageCommandLine);
				clientFileNameField.setValue(settings.plan.clientName);
				setupFileNameField.setValue(settings.plan.setupName);
				serverFileNameField.setValue(settings.plan.serverName);
				commandLineField.setValue(settings.plan.commandLine);
				break;
		}
		
	}
	
	private void clearFields()
	{
		normalWorkingDirectoryPathField.setValue(null);
		normalExePathField.setValue(null);
		dosboxPathField.setValue(null);
		packageField.setValue(null);
		packageCommandLineField.setValue("");
		clientFileNameField.setValue(null);
		setupFileNameField.setValue(null);
		serverFileNameField.setValue(null);
		commandLineField.setValue("");
		iwadSwitchField.setValue(null);
		fileSwitchField.setValue(null);
		dehackedSwitchField.setValue(null);
		dehlumpSwitchField.setValue(null);
		saveDirectorySwitchField.setValue(null);
		screenshotDirectorySwitchField.setValue(null);
		saveGameRegexField.setValue(null);
		screenshotRegexField.setValue(null);
		demoRegexField.setValue(null);
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
