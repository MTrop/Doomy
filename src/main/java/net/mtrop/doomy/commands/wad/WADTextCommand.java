package net.mtrop.doomy.commands.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Deque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.FileUtils;
import net.mtrop.doomy.struct.IOUtils;
import net.mtrop.doomy.struct.ObjectUtils;

/**
 * A command that outputs a WAD's TEXT file.
 * @author Matthew Tropiano
 */
public class WADTextCommand implements DoomyCommand
{
	private String name;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		name = args.pollFirst();
		if (name == null)
			throw new BadArgumentException("Expected name of WAD.");
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		WAD wad = WADManager.get().getWAD(name);
		
		if (wad == null)
		{
			err.println("ERROR: No such WAD: " + name);
			return ERROR_NOT_FOUND;
		}
		
		if (!(new File(wad.path)).exists())
		{
			err.println("ERROR: WAD '" + wad.path + "' not found. It should probably be removed.");
			return ERROR_NOT_FOUND;
		}
		
		File wadFile = new File(wad.path);
		
		String filename = FileUtils.getFileNameWithoutExtension(wadFile.getName());
		String textFileName = filename + ".txt";
		
		File text = new File((new File(wad.path)).getParent() + File.separator + filename + ".txt");
		if (!text.exists())
		{
			try (ZipFile zf = new ZipFile(wad.path))
			{
				ZipEntry entry = ObjectUtils.isNull(zf.getEntry(textFileName), zf.getEntry(textFileName.toUpperCase()));
				if (entry != null)
				{
					try (InputStream textIn = zf.getInputStream(entry))
					{
						IOUtils.relay(textIn, out, 8192);
					} 
					catch (Exception e) 
					{
						err.println("ERROR: " + e.getMessage());
						return ERROR_IO_ERROR;
					} 
					out.println();
				}
				else
				{
					out.println("No text file called '" + textFileName + "' found.");
				}
			} 
			catch (Exception e) 
			{
				err.println("ERROR: " + e.getMessage());
				return ERROR_IO_ERROR;
			} 
		}
		else
		{
			try (InputStream textIn = new FileInputStream(text))
			{
				IOUtils.relay(textIn, out, 8192);
			} 
			catch (Exception e) 
			{
				err.println("ERROR: " + e.getMessage());
				return ERROR_IO_ERROR;
			} 
			out.println();
		}
		
		return ERROR_NONE;
	}

}
