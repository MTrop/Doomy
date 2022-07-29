package net.mtrop.doomy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.mtrop.doomy.struct.ObjectUtils;

/**
 * Common class.
 * @author Matthew Tropiano
 */
public final class DoomyCommon
{

	/** Version number. */
	private static Map<String, String> VERSION_MAP = new HashMap<>();

	/**
	 * Gets the embedded version string for a tool name.
	 * If there is no embedded version, this returns "SNAPSHOT".
	 * @param name the name of the tool. 
	 * @return the version string or "SNAPSHOT"
	 */
	public static String getVersionString(String name)
	{
		if (VERSION_MAP.containsKey(name))
			return VERSION_MAP.get(name);
		
		String out = null;
		try (InputStream in = openResource("net/mtrop/doomy/" + name + ".version")) {
			if (in != null)
				VERSION_MAP.put(name, out = getTextualContents(in, "UTF-8").trim());
		} catch (IOException e) {
			/* Do nothing. */
		}
		
		return out != null ? out : "SNAPSHOT";
	}

	/**
	 * Opens an {@link InputStream} to a resource using the current thread's {@link ClassLoader}.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	private static InputStream openResource(String pathString)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
	}

	/**
	 * Retrieves the textual contents of a stream.
	 * @param in the input stream to use.
	 * @param encoding name of the encoding type.
	 * @return a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException if the read cannot be done.
	 */
	private static String getTextualContents(InputStream in, String encoding) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, encoding));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Checks if the input is not null.
	 * @param object the input.
	 * @throws IllegalArgumentException if object is null.
	 */
	public static void checkNotNull(Object object)
	{
		if (object == null)
			throw new IllegalArgumentException("Object should not have been null.");
	}
	
	/**
	 * Checks if the input is not empty.
	 * @param object the input.
	 * @throws IllegalArgumentException if object is empty.
	 */
	public static void checkNotEmpty(Object object)
	{
		if (ObjectUtils.isEmpty(object))
			throw new IllegalArgumentException("Object should not have been empty.");
	}

	/**
	 * Prints the splash.
	 * @param handler the handler to use. 
	 * @param version the version string to print.
	 */
	public static void splash(IOHandler handler, String version)
	{
		handler.outln("Doomy v" + version + " by Matt Tropiano");
	}

	/**
	 * Scans a directory of files and calls the provided consumer per file.
	 * @param startDir the starting directory.
	 * @param recurse if true, recurse through subdirectories.
	 * @param filter the file filter for accepted files.
	 * @param onFile the consumer to call per accepted file.
	 */
	public static void scanAndListen(File startDir, boolean recurse, FileFilter filter, Consumer<File> onFile)
	{
		Deque<File> fileQueue = new LinkedList<>();
		
		for (File f : startDir.listFiles(filter))
			fileQueue.add(f);

		while (!fileQueue.isEmpty())
		{
			File file = fileQueue.pollFirst();
			if (file.isDirectory())
			{
				if (recurse)
				{
					for (File f : file.listFiles(filter))
						fileQueue.add(f);
				}
				continue;
			}
			
			onFile.accept(file);
		}
	}

	/**
	 * Prints the usage.
	 * @param handler the handler to use.
	 */
	public static void usage(IOHandler handler)
	{
		handler.outln("Usage: doomy version");
		handler.outln("       doomy help");
		handler.outln("       doomy config");
		handler.outln("       doomy engine");
	}

	/**
	 * Prints out to a fixed-width medium, line wrapping at word breaks.
	 * @param handler the handler to use.
	 * @param width the width of the line in characters.
	 * @param indent the offset of the subsequent lines after the first break.
	 * @param message the output message.
	 */
	public static void wrapPrint(IOHandler handler, final int width, final int indent, Object message)
	{
		final String output = String.valueOf(message);
		final StringBuilder line = new StringBuilder();
		final StringBuilder word = new StringBuilder();
		final AtomicBoolean wordFlushed = new AtomicBoolean(false);
		
		final Runnable LINE_FLUSH = () -> 
		{
			handler.outln(line.toString());
			line.delete(0, line.length());
			int x = indent;
			while (x-- > 0)
				line.append(' ');
			wordFlushed.set(false);
		};

		final Runnable WORD_FLUSH = () -> 
		{
			if (word.length() + line.length() > width + 1)
				LINE_FLUSH.run();
			line.append(word.toString());
			word.delete(0, word.length());
			wordFlushed.set(true);
		};
		
		int i = 0;
		while (i < output.length())
		{
			char c = output.charAt(i++);
			
			if (c == '\n')
				LINE_FLUSH.run();
			else if (Character.isWhitespace(c))
			{
				WORD_FLUSH.run();
				line.append(' ');
			}
			else 
			{
				if (word.length() + line.length() + 1 >= width)
					LINE_FLUSH.run();
				word.append(c);
				if (c == '-')
					WORD_FLUSH.run();
			}
		}

		if (word.length() > 0)
			WORD_FLUSH.run();
		
		if (line.length() > 0)
			LINE_FLUSH.run();
	}
	
	/**
	 * Prints the help.
	 * @param handler the handler to use.
	 * @param commandName the command name to filter the output.
	 */
	public static void help(IOHandler handler, String commandName)
	{
	    wrapPrint(handler, 80, 40,     "doomy                                   Print version splash and basic help.");
		handler.outln();
		if (commandName == null || DoomyCommand.VERSION.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  version                               Print version.");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.HELP.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  help                                  Print all of this help.");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.CONFIG.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  config                                Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    list                                Print Doomy's settings.");
			wrapPrint(handler, 80, 45, "      [phrase]                            ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "    get [name]                          Prints the value of a Doomy setting called [name].");
			wrapPrint(handler, 80, 40, "    set [name] [value]                  Sets the value of a Doomy setting called [name] to [value].");
			wrapPrint(handler, 80, 40, "    remove [name]                       Removes a Doomy setting called [name].");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.ENGINE.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  engine                                Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    setup [name]                        Runs the engine's setup/settings program (if present).");
			wrapPrint(handler, 80, 40, "    add [name]                          Create a new engine profile named [name].");
			wrapPrint(handler, 80, 45, "      [template]                          ...by copying a template named [template].");
			wrapPrint(handler, 80, 40, "    copy [name] [engine]                Create a new engine profile named [name] by copying an engine named [engine].");
			wrapPrint(handler, 80, 40, "    remove [name]                       Remove an engine template named [name] (and confirm).");
			wrapPrint(handler, 80, 45, "      --quiet, -q                         ...and skip confirm.");
			wrapPrint(handler, 80, 40, "    rename [name1] [name2]              Changes an engine's name from [name1] to [name2].");
			wrapPrint(handler, 80, 40, "    list                                List all stored engine profiles.");
			wrapPrint(handler, 80, 45, "      [phrase]                            ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "    config");
			wrapPrint(handler, 80, 40, "      list [name]                       Print engine profile [name]'s settings.");
			wrapPrint(handler, 80, 45, "        [phrase]                          ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "      get [name] [setting]              Prints the value of setting called [setting] on the engine profile named [name].");
			wrapPrint(handler, 80, 40, "      set [name] [setting] [value]      Sets the value of setting called [setting] to [value] on the engine profile named [name].");
			wrapPrint(handler, 80, 40, "      remove [name] [setting]           Removes an engine template setting called [setting].");
			wrapPrint(handler, 80, 40, "    template");
			wrapPrint(handler, 80, 40, "      list                              List all stored engine templates.");
			wrapPrint(handler, 80, 45, "        [phrase]                          ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "      add [name]                        Create a new engine profile template named [name].");
			wrapPrint(handler, 80, 45, "        [template]                        ...by copying a template named [template].");
			wrapPrint(handler, 80, 40, "      remove [name]                     Remove the engine profile template named [name].");
			wrapPrint(handler, 80, 45, "        --quiet, -q                       ...and skip confirm.");
			wrapPrint(handler, 80, 40, "      config");
			wrapPrint(handler, 80, 40, "        list [name]                     Print engine profile [name]'s settings.");
			wrapPrint(handler, 80, 45, "          [phrase]                        ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "        get [name] [setting]            Prints the value of setting called [setting] on the engine profile named [name].");
			wrapPrint(handler, 80, 40, "        set [name] [setting] [value]    Sets the value of setting called [setting] to [value] on the engine profile named [name].");
			wrapPrint(handler, 80, 40, "        remove [name] [setting]         Removes an engine template setting called [setting].");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.IWAD.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  iwad                                  Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    list                                List all known IWADs.");
			wrapPrint(handler, 80, 45, "      [phrase]                            ...that contain [phrase].");
			wrapPrint(handler, 80, 40, "    add [name] [path]                   Add an IWAD by name [name] and its path at [path].");
			wrapPrint(handler, 80, 40, "    remove [name]                       Remove an IWAD by name.");
			wrapPrint(handler, 80, 45, "      --quiet, -q                         ...and skip confirm.");
			wrapPrint(handler, 80, 40, "    clean                               Removes all IWADs with obsolete/missing paths.");
			wrapPrint(handler, 80, 45, "      --quiet, -q                         ...and skip confirm.");
			wrapPrint(handler, 80, 40, "    rename [name1] [name2]              Changes an alias from IWAD [name1] to [name2].");
			wrapPrint(handler, 80, 40, "    get [name]                          Print the path of the IWAD named [name].");
			wrapPrint(handler, 80, 40, "    set [name] [path]                   Change the path of the IWAD named [name] to [path].");
			wrapPrint(handler, 80, 40, "    scan [path]                         Adds all IWADs in directory [path] (can be WAD/PK3/PKE/PK7/IPK3/IPK7/IWAD). Filename becomes [name].");
			wrapPrint(handler, 80, 45, "      --recurse, -r                       ...and search recursively from [path].");
			wrapPrint(handler, 80, 45, "      --prefix, -p [string]               ...and prepend [string] to each WAD entry name.");
			wrapPrint(handler, 80, 45, "      --force-add-existing                ...and update the names of known paths (otherwise, this does not add existing).");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.WAD.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  wad                                   Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    list                                List cached/downloaded WADs.");
			wrapPrint(handler, 80, 45, "      [phrase]                            ...that contains [phrase] (wildcard is *).");
			wrapPrint(handler, 80, 40, "    add [name] [path]                   Add a WAD alias for a WAD named [name] for [path] (can be a zip archive).");
			wrapPrint(handler, 80, 40, "    remove [name]                       Remove a WAD alias for a WAD named [name] for [path].");
			wrapPrint(handler, 80, 45, "      --quiet, -q                         ...and skip confirm.");
			wrapPrint(handler, 80, 45, "      --file                              ...and also remove the stored file.");
			wrapPrint(handler, 80, 40, "    clean                               Removes all WADs with obsolete/missing paths.");
			wrapPrint(handler, 80, 45, "      --quiet, -q                         ...and skip confirm.");
			wrapPrint(handler, 80, 40, "    rename [name1] [name2]              Changes an alias from WAD [name1] to [name2].");
			wrapPrint(handler, 80, 40, "    get [name]                          Print the path of the WAD named [name].");
			wrapPrint(handler, 80, 40, "    set [name] [path]                   Change the path of the WAD named [name] to [path].");
			wrapPrint(handler, 80, 40, "    scan [path]                         Adds all WADs in directory [path] (can be WAD/PKE/PK3/PK7/ZIPs). Filename becomes [name].");
			wrapPrint(handler, 80, 45, "      --recurse, -r                       ...and search recursively from [path].");
			wrapPrint(handler, 80, 45, "      --prefix, -p [string]               ...and prepend [string] to each WAD entry name.");
			wrapPrint(handler, 80, 45, "      --force-add-existing                ...and update the names of known paths (otherwise, this does not add existing).");
			wrapPrint(handler, 80, 40, "    text [name]                         Dumps the WAD's text entry, if any (searches same directory for FILENAME.TXT or inside archive).");
			//wrapPrint(handler, 80, 40, "    download [url] [name]               Downloads a WAD to the main download directory as its path from [url] and adds it as its source under WAD [name].");
			wrapPrint(handler, 80, 40, "    redownload [name]                   Redownload a downloaded WAD named [name]."); 
			wrapPrint(handler, 80, 40, "    source                              Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "      list                              List all URL sources.");
			wrapPrint(handler, 80, 45, "        [phrase]                          ...whose WAD name contains [phrase].");
			wrapPrint(handler, 80, 45, "          --blank, -b                     ...that aren't bound to URLs, instead.");
			wrapPrint(handler, 80, 40, "      get [name]                        Prints the URL source of a downloaded WAD named [name].");
			wrapPrint(handler, 80, 40, "      set [name] [url]                  Sets the URL source of a downloaded WAD named [name] to [url].");
			wrapPrint(handler, 80, 40, "      remove [name]                     Removes the URL source of a downloaded WAD named [name].");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.PRESET.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  preset                                Print this subsection's help and terminate."); 
			wrapPrint(handler, 80, 40, "    list                                List all presets.");
			wrapPrint(handler, 80, 45, "      [phrase]                            ...whose name/hash contains [phrase].");
			wrapPrint(handler, 80, 40, "    rename [hash] [name]                Sets name [name] on a preset with hash [hash].");
			wrapPrint(handler, 80, 40, "    create [engine]                     Create preset, but don't run engine.");
			wrapPrint(handler, 80, 45, "      --wads, -w [wads...]                ...with these WADs (by name, dependency-expanded).");
			wrapPrint(handler, 80, 45, "      --iwad, -i [iwad]                   ...with IWAD name [iwad] (may not be required if specified in engine settings).");
			wrapPrint(handler, 80, 45, "      --name, -n [name]                   ...set to name [name].");
			wrapPrint(handler, 80, 40, "    remove                              Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "      [name]                            Remove preset named [name] (takes precedence, must confirm).");
			wrapPrint(handler, 80, 45, "        --quiet, -q                       ...and skip confirm.");
			wrapPrint(handler, 80, 40, "      [hash]                            Remove preset by hash [hash] (must be full, must confirm).");
			wrapPrint(handler, 80, 45, "        --quiet, -q                       ...and skip confirm.");
			wrapPrint(handler, 80, 40, "    info                                Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "      [name]                            Get preset named [name] and list info (takes precedence).");
			wrapPrint(handler, 80, 40, "      [hash]                            Get preset hash [hash] and list info (can be starting partial, if unique enough).");
			wrapPrint(handler, 80, 40, "    run                                 Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "      [name]                            Run preset named [name] (takes precedence).");
			wrapPrint(handler, 80, 40, "      [hash]                            Run preset hash [hash] (can be starting partial, if unique enough).");
			wrapPrint(handler, 80, 40, "        --                              Send args verbatim after this token.");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.RUN.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  run [engine]                          Run engine [engine]");
			wrapPrint(handler, 80, 45, "    --wads, -w [wads...]                  ...with these WADs (by name, dependency-expanded).");
			wrapPrint(handler, 80, 45, "    --iwad, -i [iwad]                     ...with IWAD name [iwad] (may not be required if specified in engine settings).");
			wrapPrint(handler, 80, 45, "    --name, -n [name]                     ...and assign the created preset [name].");
			wrapPrint(handler, 80, 40, "    --                                  Send args verbatim after this token.");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.IDGAMES.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  idgames                               Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    ping                                Ping service.");
			wrapPrint(handler, 80, 40, "    about                               Display API \"About.\"");
			wrapPrint(handler, 80, 40, "    comic                               Return a Doom Comic quote (yes, it's a real call).");
			wrapPrint(handler, 80, 40, "    search                              Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "      file [phrase]                     Searches for filename named [phrase], return best matches up to amount.");
			wrapPrint(handler, 80, 40, "        --name, -n [name]               Specify new name, if downloaded.");
			wrapPrint(handler, 80, 40, "        --limit, -l [amount]            Set max results to [amount].");
			wrapPrint(handler, 80, 40, "        --download, -d [result]         Download the file at result position [result].");
			wrapPrint(handler, 80, 40, "        --text, -t [result]             Print text file of result [result].");
			wrapPrint(handler, 80, 40, "      title [phrase]                    Searches for title containing [phrase], return best matches up to amount.");
			wrapPrint(handler, 80, 40, "        --name, -n [name]               Specify new name, if downloaded.");
			wrapPrint(handler, 80, 40, "        --limit, -l [amount]            Set max results to [amount].");
			wrapPrint(handler, 80, 40, "        --download, -d [result]         Download the file at result position [result].");
			wrapPrint(handler, 80, 40, "        --text, -t [result]             Print text file of result [result].");
			wrapPrint(handler, 80, 40, "      author [phrase]                   Searches for author containing [phrase], return best matches up to amount.");
			wrapPrint(handler, 80, 40, "        --name, -n [name]               Specify new name, if downloaded.");
			wrapPrint(handler, 80, 40, "        --limit, -l [amount]            Set max results to [amount].");
			wrapPrint(handler, 80, 40, "        --download, -d [result]         Download the file at result position [result].");
			wrapPrint(handler, 80, 40, "        --text, -t [result]             Print text file of result [result].");
			wrapPrint(handler, 80, 40, "      text [phrase]                     Searches using text files containing [phrase], return best matches up to amount.");
			wrapPrint(handler, 80, 40, "        --name, -n [name]               Specify new name, if downloaded.");
			wrapPrint(handler, 80, 40, "        --limit, -l [amount]            Set max results to [amount].");
			wrapPrint(handler, 80, 40, "        --download, -d [result]         Download the file at result position [result].");
			wrapPrint(handler, 80, 40, "        --text, -t [result]             Print text file of result [result].");
			handler.outln();
		}
		if (commandName == null || DoomyCommand.WADARCHIVE.equalsIgnoreCase(commandName))
		{
			wrapPrint(handler, 80, 40, "  wadarchive                            Print this subsection's help and terminate.");
			wrapPrint(handler, 80, 40, "    md5 [hash]                          List results from MD5 file hash [hash].");
			wrapPrint(handler, 80, 40, "      --download, -d [result]           Download the found file from link [result].");
			wrapPrint(handler, 80, 40, "      --name, -n [name]                 Specify new alias, if downloaded.");
			wrapPrint(handler, 80, 40, "    sha1 [hash]                         List results from SHA1 file hash [hash].");
			wrapPrint(handler, 80, 40, "      --download, -d [result]           Download the found file from link [result].");
			wrapPrint(handler, 80, 40, "      --name, -n [name]                 Specify new alias, if downloaded.");
			wrapPrint(handler, 80, 40, "    file [name]                         List results from file [name].");
			wrapPrint(handler, 80, 40, "      --download, -d [result]           Download the found file from link [result].");
			wrapPrint(handler, 80, 40, "      --name, -n [name]                 Specify new alias, if downloaded.");
			handler.outln();
		}
	}
	
}
