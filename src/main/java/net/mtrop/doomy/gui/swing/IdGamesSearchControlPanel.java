/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.DownloadManager;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.IconManager;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.FieldType;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileContent;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSingleFileContent;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.MessengerManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.InstancedFuture;
import net.mtrop.doomy.struct.swing.ComponentFactory.ProgressBarOrientation;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;
import static net.mtrop.doomy.struct.swing.TableFactory.*;


/**
 * A search panel for the idGames Archive.
 */
public class IdGamesSearchControlPanel extends JPanel 
{
	private static final long serialVersionUID = 5500042924527901695L;

	private final ConfigManager config;
	private final MessengerManager messenger;
	private final GUIManager gui;
	private final LanguageManager language;
	private final IconManager icons;
	private final IdGamesManager idGames;
	private final TaskManager taskManager;
	private final WADManager wadManager;
	
	private JFormField<String> searchField;
	private JFormField<FieldType> fieldTypeField;
	private JFormField<Void> searchButtonField;
	
	private JObjectTable<IdGamesFileContent> resultsTable;
	
	private Action downloadAction;
	private Action fileInfoAction;
	
	private JLabel statusLabel;
	
	public IdGamesSearchControlPanel()
	{
		this.config = ConfigManager.get();
		this.messenger = MessengerManager.get();
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		this.icons = IconManager.get();
		this.idGames = IdGamesManager.get();
		this.taskManager = TaskManager.get();
		this.wadManager = WADManager.get();
		
		this.searchField = stringField(false, true);
		this.fieldTypeField = comboField(comboBox(Arrays.asList(FieldType.values())));
		this.fieldTypeField.setValue(FieldType.FILENAME);
		this.searchButtonField = buttonField(button(language.getText("idgames.search.button"), (b) -> taskManager.spawn(() -> onSearch())));
		
		this.resultsTable = objectTable(SelectionPolicy.SINGLE, 
			objectTableModel(IdGamesFileContent.class, Arrays.asList()), 
			(model, event) -> onSelection()
		);
		this.resultsTable.addMouseListener(new MouseInputAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
					onFileInfo();
			}
		});
		
		this.resultsTable.getColumnModel().getColumn(0).setPreferredWidth(120);
		this.resultsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		this.resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		this.resultsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		this.resultsTable.getColumnModel().getColumn(4).setPreferredWidth(100);

		this.fileInfoAction = actionItem(language.getText("idgames.fileinfo"), (e) -> onFileInfo());
		this.downloadAction = actionItem(language.getText("idgames.download"), (e) -> onDownload());
		
		this.statusLabel = label("");
		
		printSuccessStatus(language.getText("idgames.messages.ready"));
		onSelection();
		
		containerOf(this, borderLayout(8, 8),
			node(BorderLayout.CENTER, containerOf(borderLayout(0, 8),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("idgames.search.labelwidth")),
					gui.formField("idgames.search.for", searchField),
					gui.formField("idgames.search.field", fieldTypeField),
					gui.formField("field.blank", searchButtonField)
				)),
				node(BorderLayout.CENTER, scroll(resultsTable))
			)),
			node(BorderLayout.EAST, containerOf(dimension(language.getInteger("idgames.actions.width"), 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1, 0, 2),
					node(button(fileInfoAction)),
					node(button(downloadAction))
				)),
				node(BorderLayout.CENTER, containerOf())
			)),
			node(BorderLayout.SOUTH, statusLabel)
		);
	}

	private void printSuccessStatus(String message)
	{
		SwingUtils.invoke(() -> {
			statusLabel.setIcon(icons.getImage("success.png"));
			statusLabel.setText(message);
		});
	}
	
	private void printActivityStatus(String message)
	{
		SwingUtils.invoke(() -> {
			statusLabel.setIcon(icons.getImage("activity.gif"));
			statusLabel.setText(message);
		});
	}
	
	private void printErrorStatus(String message)
	{
		SwingUtils.invoke(() -> {
			statusLabel.setIcon(icons.getImage("error.png"));
			statusLabel.setText(message);
		});
	}
	
	private JFormField<String> createReadOnlyField(String content)
	{
		return ObjectUtils.apply(stringField(content, false, false), (field) -> ((JTextField)field.getFormInputComponent()).setEditable(false));
	}
	
	private void onFileInfo()
	{
		List<IdGamesFileContent> selected = resultsTable.getSelectedObjects();
		if (selected.isEmpty())
			return;
		
		IdGamesFileResponse response;

		try {
			response = idGames.getById(selected.get(0).id).get();

			if (response.error != null)
				printErrorStatus(language.getText("idgames.messages.error", response.error.message));
			else if (response.warning != null)
				printSuccessStatus(response.warning.message);
			else
				printSuccessStatus(language.getText("idgames.messages.done"));
			
		} catch (CancellationException e) {
			printErrorStatus(language.getText("idgames.messages.error.cancel"));
			return;
		} catch (InterruptedException e) {
			printErrorStatus(language.getText("idgames.messages.error.interrupt"));
			return;
		} catch (ExecutionException e) {
			printErrorStatus(language.getText("idgames.messages.error", e.getCause().getLocalizedMessage()));
			return;
		} catch (SocketTimeoutException e) {
			printErrorStatus(language.getText("idgames.messages.error.timeout"));
			return;
		} catch (IOException e) {
			printErrorStatus(language.getText("idgames.messages.error.io"));
			return;
		}
		
		int labelWidth = language.getInteger("idgames.fileinfo.labelwidth");
		IdGamesSingleFileContent content = response.content;
		
		modal(this, language.getText("idgames.fileinfo.modal.title", content.title), 
			ObjectUtils.apply(new JPanel(), (panel) -> containerOf(panel, dimension(640, 640), borderLayout(),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, labelWidth),
					gui.formField("idgames.fileinfo.filename", createReadOnlyField(content.filename)),
					gui.formField("idgames.fileinfo.path", createReadOnlyField(content.dir + content.filename)),
					gui.formField("idgames.fileinfo.title", createReadOnlyField(content.title)),
					gui.formField("idgames.fileinfo.size", createReadOnlyField(String.valueOf(content.size / 1024) + " KB")),
					gui.formField("idgames.fileinfo.date", createReadOnlyField(content.date)),
					gui.formField("idgames.fileinfo.rating", createReadOnlyField(String.valueOf(content.rating)))
				)),
				node(BorderLayout.CENTER, scroll(ObjectUtils.apply(textArea(content.textfile, 25, 80), (area) -> {
					area.setEditable(false);
					area.setFont(new Font("Monospaced", Font.PLAIN, 12));
				})))
			)), 
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true)
		).openThenDispose();
		
	}

	private void onDownload()
	{
		List<IdGamesFileContent> selected = resultsTable.getSelectedObjects();
		if (selected.isEmpty())
			return;

		// ask for download destination and WAD name.
		
		File initDir = config.getConvertedValue(ConfigManager.SETTING_IDGAMES_DOWNLOAD_DIR, (value) -> value != null ? new File(value) : null);
		
		JFormField<File> destinationDirField = fileField(initDir, "...",
			(current) -> {
				File chosen = gui.chooseDirectory(this, 
					language.getText("idgames.download.browse.file.title"), 
					language.getText("idgames.download.file.browse.select"),
					() -> current != null ? current : gui.getDefaultFile(),
					(select) -> config.setValue(ConfigManager.SETTING_IDGAMES_DOWNLOAD_DIR, select != null ? select.getAbsolutePath() : null)
				);
				return chosen != null ? chosen : current;
			}
		);
		JFormField<String> wadNameField = stringField(FileUtils.getFileNameWithoutExtension(selected.get(0).filename), true, true);
		
		Boolean ok = modal(this, language.getText("idgames.download.title"),
			containerOf(dimension(320, 80), borderLayout(),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("idgames.download.labelwidth")), 
					gui.formField("idgames.download.dest", destinationDirField),
					gui.formField("idgames.download.wadname", wadNameField)
				))
			),
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		// check WAD name
		
		final String wadName = wadNameField.getValue();
		if (wadName != null)
		{
			if (wadManager.getWAD(wadName) != null)
			{
				SwingUtils.error(this, language.getText("idgames.download.wadname.error"));
				return;
			}
		}

		// check destination.

		final File destinationDir = destinationDirField.getValue();
		if (destinationDir == null)
		{
			SwingUtils.error(this, language.getText("idgames.download.dir.error"));
			return;
		}
		if (!destinationDir.exists())
		{
			SwingUtils.error(this, language.getText("idgames.download.dir.notexist"));
			return;
		}
		if (!destinationDir.isDirectory())
		{
			SwingUtils.error(this, language.getText("idgames.download.dir.notdir"));
			return;
		}
		
		// pull data from idGames
		
		IdGamesFileResponse response;

		try {
			response = idGames.getById(selected.get(0).id).get();

			if (response.error != null)
				printErrorStatus(language.getText("idgames.messages.error", response.error.message));
			else if (response.warning != null)
				printSuccessStatus(response.warning.message);
			else
				printSuccessStatus(language.getText("idgames.messages.done"));
			
		} catch (CancellationException e) {
			printErrorStatus(language.getText("idgames.messages.error.cancel"));
			return;
		} catch (InterruptedException e) {
			printErrorStatus(language.getText("idgames.messages.error.interrupt"));
			return;
		} catch (ExecutionException e) {
			printErrorStatus(language.getText("idgames.messages.error", e.getCause().getLocalizedMessage()));
			return;
		} catch (SocketTimeoutException e) {
			printErrorStatus(language.getText("idgames.messages.error.timeout"));
			return;
		} catch (IOException e) {
			printErrorStatus(language.getText("idgames.messages.error.io"));
			return;
		}
		
		// start download
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("                 ");
	
		Modal<Boolean> cancelProgressModal = modal(this, language.getText("idgames.download.file.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
		
		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(true);
		});
	
		final AtomicReference<InstancedFuture<File>> downloadFuture = new AtomicReference<>();
		String uri = response.content.dir + response.content.filename;
		String downloadTarget = DoomyEnvironment.getDownloadDirectoryPath() + File.separator + uri.replace('/', File.separatorChar);
		String downloadTempTarget = downloadTarget + ".temp";
		
		taskManager.spawn(() -> 
		{
			signal.poll();
			if (cancelSwitch.get())
				return;
	
			downloadFuture.set(idGames.download(uri, downloadTempTarget, DownloadManager.intervalListener(125, (current, total, percent) -> 
			{
				if (cancelSwitch.get())
					return true;
				
				SwingUtils.invoke(() -> {
					progressBar.setIndeterminate(false);
					progressBar.setMinimum(0);
					progressBar.setMaximum((int)total);
					progressBar.setValue((int)current);
					progressLabel.setText( (current / 1024) + " KB / " + (total / 1024) + " KB");
				});
				
				return false;
			})));
			
			downloadFuture.get().join();
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
		
		// handle downloaded file

		InstancedFuture<File> downloadFile = downloadFuture.get();
		
		if (downloadFile == null)
			return;
		
		if (downloadFile.getException() != null)
		{
			SwingUtils.error(this, language.getText("idgames.download.error", downloadFile.getException().getLocalizedMessage()));
			return;
		}
		
		final File downloadedFile = downloadFile.result();
		if (downloadedFile == null)
			return;
		
		// copy file to destination.
		
		final Modal<Boolean> cancelCopyModal = modal(this, language.getText("idgames.download.copy.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);

		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(true);
			progressLabel.setText("0%");
		});

		final File outFile = new File(destinationDir.getAbsolutePath() + File.separator + response.content.filename);
		
		taskManager.spawn(() -> 
		{
			signal.poll();
			if (cancelSwitch.get())
				return;

			int buf = 0;
			int max = (int)downloadedFile.length();
			byte[] buffer = new byte[16384];
			final AtomicInteger current = new AtomicInteger(0);
			
			try (FileInputStream fis = new FileInputStream(downloadedFile); FileOutputStream fos = new FileOutputStream(outFile))
			{
				while (!cancelSwitch.get() && (buf = fis.read(buffer)) > 0)
				{
					current.set(current.get() + buf);
					fos.write(buffer, 0, buf);
					SwingUtils.invoke(() -> {
						progressBar.setIndeterminate(false);
						progressBar.setMinimum(0);
						progressBar.setMaximum(max);
						progressBar.setValue(current.get());
					});
				}
			} 
			catch (FileNotFoundException e) 
			{
				SwingUtils.error(this, language.getText("idgames.download.copy.notfound"));
				cancelSwitch.set(true);
			} 
			catch (IOException e) 
			{
				SwingUtils.error(this, language.getText("idgames.download.copy.ioerror", e.getLocalizedMessage()));
				cancelSwitch.set(true);
			}
	
			cancelCopyModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		out = cancelCopyModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
		
		downloadedFile.delete();
		
		if (cancelSwitch.get())
		{
			outFile.delete();
			return;
		}
		
		// add to WAD directory
		
		if (wadName != null)
		{
			if (wadManager.addWAD(wadName, outFile.getAbsolutePath()) != null)
			{
				SwingUtils.info(language.getText("idgames.download.success.wad"));
				messenger.publish(MessengerManager.CHANNEL_WADS_CHANGED, true);
			}
		}
		else
		{
			SwingUtils.info(language.getText("idgames.download.success"));
		}
	}

	private void onSearch() 
	{
		String criteria = searchField.getValue();
		FieldType fieldType = fieldTypeField.getValue();
		
		if (criteria.length() < 3)
		{
			SwingUtils.error(this, language.getText("idgames.search.error.text"));
			return;
		}

		resultsTable.setSelectedRows();
		resultsTable.getTableModel().clearAllRows();
		
		Future<IdGamesSearchResponse> responseFuture = idGames.searchBy(criteria, fieldType);
		printActivityStatus(language.getText("idgames.messages.fetch.search"));
		
		IdGamesSearchResponse searchResult;
		
		try {
			searchResult = responseFuture.get();
			
			if (searchResult.error != null)
				printErrorStatus(language.getText("idgames.messages.error", searchResult.error.message));
			else if (searchResult.warning != null)
				printSuccessStatus(searchResult.warning.message);
			else
				printSuccessStatus(language.getText("idgames.messages.done"));
			
			if (searchResult.content != null)
				resultsTable.getTableModel().setRows(Arrays.asList(searchResult.content.files));
			else
				resultsTable.getTableModel().clearAllRows();
			
		} catch (CancellationException e) {
			printErrorStatus(language.getText("idgames.messages.error.cancel"));
		} catch (InterruptedException e) {
			printErrorStatus(language.getText("idgames.messages.error.interrupt"));
		} catch (ExecutionException e) {
			printErrorStatus(language.getText("idgames.messages.error", e.getCause().getLocalizedMessage()));
		}
			
	}
	
	private void onSelection()
	{
		List<IdGamesFileContent> selected = resultsTable.getSelectedObjects();
		downloadAction.setEnabled(selected.size() == 1);
		fileInfoAction.setEnabled(selected.size() == 1);
	}

}
