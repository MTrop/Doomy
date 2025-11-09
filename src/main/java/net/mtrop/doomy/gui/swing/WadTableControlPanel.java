/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.swing.ComponentFactory;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.IOUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.FileChooserFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * The WAD control panel.
 */
public class WadTableControlPanel extends JPanel
{
	private static final long serialVersionUID = -8553950836856607413L;
	
	private final GUIManager gui;
	private final WADManager wadManager;
	private final TaskManager taskManager;
	private final LanguageManager language;
	
	private WadTablePanel wadTable;
	private Action addAction;
	private Action removeAction;
	private Action scanAction;
	private Action cleanupAction;
	private Action textAction;
	private Action openAction;
	
	/**
	 * Creates the WAD table control panel.
	 */
	public WadTableControlPanel()
	{
		this.gui = GUIManager.get();
		this.wadManager = WADManager.get();
		this.taskManager = TaskManager.get();
		this.language = LanguageManager.get();
		
		this.wadTable = new WadTablePanel(SelectionPolicy.MULTIPLE_INTERVAL, (model, event) -> onSelection(), (event) -> onOpen());

		this.addAction = actionItem(language.getText("wads.add"), (e) -> onAdd());
		this.removeAction = actionItem(language.getText("wads.remove"), (e) -> onRemove());
		this.scanAction = actionItem(language.getText("wads.scan"), (e) -> onScan());
		this.cleanupAction = actionItem(language.getText("wads.cleanup"), (e) -> onCleanup());
		this.textAction = actionItem(language.getText("wads.text"), (e) -> onTextFile());
		this.openAction = actionItem(language.getText("wads.open"), (e) -> onOpen());
		
		onSelection();

		containerOf(this, borderLayout(8, 0),
			node(BorderLayout.CENTER, wadTable),
			node(BorderLayout.EAST, containerOf(dimension(language.getInteger("wads.actions.width"), 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1, 0, 2),
					node(button(openAction)),
					node(button(addAction)),
					node(button(textAction)),
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
		
		Boolean doAdd = modal(this, language.getText("wads.add.title"), containerOf(dimension(350, 64),
				node(gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("wads.add.form.width")), 
					gui.formField("wads.add.form.name", wadNameField),
					gui.formField("wads.add.form.path", wadPathField)
				))
			),
			gui.createChoiceFromLanguageKey("wads.add.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doAdd != Boolean.TRUE)
			return;
		
		String name = wadNameField.getValue();
		File path = wadPathField.getValue();
		
		if (ObjectUtils.isEmpty(name))
		{
			SwingUtils.error(this, language.getText("wads.add.error.name.blank"));
			return;
		}

		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(this, language.getText("wads.add.error.path.blank"));
			return;
		}

		if (path.isDirectory())
		{
			SwingUtils.error(this, language.getText("wads.add.error.path.notdir"));
			return;
		}

		if (!path.exists())
		{
			SwingUtils.error(this, language.getText("wads.add.error.path.noexist"));
			return;
		}
		
		wadManager.addWAD(name, path.getAbsolutePath(), null);
		wadTable.refreshWADs();
	}
	
	// Called on WAD removal.
	private void onRemove()
	{
		final List<WAD> selected = wadTable.getSelectedWADs(); 
		if (SwingUtils.noTo(this, language.getText("wads.remove.message", selected.size())))
			return;
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");

		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("wads.remove.title"), 
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
			for (WAD wad : selected)
			{
				if (cancelSwitch.get())
					return;

				final int count = c;
				SwingUtils.invoke(() -> {
					progressBar.setValue(count);
					progressBar.setIndeterminate(false);
					progressLabel.setText((int)((float)count / selected.size() * 100) + "%");
				});
				
				wadManager.removeWAD(wad.name);
				c++;
			}
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelProgressModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);

		wadTable.refreshWADs();
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
		
		Boolean doScan = modal(this, language.getText("wads.scan.form.title"),
			containerOf(dimension(400, 120), borderLayout(0, 0),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("wads.scan.form.width")),
					gui.formField("wads.scan.form.path", directoryField),
					gui.formField("wads.scan.form.prefix", prefixField),
					gui.formField("wads.scan.form.recurse", recurseField),
					gui.formField("wads.scan.form.update", updateExistingField)
				))
			),
			gui.createChoiceFromLanguageKey("wads.scan.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doScan != Boolean.TRUE)
			return;

		File startDir = directoryField.getValue();
		
		if (startDir == null)
		{
			SwingUtils.info(this, language.getText("wads.scan.error.path.blank"));
			return;
		}
		else if (!startDir.exists())
		{
			SwingUtils.info(this, language.getText("wads.scan.error.path.noexist"));
			return;
		}
		else if (!startDir.isDirectory())
		{
			SwingUtils.info(this, language.getText("wads.scan.error.path.notdir"));
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

		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("wads.scan.title"), 
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
				if (wadManager.containsWAD(name) && force)
				{
					updatedCount.incrementAndGet();
					filesToAdd.add(file);
				}
				else if (!wadManager.containsWAD(name))
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
			final Modal<Boolean> cancelProgressModal2 = modal(this, language.getText("wads.scan.adding.title"), 
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
					if (wadManager.containsWAD(name) && force)
					{
						wadManager.setWADPath(name, file.getPath());
						int c = count.incrementAndGet();
						SwingUtils.invoke(() -> {
							progressMessage.setText(language.getText("wads.scan.adding.updating", file.getPath()));
							progressBar.setValue(c);
							progressBar.setIndeterminate(false);
							progressLabel.setText((int)(((float)c / totalCount) * 100) + "%");
						});
					}
					else if (!wadManager.containsWAD(name))
					{
						wadManager.addWAD(name, file.getPath());
						int c = count.incrementAndGet();
						SwingUtils.invoke(() -> {
							progressMessage.setText(language.getText("wads.scan.adding.adding", file.getPath()));
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

			wadTable.refreshWADs();
			
			if (cancelSwitch.get())
				return;
		}
		
		SwingUtils.info(this, language.getText("wads.scan.adding.result", addedCount.get(), updatedCount.get()));
	}
	
	// Called on WAD cleanup.
	private void onCleanup()
	{
		WAD[] allWads = wadManager.getAllWADs();
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final List<WAD> missingWads = new LinkedList<>();
		
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
		
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("wads.cleanup.scan.title"), 
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
			SwingUtils.info(this, language.getText("wads.cleanup.scan.nomissing"));
			return;
		}
		
		List<String> missingWadNames = missingWads.stream().map((wad) -> wad.name).collect(Collectors.toList());
		final JScrollPane wadListPanel = scroll(ComponentFactory.list(missingWadNames));
		final JTextArea messageLabel = wrappedLabel(language.getText("wads.cleanup.scan.remove.message", missingWads.size()));
		
		Boolean askModal = modal(this, language.getText("wads.cleanup.scan.remove.title"), 
			containerOf(dimension(300, 175), borderLayout(0, 8),
				node(BorderLayout.NORTH, messageLabel),
				node(BorderLayout.CENTER, wadListPanel)
			),
			gui.createChoiceFromLanguageKey("wads.cleanup.scan.remove.choice.yes", (Boolean)true),
			gui.createChoiceFromLanguageKey("wads.cleanup.scan.remove.choice.no", (Boolean)false)
		).openThenDispose();
		
		if (askModal != Boolean.TRUE)
			return;
		
		for (String name : missingWadNames)
			wadManager.removeWAD(name);
		
		wadTable.refreshWADs();
		
		SwingUtils.info(this, language.getText("wads.cleanup.scan.removed", missingWadNames.size()));
	}
	
	private void onTextFile()
	{
		WAD selectedWAD = wadTable.getSelectedWADs().get(0);
		
		if (!(new File(selectedWAD.path)).exists())
		{
			SwingUtils.error(language.getText("wads.text.error.nofile", selectedWAD.path));
			return;
		}
		
		File wadFile = new File(selectedWAD.path);
		
		String filename = FileUtils.getFileNameWithoutExtension(wadFile.getName());
		String textFileName = filename + ".txt";
		
		File text = new File((new File(selectedWAD.path)).getParent() + File.separator + filename + ".txt");
		if (!text.exists())
		{
			try (ZipFile zf = new ZipFile(selectedWAD.path))
			{
				ZipEntry entry = ObjectUtils.isNull(zf.getEntry(textFileName), zf.getEntry(textFileName.toUpperCase()));
				if (entry != null)
				{
					try (InputStream textIn = zf.getInputStream(entry))
					{
						displayTextFile(entry.getName(), textIn);
					} 
				}
				else
				{
					SwingUtils.info(language.getText("wads.text.error.notext", textFileName));
				}
			}
			catch (ZipException e) 
			{
				SwingUtils.error(language.getText("wads.text.error.badzip", selectedWAD.path));
			} 
			catch (IOException e) 
			{
				SwingUtils.error(language.getText("wads.text.error.iozip", selectedWAD.path));
			} 
			
		}
		else
		{
			try (FileInputStream textIn = new FileInputStream(text))
			{
				displayTextFile(text.getName(), textIn);
			} 
			catch (FileNotFoundException e) 
			{
				SwingUtils.info(language.getText("wads.text.error.notext", text.getPath()));
			} 
			catch (IOException e) 
			{
				SwingUtils.info(language.getText("wads.text.error.ioerror", text.getPath()));
			} 
		}
	}
	
	private void displayTextFile(String textFileName, InputStream inStream)
	{
		StringWriter sw = new StringWriter();
		try (Reader reader = new BufferedReader(new InputStreamReader(inStream, "IBM437")))
		{
			IOUtils.relay(reader, sw);
		}
		catch (UnsupportedEncodingException e) 
		{
			SwingUtils.error(language.getText("wads.text.error.decode", textFileName));
		} 
		catch (IOException e) 
		{
			SwingUtils.info(language.getText("wads.text.error.ioerror", textFileName));
		}
		
		JTextArea textArea = textArea(25, 80);
		textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
		textArea.setEditable(false);
		textArea.setText(sw.toString());
		textArea.setCaretPosition(0);
		
		modal(this, textFileName,
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, scroll(textArea))
			)
		).openThenDispose();
	}
	
	private void onOpen()
	{
		List<WAD> selectedWADs = wadTable.getSelectedWADs();
		if (selectedWADs.isEmpty())
			return;
		
		WAD selected = selectedWADs.get(0);
		try {
			Desktop.getDesktop().open(new File(selected.path));
		} catch (IOException e) {
			SwingUtils.error(this, language.getText("wads.open.error"));
		}
	}
	
	private void onSelection()
	{
		List<WAD> selectedWADs = wadTable.getSelectedWADs();
		removeAction.setEnabled(!selectedWADs.isEmpty());
		cleanupAction.setEnabled(wadManager.getWADCount() > 0);
		textAction.setEnabled(selectedWADs.size() == 1);
		openAction.setEnabled(selectedWADs.size() == 1);
	}
	
}
