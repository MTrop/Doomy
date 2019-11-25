package net.mtrop.doomy.managers;

import java.io.IOException;
import java.net.SocketTimeoutException;

public final class WadArchiveManagerTest
{
	public static void main(String[] args) throws SocketTimeoutException, IOException 
	{
		WadArchiveManager manager = WadArchiveManager.get();
		WadArchiveManager.WadArchiveResult result = manager.getByHash("333ca955620fd1678619e62976fb96bb");
		WadArchiveManager.WadseekerResult[] seekerResult = manager.getByName("dot");
		WadArchiveManager.WadseekerResult[] seekerResult2 = manager.getByNameAndIWAD("dot", "doom2");
		WadArchiveManager.WadseekerResult[] seekerResult3 = manager.getByNameAndPort("xexis", "zdoom");
		System.out.println();
	}
}
