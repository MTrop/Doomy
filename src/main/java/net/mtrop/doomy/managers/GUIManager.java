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
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.FileChooserFactory.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.gui.swing.TextOutputPanel;
import net.mtrop.doomy.struct.InstancedFuture;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.swing.ClipboardUtils;
import net.mtrop.doomy.struct.swing.FileChooserFactory;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.EnumUtils;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.IOUtils;

public class GUIManager 
{
	public static final String PROPERTY_THEMENAME = "gui.theme";
	public static final String PATH_LAST_SAVE = "lastsave.file";
	public static final String PATH_CHOOSER_DEFAULT = "default.path";
	
	/**
	 * Supported GUI Themes
	 */
	public enum GUIThemeType
	{
		LIGHT("com.formdev.flatlaf.FlatLightLaf"),
		DARK("com.formdev.flatlaf.FlatDarkLaf"),
		INTELLIJ("com.formdev.flatlaf.FlatIntelliJLaf"),
		DARCULA("com.formdev.flatlaf.FlatDarculaLaf");
		
		public static final Map<String, GUIThemeType> MAP = EnumUtils.createCaseInsensitiveNameMap(GUIThemeType.class);
		
		public final String className;
		
		private GUIThemeType(String className)
		{
			this.className = className;
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

	private final LanguageManager language;
	private final ImageManager images;

	private Properties properties;
	private List<Image> windowIcons;
	private Icon windowIcon;
	
	private GUIManager()
	{
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
	 * Sets the last file saved.
	 * @param path the last file saved.
	 */
	public void setLastFileSave(File path) 
	{
		properties.setProperty(PATH_LAST_SAVE, path.getPath());
		commit();
	}

	/**
	 * @return the last file saved.
	 */
	public File getLastFileSave() 
	{
		String prop = properties.getProperty(PATH_LAST_SAVE);
		return prop != null ? new File(prop) : null;
	}

	/**
	 * Sets the file chooser default path, if no good paths.
	 * @param path the last file saved.
	 */
	public void setFileChooserDefault(File path) 
	{
		properties.setProperty(PATH_CHOOSER_DEFAULT, path.getPath());
		commit();
	}

	/**
	 * @return the file chooser default path.
	 */
	public File getFileChooserDefault() 
	{
		String prop = properties.getProperty(PATH_CHOOSER_DEFAULT);
		return prop != null ? new File(prop) : null;
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
		return fileExtensionFilter(language.getText("filefilter.archives.doom") + "(*.wad, *.pk3, *.pk7, *.pk3, *.zip)", "wad", "pk3", "pk7", "pk3", "zip");
	}
	
	/**
	 * @return the text file filter.
	 */
	public FileFilter createTextFileFilter()
	{
		return fileExtensionFilter(language.getText("filefilter.textfiles") + " (*.txt)", "txt");
	}

	/**
	 * @return the executable file filter.
	 */
	public FileFilter createExecutableFilter()
	{
		return fileFilter(language.getText("filefilter.executables"), (f) -> f.canExecute());
	}

	/**
	 * @return the directory file filter.
	 */
	public FileFilter createDirectoryFilter()
	{
		return fileDirectoryFilter(language.getText("filefilter.directories"));
	}

	/**
	 * @return the all files file filter.
	 */
	public FileFilter createAllFilesFilter()
	{
		return fileFilter(language.getText("filefilter.allfiles"), (f) -> true);
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
		if ((selected = FileChooserFactory.chooseFile(parent, title, lastPath, approveText, transformFileFunction, choosableFilters)) != null)
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
		if ((selected = FileChooserFactory.chooseDirectory(parent, title, lastPath, approveText, createDirectoryFilter())) != null)
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
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFileOrDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		File lastPath = lastPathSupplier.get();
		if (lastPath == null)
			lastPath = getFileChooserDefault();
		
		File selected;
		if ((selected = FileChooserFactory.chooseFileOrDirectory(parent, title, lastPath, approveText, transformFileFunction, choosableFilters)) != null)
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
	public File chooseFileOrDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, FileFilter ... choosableFilters)
	{
		return chooseFileOrDirectory(parent, title, approveText, lastPathSupplier, lastPathSaver, (x, file) -> file, choosableFilters);
	}

	/**
	 * Brings up the file chooser to select multiple files, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File[] chooseFilesOrDirectories(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, FileFilter ... choosableFilters)
	{
		File lastPath = lastPathSupplier.get();
		if (lastPath == null)
			lastPath = getFileChooserDefault();
		
		File[] selected;
		if ((selected = FileChooserFactory.chooseFilesOrDirectories(parent, title, lastPath, approveText, choosableFilters)) != null)
			lastPathSaver.accept(selected[selected.length - 1]);
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

	/**
	 * Creates a process modal, prepped to start and open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		return createProcessModal(parent, title, inFile, new TextOutputPanel(), false, modalOutFunction);
	}
	
	/**
	 * Creates a process modal, prepped to start and selectively open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param dontOpen if set, the modal is not opened.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final boolean dontOpen,
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		return createProcessModal(parent, title, inFile, new TextOutputPanel(), dontOpen, modalOutFunction);
	}
	
	/**
	 * Creates a process modal, prepped to start and selectively open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param outputPanel the output panel to send out and err output to.
	 * @param dontOpen if set, the modal is not opened.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final TextOutputPanel outputPanel,
		final boolean dontOpen,
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		final JLabel statusLabel = label("");
		
		// Show output.
		statusLabel.setText("Running process...");
		
		final Modal<Void> outputModal;
		
		if (!dontOpen)
		{
			outputModal = modal(
				parent, 
				title,
				containerOf(borderLayout(0, 4),
					node(BorderLayout.CENTER, scroll(ScrollPolicy.AS_NEEDED, outputPanel)),
					node(BorderLayout.SOUTH, containerOf(
						node(BorderLayout.WEST, statusLabel),
						node(BorderLayout.EAST, containerOf(flowLayout(Flow.RIGHT, 4, 0),
							node(button(language.getText("clipboard.copy"), (b) -> {
								copyToClipboard(outputPanel.getText());
								statusLabel.setText(language.getText("clipboard.copy.message"));
							})),
							node(button(language.getText("clipboard.save"), (b) -> {
								if (saveToFile(outputPanel, outputPanel.getText()))
									statusLabel.setText(language.getText("doomtools.clipboard.save.message"));
							}))
						))
					))
				)
			);
		}
		else
		{
			outputModal = null;
		}
		
		return new ProcessModal() 
		{
			@Override
			public void start(TaskManager tasks, final Runnable onStart, final Runnable onEnd) 
			{
				final PrintStream outStream = outputPanel.getPrintStream();
				final PrintStream errorStream = outputPanel.getErrorPrintStream();
				tasks.spawn(() -> 
				{
					if (onStart != null)
						onStart.run();
					
					try (InputStream stdin = inFile != null ? new FileInputStream(inFile) : IOUtils.getNullInputStream()) 
					{
						InstancedFuture<Integer> runInstance = modalOutFunction.apply(outStream, errorStream, stdin);
						Integer result = runInstance.result();
						if (result == 0)
							statusLabel.setText(language.getText("process.success"));
						else
							statusLabel.setText(language.getText("process.error", String.valueOf(result)));
					} 
					catch (FileNotFoundException e) 
					{
						statusLabel.setText(language.getText("process.error", "Standard In file not found: " + inFile.getPath()));
					} 
					catch (IOException e) 
					{
						statusLabel.setText(language.getText("process.error", e.getLocalizedMessage()));
					}
					
					if (onEnd != null)
						onEnd.run();
				});
				if (!dontOpen)
					outputModal.openThenDispose();
			}
		};
	}
	
	private void copyToClipboard(String text)
	{
		ClipboardUtils.sendStringToClipboard(text);
	}
	
	private boolean saveToFile(Component parent, String text)
	{
		FileFilter filter = createTextFileFilter();
		File saveFile = chooseFile(parent, 
			language.getText("clipboard.save.title"),
			language.getText("clipboard.save.choose"),
			this::getLastFileSave, 
			this::setLastFileSave,
			(f, input) -> (f == filter ? FileUtils.addMissingExtension(input, "txt") : input),
			filter
		);
		
		if (saveFile == null)
			return false;
		
		try (FileOutputStream fos = new FileOutputStream(saveFile); ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes()))
		{
			IOUtils.relay(bis, fos);
		} 
		catch (IOException e) 
		{
			SwingUtils.error(parent, language.getText("clipboard.save.error", e.getLocalizedMessage()));
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(parent, language.getText("clipboard.save.security"));
		}
		
		return true;
	}
	
	/**
	 * Process modal.
	 */
	@FunctionalInterface
	public interface ProcessModal
	{
		/**
		 * Starts the task and opens the modal.
		 * @param tasks
		 * @param onStart called on start.
		 * @param onEnd called on end.
		 */
		default void start(TaskManager tasks)
		{
			start(tasks, null, null);
		}

		/**
		 * Starts the task and opens the modal.
		 * @param tasks
		 * @param onStart called on start.
		 * @param onEnd called on end.
		 */
		void start(TaskManager tasks, Runnable onStart, Runnable onEnd);
	}
	
	/**
	 * Process creation function.
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <R>
	 */
	@FunctionalInterface
	public interface TriFunction<T1, T2, T3, R>
	{
		/**
		 * Applies this function.
		 * @param t1
		 * @param t2
		 * @param t3
		 * @return the result.
		 */
		R apply(T1 t1, T2 t2, T3 t3);
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
