/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.engine.template;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.EngineTemplateManager;
import net.mtrop.doomy.managers.EngineTemplateManager.EngineTemplate;

/**
 * A command that prints all stored templates.
 * @author Matthew Tropiano
 */
public class EngineTemplateListCommand implements DoomyCommand
{
	private String phrase;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		phrase = args.pollFirst();
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, phrase);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param phrase the name or phrase to search for.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String phrase)
	{
		EngineTemplate[] records = EngineTemplateManager.get().getAllTemplates(phrase);
		for (int i = 0; i < records.length; i++)
			handler.outln(records[i].name);
		handler.outln(records.length + " templates found.");
		return ERROR_NONE;
	}

}
