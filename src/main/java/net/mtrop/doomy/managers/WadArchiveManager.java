package net.mtrop.doomy.managers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.blackrook.json.JSONReader;
import com.blackrook.json.annotation.JSONMapType;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.HTTPUtils;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.HTTPUtils.HTTPHeaders;
import net.mtrop.doomy.struct.HTTPUtils.HTTPReader;
import net.mtrop.doomy.struct.HTTPUtils.HTTPRequest;
import net.mtrop.doomy.struct.HTTPUtils.HTTPResponse;
import net.mtrop.doomy.struct.HTTPUtils.TransferMonitor;

/**
 * Wad-Archive Manager class.
 * @author Matthew Tropiano
 */
public final class WadArchiveManager
{
	private static final JSONResponseReader<WadArchiveResult> WADARCHIVERESULT_READER 
		= new JSONResponseReader<WadArchiveResult>(WadArchiveResult.class);

	private static final JSONResponseReader<WadseekerResult[]> WADSEEKERRESULT_READER 
		= new JSONResponseReader<WadseekerResult[]>(WadseekerResult[].class);
	
	private static final HTTPHeaders HEADERS = HTTPUtils.headers()
		// Wad-Archive blocks bots by Agent - send a browser string.
		.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
		
	// Singleton instance.
	private static final SingletonProvider<WadArchiveManager> INSTANCE = new SingletonProvider<>(() -> new WadArchiveManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static WadArchiveManager get()
	{
		return INSTANCE.get();
	}

	// =======================================================================
	
	/** Config manager. */
	private ConfigManager config;
	
	private WadArchiveManager()
	{
		this.config = ConfigManager.get();
	}

	private String getAPIURL()
	{
		return config.getValue(ConfigManager.SETTING_WADARCHIVE_API_URL);
	}

	private String getWadseekerAPIURL()
	{
		return config.getValue(ConfigManager.SETTING_WADARCHIVE_WADSEEKER_API_URL);
	}

	private int getTimeout()
	{
		int out;
		try {
			out = Integer.parseInt(config.getValue(ConfigManager.SETTING_WADARCHIVE_TIMEOUT_MILLIS, "10000"));
		} catch (NumberFormatException e) {
			return 10000;
		}
		return out;
	}
	
	/**
	 * Fetches a WadArchive entry by hash (MD5 or SHA-1). 
	 * @param hash the hash of the WAD file.
	 * @return a result.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public WadArchiveResult getByHash(String hash) throws SocketTimeoutException, IOException
	{
		return HTTPRequest.get(getAPIURL() + hash)
			.setHeaders(HEADERS)
			.timeout(getTimeout())
			.send(WADARCHIVERESULT_READER);
	}
	
	/**
	 * Fetches a Wadseeker entry by name. 
	 * @param name the name to search for (a file name).
	 * @return a result.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public WadseekerResult[] getByName(String name) throws SocketTimeoutException, IOException
	{
		return HTTPRequest.get(getWadseekerAPIURL() + name)
			.setHeaders(HEADERS)
			.timeout(getTimeout())
			.send(WADSEEKERRESULT_READER);
	}
	
	/**
	 * Fetches a Wadseeker entry by name and IWAD. 
	 * @param name the name to search for (a file name).
	 * @param iwad the IWAD name to search for.
	 * @return a result.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public WadseekerResult[] getByNameAndIWAD(String name, String iwad) throws SocketTimeoutException, IOException
	{
		return HTTPRequest.get(getWadseekerAPIURL() + name)
			.setHeaders(HEADERS)
			.parameters(HTTPUtils.entry("iwad", iwad))
			.timeout(getTimeout())
			.send(WADSEEKERRESULT_READER);
	}
	
	/**
	 * Fetches a Wadseeker entry by name and port. 
	 * @param name the name to search for (a file name).
	 * @param port the port name to search for.
	 * @return a result.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public WadseekerResult[] getByNameAndPort(String name, String port) throws SocketTimeoutException, IOException
	{
		return HTTPRequest.get(getWadseekerAPIURL() + name)
			.setHeaders(HEADERS)
			.parameters(HTTPUtils.entry("port", port))
			.timeout(getTimeout())
			.send(WADSEEKERRESULT_READER);
	}
	
	private static class JSONResponseReader<T> implements HTTPReader<T>
	{
		private Class<T> classType;
		
		private JSONResponseReader(Class<T> classType) 
		{
			this.classType = classType;
		}
		
		@Override
		public T onHTTPResponse(HTTPResponse response, AtomicBoolean cancelSwitch, TransferMonitor monitor) throws IOException 
		{
			return JSONReader.readJSON(classType, response.getContentStream());
		}
	}
	
	/**
	 * A single Wadseeker result.
	 */
	public static class WadArchiveResult
	{
		/** If present, an error happened. */
		public String error;
		/** The MD5 hash of this file. */
		public String md5;
		/** The SHA-1 hash of this file. */
		public String sha1;
		/** Size of WAD in bytes. */
		public long size;
		/** Type of WAD (PWAD/IWAD/PK3/PKE/PK7). */
		public String type;
		/** Ports that are compatible. */
		public String port;
		/** Known by these filenames. */
		public String[] filenames;
		/** Links to download from. */
		public String[] links;
		/** Comma-separated list of map lumps. */
		public String maps;
		/** Saved screenshots of each map. */
		@JSONMapType(keyType = String.class, valueType = String.class)
		public Map<String, String> screenshots;
		/** What Discs this belongs to. */
		@JSONMapType(keyType = String.class, valueType = String.class)
		public Map<String, String> discs;
	}
	
	/**
	 * A single Wadseeker result.
	 */
	public static class WadseekerResult
	{
		/** Size of WAD in bytes. */
		public long size;
		/** Type of WAD (PWAD/IWAD/PK3/PKE/PK7). */
		public String type;
		/** Ports that are compatible. */
		public String[] port;
		/** Known by these filenames. */
		public String[] filenames;
		/** Links to download from. */
		public String[] links;
	}
	
}
