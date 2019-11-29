package net.mtrop.doomy.managers;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.blackrook.json.JSONReader;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.managers.DownloadManager.FileDownloadListener;
import net.mtrop.doomy.struct.HTTPUtils;
import net.mtrop.doomy.struct.AsyncFactory.Instance;
import net.mtrop.doomy.struct.HTTPUtils.HTTPParameters;
import net.mtrop.doomy.struct.HTTPUtils.HTTPReader;
import net.mtrop.doomy.struct.HTTPUtils.HTTPResponse;

/**
 * idGames Manager class.
 * @author Matthew Tropiano
 */
public final class IdGamesManager
{
	private static JSONResponseReader<IdGamesStatusResponse> IDGAMESSTATUS_READER 
		= new JSONResponseReader<IdGamesStatusResponse>(IdGamesStatusResponse.class);

	private static JSONResponseReader<IdGamesAboutResponse> IDGAMESABOUT_READER 
		= new JSONResponseReader<IdGamesAboutResponse>(IdGamesAboutResponse.class);

	private static JSONResponseReader<IdGamesComicResponse> IDGAMESCOMIC_READER 
		= new JSONResponseReader<IdGamesComicResponse>(IdGamesComicResponse.class);

	private static JSONResponseReader<IdGamesFileResponse> IDGAMESFILE_READER 
		= new JSONResponseReader<IdGamesFileResponse>(IdGamesFileResponse.class);

	private static JSONResponseReader<IdGamesSearchResponse> IDGAMESSEARCH_READER 
		= new JSONResponseReader<IdGamesSearchResponse>(IdGamesSearchResponse.class);

	private static HTTPParameters COMMON_PARAMS = HTTPUtils.parameters()
		.addParameter("out", "json");

	private static HTTPParameters SEARCH_PARAMS = COMMON_PARAMS.copy()
		.addParameter("action", "search");

	/** Singleton instance. */
	private static IdGamesManager INSTANCE;
	
	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static IdGamesManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new IdGamesManager(ConfigManager.get(), DownloadManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	public enum SortType
	{
		DATE,
		FILENAME,
		SIZE,
		RATING;
	}
	
	public enum SortDirection
	{
		ASC,
		DESC;
	}
	
	// =======================================================================

	/** Config manager. */
	private ConfigManager config;
	/** Download manager. */
	private DownloadManager downloadManager;
	
	private IdGamesManager(ConfigManager config, DownloadManager download)
	{
		this.config = config;
		this.downloadManager = download;
	}

	private String getAPIURL()
	{
		return config.getValue(ConfigManager.SETTING_IDGAMES_API_URL);
	}

	private String getMirrorURL()
	{
		return config.getValue(ConfigManager.SETTING_IDGAMES_MIRROR_BASE_URL);
	}

	private int getTimeout()
	{
		int out;
		try {
			out = Integer.parseInt(config.getValue(ConfigManager.SETTING_IDGAMES_TIMEOUT_MILLIS, "10000"));
		} catch (NumberFormatException e) {
			return 10000;
		}
		return out;
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
	 * Pings idGames and returns a status response.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesStatusResponse ping() throws SocketTimeoutException, IOException
	{
		return HTTPUtils.httpGet(getAPIURL(), COMMON_PARAMS.copy().addParameter("action", "ping"), getTimeout(), IDGAMESSTATUS_READER);
	}
	
	/**
	 * Fetches "about" info from idGames and returns the response.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesAboutResponse about() throws SocketTimeoutException, IOException
	{
		return HTTPUtils.httpGet(getAPIURL(), COMMON_PARAMS.copy().addParameter("action", "about"), getTimeout(), IDGAMESABOUT_READER);
	}
	
	/**
	 * Makes a "comic" request from idGames and returns the response.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesComicResponse comic() throws SocketTimeoutException, IOException
	{
		return HTTPUtils.httpGet(getAPIURL(), COMMON_PARAMS.copy().addParameter("action", "comic"), getTimeout(), IDGAMESCOMIC_READER);
	}
	
	/**
	 * Makes a file request from idGames and returns the response.
	 * Depending on the service, this may not return the same file.
	 * @param id the file id.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesFileResponse getById(long id) throws SocketTimeoutException, IOException
	{
		return HTTPUtils.httpGet(getAPIURL(), COMMON_PARAMS.copy().addParameter("action", "get").addParameter("id", id), getTimeout(), IDGAMESFILE_READER);
	}

	private IdGamesSearchResponse search(HTTPParameters parameters) throws SocketTimeoutException, IOException
	{
		return HTTPUtils.httpGet(getAPIURL(), parameters, getTimeout(), IDGAMESSEARCH_READER);
	}
	
	/**
	 * Makes a search request from idGames by file name and returns the response.
	 * @param criteria the search criteria.
	 * @param sortType the result sort type.
	 * @param direction the sort direction.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesSearchResponse searchByFileName(String criteria, SortType sortType, SortDirection direction) throws SocketTimeoutException, IOException
	{
		return search(SEARCH_PARAMS.copy()
			.addParameter("query", criteria)
			.addParameter("type", "filename")
			.addParameter("sort", sortType.name().toLowerCase())
			.addParameter("dir", direction.name().toLowerCase())
		);
	}
	
	/**
	 * Makes a search request from idGames by title and returns the response.
	 * @param criteria the search criteria.
	 * @param sortType the result sort type.
	 * @param direction the sort direction.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesSearchResponse searchByTitle(String criteria, SortType sortType, SortDirection direction) throws SocketTimeoutException, IOException
	{
		return search(SEARCH_PARAMS.copy()
			.addParameter("query", criteria)
			.addParameter("type", "title")
			.addParameter("sort", sortType.name().toLowerCase())
			.addParameter("dir", direction.name().toLowerCase())
		);
	}
	
	/**
	 * Makes a search request from idGames by author and returns the response.
	 * @param criteria the search criteria.
	 * @param sortType the result sort type.
	 * @param direction the sort direction.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesSearchResponse searchByAuthor(String criteria, SortType sortType, SortDirection direction) throws SocketTimeoutException, IOException
	{
		return search(SEARCH_PARAMS.copy()
			.addParameter("query", criteria)
			.addParameter("type", "author")
			.addParameter("sort", sortType.name().toLowerCase())
			.addParameter("dir", direction.name().toLowerCase())
		);
	}
	
	/**
	 * Makes a search request from idGames by text file contents and returns the response.
	 * @param criteria the search criteria.
	 * @param sortType the result sort type.
	 * @param direction the sort direction.
	 * @return the response object.
	 * @throws SocketTimeoutException if the call times out.
	 * @throws IOException if an I/O error occurs.
	 */
	public IdGamesSearchResponse searchByTextFile(String criteria, SortType sortType, SortDirection direction) throws SocketTimeoutException, IOException
	{
		return search(SEARCH_PARAMS.copy()
			.addParameter("query", criteria)
			.addParameter("type", "textfile")
			.addParameter("sort", sortType.name().toLowerCase())
			.addParameter("dir", direction.name().toLowerCase())
		);
	}
	
	/**
	 * Creates an asynchronous task for downloading a file from idGames.
	 * @param idGamesPath the path to the file (relative to the public directory root).
	 * @param targetPath the target file path to write to (the directory will be created).
	 * @param listener a listener for file progress.
	 * @return the response object.
	 */
	public Instance<File> download(String idGamesPath, String targetPath, FileDownloadListener listener)
	{
		return downloadManager.download(getMirrorURL() + idGamesPath, getTimeout(), targetPath, listener);
	}
	
	// ===== Response Content ==================================================

	/**
	 * idGames Meta
	 */
	public static class IdGamesMetaContent
	{
		public int version;
	}
	
	/**
	 * idGames Message
	 */
	public static class IdGamesMessageContent
	{
		public String type;
		public String message;
	}
	
	/**
	 * idGames Abbreviated File Content
	 */
	public static class IdGamesFileContent
	{
		public long id;
		public String title;
		public String dir;
		public String filename;
		public long size;
		public long age;
		public String date;
		public String author;
		public String email;
		public String description;
		public double rating;
		public int votes;
		public String url;
		public String idgamesurl;
	}

	/**
	 * idGames Full File Content
	 */
	public static class IdGamesSingleFileContent extends IdGamesFileContent
	{
		public String credits;
		public String base;
		public String buildtime;
		public String editors;
		public String bugs;
		public String textfile;
	}
	
	/**
	 * idGames Status Content
	 */
	public static class IdGamesStatusContent
	{
		public String status;
	}

	/**
	 * idGames About Content
	 */
	public static class IdGamesAboutContent
	{
		public String credits;
		public String copyright;
		public String info;
	}

	/**
	 * idGames Comic Content
	 */
	public static class IdGamesComicContent
	{
		public String quote;
		public String order;
	}

	/**
	 * idGames Search Content
	 */
	public static class IdGamesSearchContent
	{
		public IdGamesFileContent[] file;
	}

	// ===== Response Shape ==================================================
	
	/**
	 * idGames Response Common Response
	 */
	public static abstract class IdGamesResponse
	{
		public IdGamesMetaContent meta;
		public IdGamesMessageContent error;
		public IdGamesMessageContent warning;
	}
	
	/**
	 * idGames Status Response
	 */
	public static class IdGamesStatusResponse extends IdGamesResponse
	{
		public IdGamesStatusContent content;
	}
	
	/**
	 * idGames About Response
	 */
	public static class IdGamesAboutResponse extends IdGamesResponse
	{
		public IdGamesAboutContent content;
	}
	
	/**
	 * idGames Comic Response
	 */
	public static class IdGamesComicResponse extends IdGamesResponse
	{
		public IdGamesComicContent content;
	}
	
	/**
	 * idGames File Response
	 */
	public static class IdGamesFileResponse extends IdGamesResponse
	{
		public IdGamesSingleFileContent content;
	}
	
	/**
	 * idGames Search Response
	 */
	public static class IdGamesSearchResponse extends IdGamesResponse
	{
		public IdGamesSearchContent content;
	}
	
}
