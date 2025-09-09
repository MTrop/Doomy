package net.mtrop.doomy.gui.swing;

import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.swing.ComponentFactory;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
	
	private WADManager wadManager;
	private TaskManager taskManager;
	
	private Action addAction;
	private Action removeAction;
	private Action scanAction;
	private Action cleanupAction;
	private WadTablePanel wadTable;
	
	/**
	 * Creates the WAD table control panel.
	 */
	public WadTableControlPanel()
	{
		this.wadManager = WADManager.get();
		this.taskManager = TaskManager.get();
		
		this.addAction = actionItem("Add...", (e) -> onAdd());
		this.removeAction = actionItem("Remove", (e) -> onRemove());
		this.scanAction = actionItem("Scan...", (e) -> onScan());
		this.cleanupAction = actionItem("Cleanup...", (e) -> onCleanup());
		this.wadTable = new WadTablePanel(this::onSelection);
		
		this.removeAction.setEnabled(false);
		this.cleanupAction.setEnabled(wadManager.getWADCount() > 0);

		containerOf(this, borderLayout(8, 0),
			node(BorderLayout.CENTER, wadTable),
			node(BorderLayout.EAST, containerOf(dimension(100, 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1),
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
		final JFormField<File> wadPathField = fileField(null, "Browse...", 
			(current) -> {
				File chosen = chooseFile(this, "Browse...", "Select", fileExtensionFilter("Doom Archives", "wad", "pk3", "pke", "zip", "pk7"));
				return chosen != null ? chosen : current;
			}, 
			(selected) -> {
				if (wadNameField.getValue().length() == 0)
					wadNameField.setValue(FileUtils.getFileNameWithoutExtension(selected));
			}
		);
		
		Boolean doAdd = modal(this, "Add WAD", containerOf(dimension(350, 64),
				node(form(LabelSide.LEADING, LabelJustification.LEADING, 50)
					.addField("Name", wadNameField)
					.addField("Path", wadPathField)
				)
			), 
			choice("Add", KeyEvent.VK_A, (Boolean)true),
			choice("Cancel", KeyEvent.VK_C, (Boolean)false)
		).openThenDispose();
		
		if (doAdd != Boolean.TRUE)
			return;
		
		String name = wadNameField.getValue();
		File path = wadPathField.getValue();
		
		if (ObjectUtils.isEmpty(name))
		{
			SwingUtils.error(this, "Name cannot be blank!");
			return;
		}

		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(this, "Path cannot be blank!");
			return;
		}

		if (path.isDirectory())
		{
			SwingUtils.error(this, "Path cannot be a directory!");
			return;
		}

		if (!path.exists())
		{
			SwingUtils.error(this, "Path does not exist!");
			return;
		}
		
		wadManager.addWAD(name, path.getAbsolutePath(), null);
		wadTable.refreshWADs();
	}
	
	// Called on WAD removal.
	private void onRemove()
	{
		final List<WAD> selected = wadTable.getSelectedWADs(); 
		if (SwingUtils.noTo(this, "Are you sure that you want to remove " + selected.size() + " WAD(s)?"))
			return;
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");

		final Modal<Boolean> cancelProgressModal = modal(this, "Removing WADs...", ModalityType.APPLICATION_MODAL, 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, progressLabel)
			),
			choice("Cancel", (Boolean)true)
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
		cancelProgressModal.openThenDispose();
		wadTable.refreshWADs();
	}
	
	// Called for scanning for new WADs.
	private void onScan()
	{
		final JFormField<String> prefixField = stringField("", false, true);
		final JFormField<Boolean> recurseField = checkBoxField(checkBox(true));
		final JFormField<Boolean> updateExistingField = checkBoxField(checkBox(false));
		final JFormField<File> directoryField = fileField(null, "Browse...", 
			(current) -> {
				File chosen = chooseDirectory(this, "Browse Diectory", "Select");
				return chosen != null ? chosen : current;
			}
		);
		
		Boolean doScan = modal(this, "Scan for WADs",
			containerOf(dimension(400, 120), borderLayout(0, 0),
				node(BorderLayout.NORTH, form(LabelSide.LEADING, LabelJustification.LEADING, 75)
					.addField("Path", directoryField)
					.addField("Prefix", prefixField)
					.addField("Recurse", recurseField)
					.addField("Update", updateExistingField)
				)
			),
			choice("Add", (Boolean)true),
			choice("Cancel", (Boolean)false)
		).openThenDispose();
		
		if (doScan != Boolean.TRUE)
			return;

		File startDir = directoryField.getValue();
		
		if (startDir == null)
		{
			SwingUtils.info(this, "No directory selected for scanning!");
			return;
		}
		else if (!startDir.exists())
		{
			SwingUtils.info(this, "Directory for scanning does not exist!");
			return;
		}
		else if (!startDir.isDirectory())
		{
			SwingUtils.info(this, "Selected path is not a directory");
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

		final Modal<Boolean> cancelProgressModal = modal(this, "Scanning WADs...", ModalityType.APPLICATION_MODAL, 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.NORTH, progressLabel)
			),
			choice("Cancel", (Boolean)true)
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
			final Modal<Boolean> cancelProgressModal2 = modal(this, "Adding/Updating WADs...", ModalityType.APPLICATION_MODAL, 
				containerOf(dimension(350, 48), borderLayout(8, 0),
					node(BorderLayout.NORTH, progressMessage),
					node(BorderLayout.CENTER, progressBar),
					node(BorderLayout.LINE_END, progressLabel)
				),
				choice("Cancel", (Boolean)true)
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
							progressMessage.setText("Updating " + file.getPath());
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
							progressMessage.setText("Adding " + file.getPath());
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
		
		SwingUtils.info(this, addedCount.get() + " file(s) added, " + updatedCount + " file(s) updated.");
	}
	
	// Called on WAD cleanup.
	private void onCleanup()
	{
		WAD[] allWads = wadManager.getAllWADs("");
		
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final List<WAD> missingWads = new LinkedList<>();
		
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
		
		final Modal<Boolean> cancelProgressModal = modal(this, "Scanning WADs...", ModalityType.APPLICATION_MODAL, 
			containerOf(dimension(350, 24), borderLayout(8, 0),
				node(BorderLayout.CENTER, progressBar),
				node(BorderLayout.LINE_END, dimension(50, 1), progressLabel)
			),
			choice("Cancel", (Boolean)true)
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
			SwingUtils.info(this, "All WADs accounted for. No WADs missing.");
			return;
		}
		
		List<String> missingWadNames = missingWads.stream().map((wad) -> wad.name).collect(Collectors.toList());
		final JScrollPane wadListPanel = scroll(ComponentFactory.list(missingWadNames));
		final JTextArea messageLabel = wrappedLabel("The following " + missingWads.size() + " WADs are missing. Are you sure you wish to remove them?");
		
		Boolean askModal = modal(this, "Remove Missing WADs", 
			containerOf(dimension(300, 175), borderLayout(0, 8),
				node(BorderLayout.NORTH, messageLabel),
				node(BorderLayout.CENTER, wadListPanel)
			), 
			choice("Yes", KeyEvent.VK_Y, (Boolean)true),
			choice("No", KeyEvent.VK_N, (Boolean)false)
		).openThenDispose();
		
		if (askModal != Boolean.TRUE)
			return;
		
		for (String name : missingWadNames)
			wadManager.removeWAD(name);
		
		wadTable.refreshWADs();
		
		SwingUtils.info(this, missingWadNames.size() + " WAD(s) removed.");
	}
	
	private void onSelection(DefaultListSelectionModel model, ListSelectionEvent event)
	{
		removeAction.setEnabled(!wadTable.getSelectedWADs().isEmpty());
		cleanupAction.setEnabled(wadManager.getWADCount() > 0);
	}
	
}
