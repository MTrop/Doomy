/*******************************************************************************
 * Copyright (c) 2020-2025 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.struct.swing;

import java.awt.Component;
import java.io.File;
import java.util.function.BiFunction;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;

/**
 * A factory that creates system file chooser dialogs.
 * @author Matthew Tropiano
 */
public final class SystemFileChooserFactory
{
	private static final BiFunction<FileFilter, File, File> NO_CHANGE_TRANSFORM = (x0, file) -> file;
	
	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param initPath the initial path for the directory chooser.
	 * @param approveText the text to put on the approval button.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File chooseDirectory(Component parent, String title, File initPath, String approveText)
	{
		SystemFileChooser jfc = new SystemFileChooser();
		jfc.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
		if (initPath != null)
		{
			if (initPath.isDirectory())
				jfc.setCurrentDirectory(initPath);
			else
				jfc.setSelectedFile(initPath);
		}
		if (title != null)
			jfc.setDialogTitle(title);
		jfc.resetChoosableFileFilters();
		switch (jfc.showDialog(parent, approveText))
		{
			default:
			case SystemFileChooser.CANCEL_OPTION: 
				return null;
			case SystemFileChooser.APPROVE_OPTION:
				return jfc.getSelectedFile();
		}
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File chooseDirectory(Component parent, String title, String approveText)
	{
		return chooseDirectory(parent, title, null, approveText);
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param initPath the initial path for the directory chooser.
	 * @param approveText the text to put on the approval button.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File chooseDirectory(Component parent, File initPath, String approveText)
	{
		return chooseDirectory(parent, null, initPath, approveText);
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param approveText the text to put on the approval button.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File chooseDirectory(Component parent, String approveText)
	{
		return chooseDirectory(parent, null, null, approveText);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String title, File initPath, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		SystemFileChooser jfc = new SystemFileChooser();
		jfc.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		if (initPath != null)
		{
			if (initPath.isDirectory())
				jfc.setCurrentDirectory(initPath);
			else
				jfc.setSelectedFile(initPath);
		}
		if (title != null)
			jfc.setDialogTitle(title);
		jfc.resetChoosableFileFilters();
		for (FileFilter filter : choosableFilters)
		{
			jfc.addChoosableFileFilter(filter);
			jfc.setFileFilter(filter);
		}
		switch (jfc.showDialog(parent, approveText))
		{
			default:
			case SystemFileChooser.CANCEL_OPTION: 
				return null;
			case SystemFileChooser.APPROVE_OPTION:
				return transformFileFunction.apply(jfc.getFileFilter(), jfc.getSelectedFile());
		}
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String title, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, title, null, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, File initPath, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, null, initPath, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, null, null, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String title, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, title, initPath, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String title, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, title, null, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, null, initPath, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(Component parent, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, null, null, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, File initPath, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, initPath, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, null, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, null, null, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, initPath, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, null, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(String title, FileFilter ... choosableFilters)
	{
		return chooseFile(null, title, null, null, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(File initPath, String approveText, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(null, null, initPath, approveText, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return chooseFile(null, null, initPath, approveText, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		return chooseFile(null, null, null, null, transformFileFunction, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File chooseFile(FileFilter ... choosableFilters)
	{
		return chooseFile(null, null, null, null, NO_CHANGE_TRANSFORM, choosableFilters);
	}

	/**
	 * Creates a file extension filter for file dialogs.
	 * @param description the description of the filter.
	 * @param extensions the qualifying extensions.
	 * @return the new filter.
	 */
	public static FileFilter fileExtensionFilter(final String description, final String ... extensions)
	{
		return new SystemFileChooser.FileNameExtensionFilter(description, extensions);
	}
	
}
