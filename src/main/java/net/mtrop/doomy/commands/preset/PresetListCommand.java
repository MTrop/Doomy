package net.mtrop.doomy.commands.preset;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.PresetInfo;
import net.mtrop.doomy.struct.ObjectUtils;

/**
 * A command that prints all stored templates.
 * @author Matthew Tropiano
 */
public class PresetListCommand implements DoomyCommand
{
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		PresetInfo[] records = PresetManager.get().getAllPresetsByNameOrHash(phrase);
		if (records.length > 0)
		{
			int hashlen = 0;
			int namelen = 4;
			int enginelen = 6;
			int iwadlen = 4;
			for (int i = 0; i < records.length; i++)
			{
				hashlen = Math.max(ObjectUtils.isNull(records[i].hash, "").length() + 1, hashlen);
				namelen = Math.max(ObjectUtils.isNull(records[i].name, "").length() + 1, namelen);
				enginelen = Math.max(ObjectUtils.isNull(records[i].engineName, "").length() + 1, enginelen);
				iwadlen = Math.max(ObjectUtils.isNull(records[i].iwadName, "").length() + 1, iwadlen);
			}
			
			String format = "%-" + hashlen + "s %-" + namelen + "s %-" + enginelen + "s %-" + iwadlen + "s %s\n";
			out.printf(format, "Hash", "Name", "Engine", "IWAD", "Wads");
			out.printf(format, "====", "====", "======", "====", "====");
			for (int i = 0; i < records.length; i++)
			{
				String wadliststr = Arrays.toString(records[i].wads);
				out.printf(format, records[i].hash, records[i].name, records[i].engineName, ObjectUtils.isNull(records[i].iwadName, ""), wadliststr.substring(1, wadliststr.length() - 1));
			}
		}
		out.println(records.length + " presets found.");
		return ERROR_NONE;
	}

}
