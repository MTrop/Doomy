package net.mtrop.doomy.managers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;

import com.blackrook.json.JSONReader;
import com.blackrook.json.annotation.JSONMapType;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.HTTPUtils;
import net.mtrop.doomy.struct.HTTPUtils.HTTPHeaders;
import net.mtrop.doomy.struct.HTTPUtils.HTTPReader;
import net.mtrop.doomy.struct.HTTPUtils.HTTPResponse;

/**
 * Wad-Archive Manager class.
 * @author Matthew Tropiano
 */
public final class WadArchiveManager
{
	private static JSONResponseReader<WadArchiveResult> WADARCHIVERESULT_READER 
		= new JSONResponseReader<WadArchiveResult>(WadArchiveResult.class);

	private static JSONResponseReader<WadseekerResult[]> WADSEEKERRESULT_READER 
		= new JSONResponseReader<WadseekerResult[]>(WadseekerResult[].class);
	
	private static HTTPHeaders HEADERS = HTTPUtils.headers()
		// Wad-Archive blocks bots by Agent - send a browser string.
		.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
		;
		
	/** Singleton instance. */
	private static WadArchiveManager INSTANCE;
	
	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static WadArchiveManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new WadArchiveManager(ConfigManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Config manager. */
	private ConfigManager config;
	
	private WadArchiveManager(ConfigManager config)
	{
		this.config = config;
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
		return HTTPUtils.httpGet(getAPIURL() + hash, HEADERS, getTimeout(), WADARCHIVERESULT_READER);
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
		return HTTPUtils.httpGet(getWadseekerAPIURL() + name, HEADERS, getTimeout(), WADSEEKERRESULT_READER);
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
		return HTTPUtils.httpGet(getWadseekerAPIURL() + name, HEADERS, HTTPUtils.parameters().setParameter("iwad", iwad), getTimeout(), WADSEEKERRESULT_READER);
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
		return HTTPUtils.httpGet(getWadseekerAPIURL() + name, HEADERS, HTTPUtils.parameters().setParameter("port", port), getTimeout(), WADSEEKERRESULT_READER);
	}
	
	private static class JSONResponseReader<T> implements HTTPReader<T>
	{
		private Class<T> classType;
		
		private JSONResponseReader(Class<T> classType) 
		{
			this.classType = classType;
		}
		
		@Override
		public T onHTTPResponse(HTTPResponse response) throws IOException
		{
			return JSONReader.readJSON(classType, response.getInputStream());
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
