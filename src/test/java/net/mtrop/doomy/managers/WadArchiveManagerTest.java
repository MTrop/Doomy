/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.io.IOException;
import java.net.SocketTimeoutException;

import net.mtrop.doomy.IOHandler;

public final class WadArchiveManagerTest
{
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SocketTimeoutException, IOException 
	{
		WadArchiveManager manager = WadArchiveManager.get();
		WadArchiveManager.WadArchiveResult result = manager.getByHash("333ca955620fd1678619e62976fb96bb");
		WadArchiveManager.WadseekerResult[] seekerResult = manager.getByName("dot");
		WadArchiveManager.WadseekerResult[] seekerResult2 = manager.getByNameAndIWAD("dot", "doom2");
		WadArchiveManager.WadseekerResult[] seekerResult3 = manager.getByNameAndPort("xexis", "zdoom");
		IOHandler.stdio().outln();
	}
}
