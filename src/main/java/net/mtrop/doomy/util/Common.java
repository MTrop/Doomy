package net.mtrop.doomy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.struct.ObjectUtils;

/**
 * Common class.
 * @author Matthew Tropiano
 */
public final class Common
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
	 * @throws IllegalArgumentException if object is null.
	 */
	public static void checkNotNull(Object object)
	{
		if (object == null)
			throw new IllegalArgumentException("Object should not have been null.");
	}
	
	/**
	 * Checks if the input is not empty.
	 * @throws IllegalArgumentException if object is empty.
	 */
	public static void checkNotEmpty(Object object)
	{
		if (ObjectUtils.isEmpty(object))
			throw new IllegalArgumentException("Object should not have been empty.");
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	public static void splash(PrintStream out, String version)
	{
		out.println("Doomy v" + version + " by Matt Tropiano");
	}

	/**
	 * Reads a line from an input stream.
	 * @param out the output stream to print the prompt to.
	 * @param in the input stream.
	 * @param prompt the prompt to display.
	 * @return the string read.
	 */
	public static String prompt(PrintStream out, InputStream in, String prompt)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			out.print(prompt + "> ");
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	public static void usage(PrintStream out)
	{
		out.println("Usage: doomy version");
		out.println("       doomy help");
		out.println("       doomy config");
		out.println("       doomy engine");
	}

	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	public static void help(PrintStream out, String commandName)
	{
		out.println("doomy                                  Print version splash and basic help.");
		if (commandName == null || DoomyCommand.VERSION.equalsIgnoreCase(commandName))
		{
			out.println("  version                              Print version splash and terminate.");
		}
		if (commandName == null || DoomyCommand.HELP.equalsIgnoreCase(commandName))
		{
			out.println("  help                                 Print all of this help and terminate.");
		}
		if (commandName == null || DoomyCommand.CONFIG.equalsIgnoreCase(commandName))
		{
			out.println("  config                               Print this subsection's help and terminate.");
			out.println("    list                               Print Doomy's settings.");
			out.println("      [phrase]                           ...that contain [phrase].");
			out.println("    get [name]                         Prints the value of a Doomy setting called [name].");
			out.println("    set [name] [value]                 Sets the value of a Doomy setting called [name] to [value].");
			out.println("    remove [name]                      Removes a Doomy setting called [name].");
		}
		if (commandName == null || DoomyCommand.ENGINE.equalsIgnoreCase(commandName))
		{
			out.println("  engine                               Print this subsection's help and terminate.");
			out.println("    setup [name]                       Runs the engine's setup/settings program (if present).");
			out.println("    add [name] [template]              Create a new engine profile named [name] by copying a template named [template].");
			out.println("    remove [name]                      Remove an engine template named [name] (and confirm).");
			out.println("      --quiet, -q                        ...but skip confirm.");
			out.println("    rename [name1] [name2]             Changes an alias from Engine [name1] to [name2].");
			out.println("    list                               List all stored engine profiles.");
			out.println("      [phrase]                           ...that contain [phrase].");
			out.println("    config [name]");
			out.println("      list                             Print engine profile [name]'s settings.");
			out.println("        [phrase]                         ...that contain [phrase].");
			out.println("      get [setting]                    Prints the value of setting called [setting] on the engine profile named [name].");
			out.println("      set [setting] [value]            Sets the value of setting called [setting] to [value] on the engine profile named [name].");
			out.println("      remove [setting]                 Removes an engine template setting called [setting].");
			out.println("    setup [name]                       Runs the engine's setup/settings program (if present).");
			out.println("    template");
			out.println("      list                             List all stored engine templates.");
			out.println("        [phrase]                         ...that contain [phrase].");
			out.println("      add [name]                       Create a new engine profile template named [name].");
			out.println("        [template]                       ...by copying a template named [template].");
			out.println("      remove [name]                    Remove the engine profile template named [name] (and confirm).");
			out.println("        --quiet, -q                      ...but skip confirm.");
			out.println("      config");
			out.println("        list");
			out.println("          [name]                       Print engine profile [name]'s settings.");
			out.println("            [phrase]                     ...that contain [phrase].");
			out.println("        get [name] [setting]           Prints the value of setting called [setting] on the engine profile named [name].");
			out.println("        set [name] [setting] [value]   Sets the value of setting called [setting] to [value] on the engine profile named [name].");
			out.println("        remove [name] [setting]        Removes an engine template setting called [setting].");
		}
		if (commandName == null || DoomyCommand.IWAD.equalsIgnoreCase(commandName))
		{
			out.println("  iwad                                 Print this subsection's help and terminate.");
			out.println("    list                               List all known IWADs.");
			out.println("      [phrase]                           ...that contain [phrase].");
			out.println("    add [name] [path]                  Add an IWAD by name [name] and its path at [path].");
			out.println("    remove [name]                      Remove an IWAD by name.");
			out.println("    rename [name1] [name2]             Changes an alias from IWAD [name1] to [name2].");
			out.println("    get [name]                         Print the path of the IWAD named [name].");
			out.println("    set [name] [path]                  Change the path of the IWAD named [name] to [path].");
			out.println("    scan [path]                        Adds all IWADs in directory [path] (can be WAD/PK3/IPK3/IPK7). Filename becomes [name].");
			out.println("      --recurse, -r                      ...and search recursively from [path].");
			out.println("      --prefix, -p [string]              ...and prepend [string] to each WAD entry name.");
			out.println("      --force-add-existing               ...and update the names of known paths (otherwise, this does not add existing).");
		}
		if (commandName == null || DoomyCommand.WAD.equalsIgnoreCase(commandName))
		{
			out.println("  wad                                  Print this subsection's help and terminate.");
			out.println("    list                               List cached/downloaded WADs.");
			out.println("      [phrase]                           ...that contains [phrase] (wildcard is *).");
			out.println("    add [name] [path]                  Add a WAD alias for a WAD named [name] for [path] (can be a zip archive).");
			out.println("    remove [name]                      Remove a WAD alias for a WAD named [name] for [path].");
			out.println("      --downloaded, -d                   ...and also remove the downloaded WAD.");
			out.println("    rename [name1] [name2]             Changes an alias from WAD [name1] to [name2].");
			out.println("    get [name]                         Print the path of the WAD named [name].");
			out.println("    set [name] [path]                  Change the path of the WAD named [name] to [path].");
			out.println("    scan [path]                        Adds all WADs in directory [path] (can be WAD/PK3/PK7/ZIPs). Filename becomes [name].");
			out.println("      --recurse, -r                      ...and search recursively from [path].");
			out.println("      --prefix, -p [string]              ...and prepend [string] to each WAD entry name.");
			out.println("      --force-add-existing               ...and update the names of known paths (otherwise, this does not add existing).");
			out.println("    source                             Print this subsection's help and terminate.");
			out.println("    list                               List all URL sources.");
			out.println("      [phrase]                           ...whose WAD name contains [phrase].");
			out.println("    get [name]                         Prints the URL source of a downloaded WAD named [name].");
			out.println("    set [name] [url]                   Sets the URL source of a downloaded WAD named [name] to [url].");
			out.println("    text                               Dumps the WAD's text entry, if any (searches same directory for FILENAME.TXT or inside archive).");
			out.println("    download [name] [url]              Downloads a WAD to the main download directory as its path from [url] and adds it as its source under WAD [name].");
			out.println("    redownload"); 
			out.println("      [name]                           Redownload a downloaded WAD named [name].");
			out.println("    dependency");
			out.println("      list [name]                      Lists all dependencies of WAD [name].");
			out.println("      add [name1] [name2]              Adds a dependency of WAD [name1] to WAD [name2] (will be loaded automatically, and before [name1]).");
			out.println("      remove [name1] [name2]           Removes a dependency of WAD [name1], specifically, [name2].");
			out.println("      clear [name]                     Removes all dependencies of WAD [name].");
		}
		if (commandName == null || DoomyCommand.PRESET.equalsIgnoreCase(commandName))
		{
			out.println("  preset                               Print this subsection's help and terminate."); 
			out.println("    list                               List all presets.");
			out.println("      [phrase]                           ...whose name/hash contains [phrase].");
			out.println("    name [hash] [name]                 Sets name [name] on a preset with hash [hash].");
			out.println("    create [engine]                    Create preset, but don't run engine.");
			out.println("      --wads, -w [wads...]               ...with these WADs/Zips (by name, dependency-expanded).");
			out.println("      --iwad, -i [name]                  ...with IWAD name [name] (may not be required if specified in engine settings).");
			out.println("    remove                             Print this subsection's help and terminate.");
			out.println("      [name]                           Remove preset named [name] (takes precedence, must confirm).");
			out.println("        --quiet, -q                      ...but skip confirm.");
			out.println("      [hash]                           Remove preset by hash [hash] (must be full, must confirm).");
			out.println("        --quiet, -q                      ...but skip confirm.");
			out.println("    info                               Print this subsection's help and terminate.");
			out.println("      [name]                           Get preset named [name] and list info (takes precedence).");
			out.println("      [hash]                           Get preset hash [hash] and list info (can be starting partial, if unique enough).");
			out.println("    run                                Print this subsection's help and terminate.");
			out.println("      [name]                           Run preset named [name] (takes precedence).");
			out.println("      [hash]                           Run preset hash [hash] (can be starting partial, if unique enough).");
			out.println("        --                             Send args verbatim after this token.");
		}
		if (commandName == null || DoomyCommand.RUN.equalsIgnoreCase(commandName))
		{
			out.println("  run [engine]                         Run engine [engine]");
			out.println("    --server                             ...but use the server EXE, if specified.");
			out.println("    --wads, -w [wads...]                 ...with these WADs/Zips (by name, dependency-expanded) (they are unzipped and added via -file and -deh).");
			out.println("    --iwad, -i [name]                    ...with IWAD name [name] (may not be required if specified in engine settings).");
			out.println("    --preset, -p [name]                  ...and assign the created preset [name].");
			out.println("    --spawn, -s                          ...and do not wait for its completion.");
			out.println("    --dry-command                        ...or instead, print the full command line and do not execute.");
			out.println("    --                                 Send args verbatim after this token.");
		}
		if (commandName == null || DoomyCommand.IDGAMES.equalsIgnoreCase(commandName))
		{
			out.println("  idgames                              Print this subsection's help and terminate.");
			out.println("    ping                               Ping service.");
			out.println("    about                              Display API \"About.\"");
			out.println("    comic                              Return a Doom Comic quote (yes, it's a real call).");
			out.println("    search                             Print this subsection's help and terminate.");
			out.println("      id [id]                          Search for file with ID [id] (not standardized!).");
			out.println("        --name, -n [name]              Specify new name, if downloaded.");
			out.println("        --download, -d [result]        Download the file at result position [result].");
			out.println("        --text, -t [result]            Print text file of result [result].");
			out.println("      file [phrase]                    Searches for filename named [phrase], return best matches up to amount.");
			out.println("        --name, -n [name]              Specify new name, if downloaded.");
			out.println("        --limit, -l [amount]           Set max results to [amount].");
			out.println("        --download, -d [result]        Download the file at result position [result].");
			out.println("        --text, -t [result]            Print text file of result [result].");
			out.println("      title [phrase]                   Searches for title containing [phrase], return best matches up to amount.");
			out.println("        --name, -n [name]              Specify new name, if downloaded.");
			out.println("        --limit, -l [amount]           Set max results to [amount].");
			out.println("        --download, -d [result]        Download the file at result position [result].");
			out.println("        --text, -t [result]            Print text file of result [result].");
			out.println("      author [phrase]                  Searches for author containing [phrase], return best matches up to amount.");
			out.println("        --name, -n [name]              Specify new name, if downloaded.");
			out.println("        --limit, -l [amount]           Set max results to [amount].");
			out.println("        --download, -d [result]        Download the file at result position [result].");
			out.println("        --text, -t [result]            Print text file of result [result].");
			out.println("      text [phrase]                    Searches for author containing [phrase], return best matches up to amount.");
			out.println("        --name, -n [name]              Specify new name, if downloaded.");
			out.println("        --limit, -l [amount]           Set max results to [amount].");
			out.println("        --download, -d [result]        Download the file at result position [result].");
			out.println("        --text, -t [result]            Print text file of result [result].");
		}
		if (commandName == null || DoomyCommand.WADARCHIVE.equalsIgnoreCase(commandName))
		{
			out.println("  wadarchive                           Print this subsection's help and terminate.");
			out.println("    md5 [hash]                         List results from MD5 file hash [hash].");
			out.println("      --download, -d [result]          Download the found file from link [result].");
			out.println("      --name, -n [name]                Specify new alias, if downloaded.");
			out.println("    sha1 [hash]                        List results from SHA1 file hash [hash].");
			out.println("      --download, -d [result]          Download the found file from link [result].");
			out.println("      --name, -n [name]                Specify new alias, if downloaded.");
			out.println("    file [name]                        List results from file [name].");
			out.println("      --download, -d [result]          Download the found file from link [result].");
			out.println("      --name, -n [name]                Specify new alias, if downloaded.");
		}
	}
	
}
