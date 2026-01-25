/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.SystemFileChooserFactory.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.SystemFileChooserFactory;
import net.mtrop.doomy.struct.util.EnumUtils;

public class GUIManager 
{
	public static final String PROPERTY_THEMENAME = "gui.theme";
	
	/**
	 * Supported GUI Themes
	 */
	public enum GUIThemeType
	{
		LIGHT("Light", "com.formdev.flatlaf.FlatLightLaf", false),
		DARK("Dark", "com.formdev.flatlaf.FlatDarkLaf", true),
		INTELLIJ("IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf", false),
		DARCULA("Darcula", "com.formdev.flatlaf.FlatDarculaLaf", true),
		MACOSLIGHT("macOS Light", "com.formdev.flatlaf.themes.FlatMacLightLaf", false),
		MACOSDARK("macOS Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf", true),
		ARC("Arc", "com.formdev.flatlaf.intellijthemes.FlatArcIJTheme", false),
		ARCORANGE("Arc - Orange", "com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme", false),
		ARCDARK("Arc Dark", "com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme", true),
		ARCDARKORANGE("Arc Dark - Orange", "com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme", true),
		CARBON("Carbon", "com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme", true),
		COBALT2("Cobalt 2", "com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme", true),
		CYANLIGHT("Cyan light", "com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme", false),
		DARKFLAT("Dark Flat", "com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme", true),
		DARKPURPLE("Dark purple", "com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme", true),
		DRACULA("Dracula", "com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme", true),
		GRADIENTODARKFUSCHIA("Gradianto Dark Fuchsia", "com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme", true),
		GRADIENTODEEPOCEAN("Gradianto Deep Ocean", "com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme", true),
		GRADIENTOMIDNIGHTBLUE("Gradianto Midnight Blue", "com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme", true),
		GRADIENTONATUREGREEN("Gradianto Nature Green", "com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme", true),
		GRAY("Gray", "com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme", false),
		GRUVBOXDARKHARD("Gruvbox Dark Hard", "com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme", true),
		HIBERBEEDARK("Hiberbee Dark", "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme", true),
		HIGHCONTRAST("High Contrast", "com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme", true),
		LIGHTFLAT("Light Flat", "com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme", false),
		MATERIALDESIGNDARK("Material Design Dark", "com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme", true),
		MONOKAI("Monocai", "com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme", true),
		MONOKAIPRO("Monokai Pro", "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme", true),
		NORD("Nord", "com.formdev.flatlaf.intellijthemes.FlatNordIJTheme", true),
		ONEDARK("One Dark", "com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme", true),
		SOLARIZEDDARK("Solarized Dark", "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme", true),
		SOLARIZEDLIGHT("Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme", false),
		SPACEGRAY("Spacegray", "com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme", true),
		VUESION("Vuesion", "com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme", true),
		XCODEDARK("Xcode-Dark", "com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme", true),
		ARCDARKMATERIAL("Arc Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTArcDarkIJTheme", true),
		ATOMONEDARKMATERIAL("Atom One Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneDarkIJTheme", true),
		ATOMONELIGHTMATERIAL("Atom One Light (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneLightIJTheme", false),
		DRACULAMATERIAL("Dracula (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTDraculaIJTheme", true),
		GITHUBMATERIAL("GitHub (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubIJTheme", false),
		GITHUBDARKMATERIAL("GitHub Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubDarkIJTheme", true),
		LIGHTOWLMATERIAL("Light Owl (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTLightOwlIJTheme", false),
		MATERIALDARKER("Material Darker (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDarkerIJTheme", true),
		MATERIALDEEPOCEAN("Material Deep Ocean (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDeepOceanIJTheme", true),
		MATERIALLIGHTER("Material Lighter (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme", false),
		MATERIALOCEANIC("Material Oceanic (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialOceanicIJTheme", true),
		MATERIALPALEKNIGHT("Material Palenight (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialPalenightIJTheme", true),
		MONOKAIPROMATERIAL("Monokai Pro (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMonokaiProIJTheme", true),
		MOONLIGHTMATERIAL("Moonlight (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMoonlightIJTheme", true),
		NIGHTOWLMATERIAL("Night Owl (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTNightOwlIJTheme", true),
		SOLARIZEDDARKMATERIAL("Solarized Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedDarkIJTheme", true),
		SOLARIZEDLIGHTMATERIAL("Solarized Light (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedLightIJTheme", false),
		;
		
		public static final Map<String, GUIThemeType> MAP = EnumUtils.createCaseInsensitiveNameMap(GUIThemeType.class);
		
		private final String className;
		private final String name;
		private final boolean dark;
		
		private GUIThemeType(String name, String className, boolean dark)
		{
			this.className = className;
			this.name = name;
			this.dark = dark;
		}
		
		public String getClassName()
		{
			return this.className;
		}
		
		public boolean isDark()
		{
			return this.dark;
		}
		
		@Override
		public String toString() 
		{
			return (this.dark ? "(Dark) " : "(Light) ") + this.name;
		}
	}
	
	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<GUIManager> INSTANCE = new SingletonProvider<>(() -> new GUIManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static GUIManager get()
	{
		return INSTANCE.get();
	}

	// =======================================================================

	private final ConfigManager config;
	private final LanguageManager language;
	private final ImageManager images;

	private Properties properties;
	private List<Image> windowIcons;
	private Icon windowIcon;
	
	private GUIManager()
	{
		this.config = ConfigManager.get();
		this.language = LanguageManager.get();
		this.images = ImageManager.get();
		this.properties = new Properties();
		
		final Image icon16  = images.getImage("doomy16.png"); 
		final Image icon32  = images.getImage("doomy32.png"); 
		final Image icon48  = images.getImage("doomy48.png"); 
		final Image icon64  = images.getImage("doomy64.png"); 
		final Image icon96  = images.getImage("doomy96.png"); 
		final Image icon128 = images.getImage("doomy128.png"); 
		final Image icon256 = images.getImage("doomy256.png"); 

		this.windowIcons = Arrays.asList(icon256, icon128, icon96, icon64, icon48, icon32, icon16);
		this.windowIcon = new ImageIcon(icon16);
		
		File propsFile = new File(DoomyEnvironment.getPropertiesPath());
		if (propsFile.exists())
		{
			try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(propsFile), StandardCharsets.UTF_8)))
			{
				properties.load(reader);
			} 
			catch (FileNotFoundException e) 
			{
				SwingUtils.error(e.getLocalizedMessage());
			} 
			catch (IOException e) 
			{
				SwingUtils.error(e.getLocalizedMessage());
			}
		}
	}
	
	private void commit()
	{
		File propsFile = new File(DoomyEnvironment.getPropertiesPath());
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propsFile), StandardCharsets.UTF_8)))
		{
			properties.store(writer, "Written by Doomy");
		} 
		catch (IOException e) 
		{
			SwingUtils.error(e.getLocalizedMessage());
		}
	}
	
	private File getFileChooserDefault() 
	{
		return config.getConvertedValue(ConfigManager.SETTING_FILECHOOSER_DEFAULT_DIR, (value) -> value != null ? new File(value) : null);
	}

	/**
	 * Gets the theme type to use for the GUI theme.
	 * @return the theme type.
	 */
	public GUIThemeType getThemeType()
	{
		String value = properties.getProperty(PROPERTY_THEMENAME);
		if (value == null)
			return GUIThemeType.LIGHT;
		GUIThemeType theme = GUIThemeType.MAP.get(value);
		return theme != null ? theme : GUIThemeType.LIGHT;
	}
	
	/**
	 * Sets the theme type to use for the GUI theme.
	 * @param type the theme type.
	 */
	public void setThemeType(GUIThemeType type)
	{
		properties.setProperty(PROPERTY_THEMENAME, type.name());
		commit();
	}
	
	/**
	 * @return the common window icons to use.
	 */
	public List<Image> getWindowIcons() 
	{
		return windowIcons;
	}
	
	/**
	 * @return the single window icon to use.
	 */
	public Icon getWindowIcon() 
	{
		return windowIcon;
	}
	
	/**
	 * Creates a consistent title panel.
	 * @param title the title.
	 * @param container the enclosed container.
	 * @return a new container wrapped in a titled border.
	 */
	public Container createTitlePanel(String title, Container container)
	{
		Border border = createTitledBorder(
			createLineBorder(Color.GRAY, 1), title, TitledBorder.LEADING, TitledBorder.TOP
		);
		return containerOf(border, 
			node(containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, container)
			))
		);
	}

	/**
	 * @return a new "Doom Archives" filter.
	 */
	public FileFilter createDoomArchivesFilter()
	{
		return fileExtensionFilter(language.getText("filefilter.archives.doom") + "(*.wad, *.pk3, *.pk7, *.pke, *.zip)", "wad", "pk3", "pk7", "pke", "zip");
	}
	
	/**
	 * @return a new "Doom Archives" filter.
	 */
	public FileFilter createDoomIWADArchivesFilter()
	{
		return fileExtensionFilter(language.getText("filefilter.archives.doom.iwad") + "(*.wad, *.iwad, *.pk3, *.ipk3, *.pk7, *.ipk7, *.pke, *.zip)", "wad", "iwad", "ipk3", "pk3", "ipk7", "pk7", "pke", "zip");
	}
	
	/**
	 * @return the text file filter.
	 */
	public FileFilter createTextFileFilter()
	{
		return fileExtensionFilter(language.getText("filefilter.textfiles") + " (*.txt)", "txt");
	}

	/**
	 * Adds a form field to a form, attaching a tool tip, if any.
	 * @param formPanel the form panel.
	 * @param formFields the list of fields to add.
	 * @return the form panel passed in.
	 */
	public JFormPanel createForm(JFormPanel formPanel, FormFieldInfo ... formFields)
	{
		for (int i = 0; i < formFields.length; i++) 
		{
			FormFieldInfo formFieldInfo = formFields[i];
			String label = formFieldInfo.languageKey != null ? language.getText(formFieldInfo.languageKey) : "";
			String tipKey = formFieldInfo.languageKey + ".tip";
			String tip =  language.hasKey(tipKey) ? language.getText(tipKey) : null;
			formPanel.addTipField(label, tip, formFieldInfo.field);
		}
		return formPanel;
	}

	/**
	 * Creates a {@link FormFieldInfo} object for use with {@link #createForm(JFormPanel, FormFieldInfo...)}.
	 * @param languageKey the language key prefix for the label and tooltip.
	 * @param field the field to add.
	 * @return new info.
	 */
	public FormFieldInfo formField(String languageKey, JFormField<?> field)
	{
		return new FormFieldInfo(languageKey, field);
	}

	/**
	 * Creates a {@link FormFieldInfo} object for use with {@link #createForm(JFormPanel, FormFieldInfo...)}.
	 * The label is blank.
	 * @param field the field to add.
	 * @return new info.
	 */
	public FormFieldInfo formField(JFormField<?> field)
	{
		return new FormFieldInfo(null, field);
	}

	/**
	 * @return the default file to use when opening a blank path from a file chooser.
	 */
	public File getDefaultFile()
	{
		return config.getConvertedValue(ConfigManager.SETTING_LASTFILE, (v) -> 
			v != null 
				? new File(v) 
				: config.getConvertedValue(ConfigManager.SETTING_FILECHOOSER_DEFAULT_DIR, (f) -> f != null ? new File(f) : null))
		;
	}
	
	/**
	 * Sets the default file to use.
	 * @param selected the selected file.
	 */
	public void setDefaultFile(File selected)
	{
		config.setValue(ConfigManager.SETTING_LASTFILE, selected != null ? selected.getAbsolutePath() : null);
	}
	
	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFile(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		File lastPath = lastPathSupplier.get();
		if (lastPath == null)
			lastPath = getFileChooserDefault();
		
		File selected;
		if ((selected = SystemFileChooserFactory.chooseFile(parent, title, lastPath, approveText, transformFileFunction, choosableFilters)) != null)
			lastPathSaver.accept(selected);
		return selected;
	}

	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFile(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, title, approveText, lastPathSupplier, lastPathSaver, (x, file) -> file, choosableFilters);
	}

	/**
	 * Brings up the file chooser to select a directory, but on successful selection, returns the directory
	 * and sets the last project path used in settings. Initial file is also the last project directory used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver)
	{
		File lastPath = lastPathSupplier.get();
		if (lastPath == null)
			lastPath = getFileChooserDefault();
		
		File selected;
		if ((selected = SystemFileChooserFactory.chooseDirectory(parent, title, lastPath, approveText)) != null)
			lastPathSaver.accept(selected);
		return selected;
	}

	/**
	 * Creates a menu from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param nodes the additional component nodes.
	 * @return the new menu.
	 */
	public JMenu createMenuFromLanguageKey(String keyPrefix, MenuNode... nodes)
	{
		return menu(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			nodes
		);
	}

	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param nodes the additional component nodes.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, MenuNode... nodes)
	{
		return menuItem(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			nodes
		);
	}

	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, MenuItemClickHandler handler)
	{
		return menuItem(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			handler
		);
	}

	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * Name is taken form the action.
	 * @param keyPrefix the key prefix.
	 * @param action the attached action.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, Action action)
	{
		return menuItem(
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			action
		);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param result the choice result supplier.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix, Supplier<T> result)
	{
		return choice(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			result
		);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param result the choice result.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix, T result)
	{
		return choice(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			result
		);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix)
	{
		return createChoiceFromLanguageKey(keyPrefix, (T)null);
	}

	/* ==================================================================== */
	
	/**
	 * Form field info.
	 */
	public static class FormFieldInfo
	{
		private String languageKey;
		private JFormField<?> field;
		
		private FormFieldInfo(String languageKey, JFormField<?> field)
		{
			this.languageKey = languageKey;
			this.field = field;
		}
	}
	
}
