/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.struct.swing.ComponentFactory;
import net.mtrop.doomy.struct.swing.ComponentFactory.ProgressBarOrientation;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.FileChooserFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

public class IwadTableControlPanel extends JPanel 
{
	private static final long serialVersionUID = -6460082133191850158L;
	
	private final GUIManager gui;
	private final IWADManager iwadManager;
	private final TaskManager taskManager;
	private final LanguageManager language;
	
	private IwadTablePanel iwadTable;
	private Action addAction;
	private Action removeAction;
	private Action scanAction;
	private Action cleanupAction;
	private Action openAction;

	public IwadTableControlPanel()
	{
		this.gui = GUIManager.get();
		this.iwadManager = IWADManager.get();
		this.taskManager = TaskManager.get();
		this.language = LanguageManager.get();
		
		this.iwadTable = new IwadTablePanel(SelectionPolicy.MULTIPLE_INTERVAL, (model, event) -> onSelection(), (event) -> onOpen());

		this.addAction = actionItem(language.getText("iwads.add"), (e) -> onAdd());
		this.removeAction = actionItem(language.getText("iwads.remove"), (e) -> onRemove());
		this.scanAction = actionItem(language.getText("iwads.scan"), (e) -> onScan());
		this.cleanupAction = actionItem(language.getText("iwads.cleanup"), (e) -> onCleanup());
		this.openAction = actionItem(language.getText("iwads.open"), (e) -> onOpen());

		onSelection();

		containerOf(this, borderLayout(8, 0),
			node(BorderLayout.CENTER, iwadTable),
			node(BorderLayout.EAST, containerOf(dimension(language.getInteger("iwads.actions.width"), 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1, 0, 2),
					node(button(openAction)),
					node(button(addAction)),
					node(button(removeAction)),
					node(button(scanAction)),
					node(button(cleanupAction))
				)),
				node(BorderLayout.CENTER, containerOf())
			))
		);
	}

	// Called when adding a WAD file.
	private void onAdd()
	{
		final JFormField<String> wadNameField = stringField(false, true);
		final JFormField<File> wadPathField = fileField(null, language.getText("file.browse"), 
			(current) -> {
				File chosen = chooseFile(this, language.getText("file.browse.file.title"), language.getText("file.browse.select"), gui.createDoomArchivesFilter());
				return chosen != null ? chosen : current;
			}, 
			(selected) -> {
				if (wadNameField.getValue().length() == 0)
					wadNameField.setValue(FileUtils.getFileNameWithoutExtension(selected));
			}
		);
		
		Boolean doAdd = modal(this, language.getText("iwads.add.title"), containerOf(dimension(350, 64),
				node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("iwads.add.form.width")), 
					gui.formField("iwads.add.form.name", wadNameField),
					gui.formField("iwads.add.form.path", wadPathField)
				))
			),
			gui.createChoiceFromLanguageKey("iwads.add.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doAdd != Boolean.TRUE)
			return;
		
		String name = wadNameField.getValue();
		File path = wadPathField.getValue();
		
		if (ObjectUtils.isEmpty(name))
		{
			SwingUtils.error(this, language.getText("iwads.add.error.name.blank"));
			return;
		}
	
		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(this, language.getText("iwads.add.error.path.blank"));
			return;
		}
	
		if (path.isDirectory())
		{
			SwingUtils.error(this, language.getText("iwads.add.error.path.notdir"));
			return;
		}
	
		if (!path.exists())
		{
			SwingUtils.error(this, language.getText("iwads.add.error.path.noexist"));
			return;
		}
		
		iwadManager.addIWAD(name, path.getAbsolutePath());
		iwadTable.refreshIWADs();
	}

	// Called on WAD removal.
	private void onRemove()
	{
		final List<IWAD> selected = iwadTable.getSelectedIWADs(); 
		if (SwingUtils.noTo(this, language.getText("iwads.remove.message", selected.size())))
			return;
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
	
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("iwads.remove.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
			
		SwingUtils.invoke(() -> {
			progressBar.setMinimum(0);
			progressBar.setIndeterminate(true);
			progressBar.setMaximum(selected.size());
		});
	
		taskManager.spawn(() -> 
		{
			signal.poll();
			int c = 0;
			for (IWAD wad : selected)
			{
				if (cancelSwitch.get())
					return;
	
				final int count = c;
				SwingUtils.invoke(() -> {
					progressBar.setValue(count);
					progressBar.setIndeterminate(false);
					progressLabel.setText((int)((float)count / selected.size() * 100) + "%");
				});
				
				iwadManager.removeIWAD(wad.name);
				c++;
			}
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
		iwadTable.refreshIWADs();
	}

	// Called for scanning for new WADs.
	private void onScan()
	{
		final JFormField<String> prefixField = stringField("", false, true);
		final JFormField<Boolean> recurseField = checkBoxField(checkBox(true));
		final JFormField<Boolean> updateExistingField = checkBoxField(checkBox(false));
		final JFormField<File> directoryField = fileField(null, language.getText("file.browse"), 
			(current) -> {
				File chosen = chooseDirectory(this, language.getText("file.browse.dir.title"), language.getText("file.browse.select"));
				return chosen != null ? chosen : current;
			}
		);
		
		Boolean doScan = modal(this, language.getText("iwads.scan.form.title"),
			containerOf(dimension(400, 120), borderLayout(0, 0),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("iwads.scan.form.width")),
					gui.formField("iwads.scan.form.path", directoryField),
					gui.formField("iwads.scan.form.prefix", prefixField),
					gui.formField("iwads.scan.form.recurse", recurseField),
					gui.formField("iwads.scan.form.update", updateExistingField)
				))
			),
			gui.createChoiceFromLanguageKey("iwads.scan.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doScan != Boolean.TRUE)
			return;
	
		File startDir = directoryField.getValue();
		
		if (startDir == null)
		{
			SwingUtils.info(this, language.getText("iwads.scan.error.path.blank"));
			return;
		}
		else if (!startDir.exists())
		{
			SwingUtils.info(this, language.getText("iwads.scan.error.path.noexist"));
			return;
		}
		else if (!startDir.isDirectory())
		{
			SwingUtils.info(this, language.getText("iwads.scan.error.path.notdir"));
			return;
		}
		
		String prefix = prefixField.getValue();
		boolean recurse = recurseField.getValue();
		boolean force = updateExistingField.getValue();
		
		final String[] FILETYPES = {"PK3", "PK7", "PKE", "WAD", "ZIP"};
		
		final FileFilter WADFILTER = (file) ->
		{
			if (file.isDirectory())
				return true;
			String ext = FileUtils.getFileExtension(file).toUpperCase();
			return Arrays.binarySearch(FILETYPES, ext) >= 0;
		};
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final AtomicInteger addedCount = new AtomicInteger(0);
		final AtomicInteger updatedCount = new AtomicInteger(0);
		final List<File> filesToAdd = new LinkedList<>();
		
		final JLabel progressLabel = label("");
	
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("iwads.scan.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.NORTH, progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
		
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		taskManager.spawn(() -> 
		{
			signal.poll();
			DoomyCommon.scanAndListen(startDir, recurse, WADFILTER, (file) -> 
			{
				SwingUtils.invoke(() -> {
					progressLabel.setText(file.getPath());
				});
				
				String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
				if (iwadManager.containsIWAD(name) && force)
				{
					updatedCount.incrementAndGet();
					filesToAdd.add(file);
				}
				else if (!iwadManager.containsIWAD(name))
				{
					addedCount.incrementAndGet();
					filesToAdd.add(file);
				}
			});
			cancelProgressModal.dispose();
		});
	
		signal.offer(true); // alert thread
		
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
	
		if (cancelSwitch.get())
			return;
	
		final int totalCount = addedCount.get() + updatedCount.get();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressMessage = label("");
		progressLabel.setText("0%");
		
		SwingUtils.invoke(() -> {
			progressBar.setMinimum(0);
			progressBar.setIndeterminate(true);
			progressBar.setMaximum(totalCount);
		});
		
		if (totalCount > 0)
		{
			final Modal<Boolean> cancelProgressModal2 = modal(this, language.getText("iwads.scan.adding.title"), 
				containerOf(dimension(350, 48), borderLayout(8, 0),
					node(BorderLayout.NORTH, progressMessage),
					node(BorderLayout.CENTER, progressBar),
					node(BorderLayout.LINE_END, progressLabel)
				),
				gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
			);
			
			taskManager.spawn(() -> 
			{
				signal.poll();
	
				// Do update.
				AtomicInteger count = new AtomicInteger(0);
				
				for (File file : filesToAdd)
				{
					if (cancelSwitch.get())
						return;
	
					String name = (prefix + FileUtils.getFileNameWithoutExtension(file)).toLowerCase();
					if (iwadManager.containsIWAD(name) && force)
					{
						iwadManager.setIWADPath(name, file.getPath());
						int c = count.incrementAndGet();
						SwingUtils.invoke(() -> {
							progressMessage.setText(language.getText("iwads.scan.adding.updating", file.getPath()));
							progressBar.setValue(c);
							progressBar.setIndeterminate(false);
							progressLabel.setText((int)(((float)c / totalCount) * 100) + "%");
						});
					}
					else if (!iwadManager.containsIWAD(name))
					{
						iwadManager.addIWAD(name, file.getPath());
						int c = count.incrementAndGet();
						SwingUtils.invoke(() -> {
							progressMessage.setText(language.getText("iwads.scan.adding.adding", file.getPath()));
							progressBar.setValue(c);
							progressBar.setIndeterminate(false);
							progressLabel.setText((int)(((float)c / totalCount) * 100) + "%");
						});
					}
				}
				cancelProgressModal2.dispose();
			});
	
			signal.offer(true); // alert thread
			
			out = cancelProgressModal2.openThenDispose();
			if (out == Boolean.TRUE)
				cancelSwitch.set(true);
	
			iwadTable.refreshIWADs();
			
			if (cancelSwitch.get())
				return;
		}
		
		SwingUtils.info(this, language.getText("iwads.scan.adding.result", addedCount.get(), updatedCount.get()));
	}

	// Called on WAD cleanup.
	private void onCleanup()
	{
		IWAD[] allWads = iwadManager.getAllIWADs();
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final List<IWAD> missingWads = new LinkedList<>();
		
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
		
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("iwads.cleanup.scan.title"), 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, dimension(50, 1), progressLabel)
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		); 
		
		SwingUtils.invoke(() -> {
			progressBar.setMinimum(0);
			progressBar.setIndeterminate(true);
			progressBar.setMaximum(allWads.length);
		});
		
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		taskManager.spawn(() -> 
		{
			signal.poll();
			for (int i = 0; i < allWads.length; i++)
			{
				if (cancelSwitch.get())
					break;
				
				int progressVal = (int)(i / (float)(allWads.length) * 100f);
				final int prog = i;
				SwingUtils.invoke(() -> {
					progressBar.setValue(prog);
					progressBar.setIndeterminate(false);
					progressLabel.setText(String.valueOf(progressVal) + "%");
				});
				
				if (!new File(allWads[i].path).exists())
					missingWads.add(allWads[i]);
			}
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert task
		
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
	
		if (cancelSwitch.get())
			return;
		
		if (missingWads.isEmpty())
		{
			SwingUtils.info(this, language.getText("iwads.cleanup.scan.nomissing"));
			return;
		}
		
		List<String> missingWadNames = missingWads.stream().map((wad) -> wad.name).collect(Collectors.toList());
		final JScrollPane wadListPanel = scroll(ComponentFactory.list(missingWadNames));
		final JTextArea messageLabel = wrappedLabel(language.getText("iwads.cleanup.scan.remove.message", missingWads.size()));
		
		Boolean askModal = modal(this, language.getText("iwads.cleanup.scan.remove.title"), 
			containerOf(dimension(300, 175), borderLayout(0, 8),
				node(BorderLayout.NORTH, messageLabel),
				node(BorderLayout.CENTER, wadListPanel)
			),
			gui.createChoiceFromLanguageKey("iwads.cleanup.scan.remove.choice.yes", (Boolean)true),
			gui.createChoiceFromLanguageKey("iwads.cleanup.scan.remove.choice.no", (Boolean)false)
		).openThenDispose();
		
		if (askModal != Boolean.TRUE)
			return;
		
		for (String name : missingWadNames)
			iwadManager.removeIWAD(name);
		
		iwadTable.refreshIWADs();
		
		SwingUtils.info(this, language.getText("iwads.cleanup.scan.removed", missingWadNames.size()));
	}

	private void onOpen()
	{
		List<IWAD> selectedIWADs = iwadTable.getSelectedIWADs();
		if (selectedIWADs.isEmpty())
			return;
		
		IWAD selected = selectedIWADs.get(0);
		try {
			Desktop.getDesktop().open(new File(selected.path));
		} catch (IOException e) {
			SwingUtils.error(this, language.getText("iwads.open.error"));
		}
	}
	
	private void onSelection()
	{
		List<IWAD> selectedIWADs = iwadTable.getSelectedIWADs();
		removeAction.setEnabled(!selectedIWADs.isEmpty());
		cleanupAction.setEnabled(iwadManager.getIWADCount() > 0);
		openAction.setEnabled(selectedIWADs.size() == 1);
	}
	
}
