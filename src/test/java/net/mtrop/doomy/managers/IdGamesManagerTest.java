package net.mtrop.doomy.managers;

import java.io.IOException;
import java.net.SocketTimeoutException;

import net.mtrop.doomy.managers.IdGamesManager.IdGamesAboutResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesComicResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesStatusResponse;
import net.mtrop.doomy.managers.IdGamesManager.SortDirection;
import net.mtrop.doomy.managers.IdGamesManager.SortType;

public final class IdGamesManagerTest
{
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SocketTimeoutException, IOException 
	{
		IdGamesManager manager = IdGamesManager.get();
		IdGamesStatusResponse resp = manager.ping();
		IdGamesAboutResponse resp2 = manager.about();
		IdGamesComicResponse resp3 = manager.comic();
		IdGamesFileResponse resp4 = manager.getById(15156);
		IdGamesSearchResponse resp5 = manager.searchByTextFile("mtrop", SortType.DATE, SortDirection.ASC);
		System.out.println();
	}
}
