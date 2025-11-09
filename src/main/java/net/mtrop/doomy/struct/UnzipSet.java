/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.struct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.IOUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

/**
 * An unzip set that manages the files unzipped from it.
 * This set is {@link AutoCloseable} - when closed, this will also cal
 * @author Matthew Tropiano
 */
public class UnzipSet implements AutoCloseable
{
	/**
	 * Filter for zip entries.
	 */
	@FunctionalInterface
	public static interface ZipEntryFilter extends Predicate<ZipEntry>
	{
		/**
		 * Checks this Zip entry for passing the filter.
		 * @param entry the entry to test.
		 * @return true if passes, false if not.
		 */
		boolean test(ZipEntry entry);
	}
	
	/**
	 * Reader for zip entries.
	 */
	@FunctionalInterface
	public static interface ZipEntryReader
	{
		/**
		 * Reads a zip entry.
		 * @param entry the entry being read.
		 * @param in the input stream to read the entry with.
		 * @throws IOException if an I/O error occurs.
		 */
		void read(ZipEntry entry, InputStream in) throws IOException;
	}
	
	/** Zip file. */
	private ZipFile zipFile;
	/** Output directory. */
	private File outputDirectory;
	/** Unzipped files. */
	private Set<File> unzippedFiles;
	
	/**
	 * Creates a writer for outputting Zip contents into a file.
	 * @param outputFile the output file.
	 * @return a new reader.
	 */
	public static ZipEntryReader createFileWriter(final File outputFile)
	{
		return (e, in) -> 
		{
			try (FileOutputStream fos = new FileOutputStream(outputFile))
			{
				IOUtils.relay(in, fos, 8192);
			}
		};
	}
	
	/**
	 * Creates a new UnzipSet.
	 * @param zipFile the file to open as a Zip file.
	 * @param outputDirectory the output directory for unzipped files.
	 * @throws IOException if the file cannot be read.
	 * @throws ZipException if the provided file is not a zip file.
	 * @throws SecurityException if you have no permission to open the Zip file.
	 */
	public UnzipSet(File zipFile, File outputDirectory) throws ZipException, IOException
	{
		this.zipFile = new ZipFile(zipFile);
		this.outputDirectory = outputDirectory;
		this.unzippedFiles = new HashSet<>();
	}
	
	/**
	 * Reads an entry, auto-closing the input stream.
	 * @param entry the zip entry.
	 * @param zereader the reader for reading the entry data.
	 * @throws IOException on a read error or the exception thrown from the provided reader.
	 */
	public void read(ZipEntry entry, ZipEntryReader zereader) throws IOException
	{
		try (InputStream in = zipFile.getInputStream(entry))
		{
			zereader.read(entry, in);
		}
	}
	
	/**
	 * Unzips a file to the target directory.
	 * @param entry the zip entry.
	 * @return the unzipped file.
	 * @throws IOException on a read/write error.
	 * @throws SecurityException if you have no permission to write to the destination.
	 */
	public File unzip(ZipEntry entry) throws IOException
	{
		final File outputFile = new File(outputDirectory.getPath() + File.separator + entry.getName());
		if (!FileUtils.createPathForFile(outputFile))
			throw new IOException("Could not create path for file: " + outputFile.getPath());
		read(entry, createFileWriter(outputFile));
		unzippedFiles.add(outputFile);
		return outputFile;
	}

	/**
	 * Unzips all of the entries that match a {@link ZipEntryFilter}.
	 * @param filter the filter for the accepted entries.
	 * @return the unzipped files.
	 * @throws IOException on a read/write error.
	 * @throws SecurityException if you have no permission to write to the destination.
	 */
	public List<File> unzipAll(ZipEntryFilter filter) throws IOException
	{
		List<File> unzipped = new LinkedList<>();
		for (ZipEntry entry : ObjectUtils.enumerationToIterable(zipFile.entries()))
			if (filter.test(entry))
				unzipped.add(unzip(entry));
		return unzipped;
	}

	/**
	 * Attempts to delete all of the unzipped files in this set.
	 * Each successfully deleted file is removed from the internal set of files.
	 * @return the amount of files deleted.
	 */
	public int cleanUp()
	{
		int out = 0;
		Set<File> deleted = new HashSet<>();
		for (File f : unzippedFiles)
		{
			if (f.delete())
			{
				deleted.add(f);
				out++;
			}
		}
		unzippedFiles.removeAll(deleted);
		return out;
	}

	@Override
	public void close() throws IOException
	{
		zipFile.close();
	}
	
}
