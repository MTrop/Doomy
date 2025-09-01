package net.mtrop.doomy.commands.wad;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Deque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

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
	public int call(IOHandler handler)
	{
		return execute(handler, name);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param name the WAD name.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String name)
	{
		WAD wad = WADManager.get().getWAD(name);
		
		if (wad == null)
		{
			handler.errln("ERROR: No such WAD: " + name);
			return ERROR_NOT_FOUND;
		}
		
		if (!(new File(wad.path)).exists())
		{
			handler.errln("ERROR: WAD '" + wad.path + "' not found. It should probably be removed.");
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
						handler.relay(textIn);
					} 
					catch (Exception e) 
					{
						handler.errln("ERROR: " + e.getMessage());
						return ERROR_IO_ERROR;
					} 
					handler.outln();
				}
				else
				{
					handler.outln("No text file called '" + textFileName + "' found.");
				}
			} 
			catch (Exception e) 
			{
				handler.errln("ERROR: " + e.getMessage());
				return ERROR_IO_ERROR;
			}
		}
		else
		{
			try (InputStream textIn = new FileInputStream(text))
			{
				handler.relay(textIn);
			} 
			catch (Exception e) 
			{
				handler.errln("ERROR: " + e.getMessage());
				return ERROR_IO_ERROR;
			} 
			handler.outln();
		}
		
		return ERROR_NONE;
	}

}
