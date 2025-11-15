package net.mtrop.doomy.gui.swing;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.doomfetch.FetchDriver;
import net.mtrop.doomy.doomfetch.FetchDriver.Response;
import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.MessengerManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.ComponentFactory.ProgressBarOrientation;
import net.mtrop.doomy.struct.util.HTTPUtils.HTTPResponse;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The control panel for the DoomFetch driver.
 */
public class DoomFetchControlPanel extends JPanel
{
	private static final long serialVersionUID = 5326859198339596748L;
	
	private final ConfigManager config;
	private final MessengerManager messenger;
	private final GUIManager gui;
	private final LanguageManager language;
	private final TaskManager taskManager;
	private final WADManager wadManager;

	private JFormField<String> searchField;
	private JFormField<String> driverField;
	private JFormField<Void> searchButtonField;
	
	public DoomFetchControlPanel()
	{
		this.config = ConfigManager.get();
		this.messenger = MessengerManager.get();
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		this.taskManager = TaskManager.get();
		this.wadManager = WADManager.get();
		
		this.searchField = stringField(false, true);
		this.driverField = comboField(comboBox(FetchDriver.DRIVER_LIST.keySet()));
		this.searchButtonField = buttonField(button(language.getText("doomfetch.search.button"), (b) -> onSearch()));
		
		containerOf(this, borderLayout(),
			node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("doomfetch.search.labelwidth")),
				gui.formField("doomfetch.search.for", searchField),
				gui.formField("doomfetch.driver", driverField),
				gui.formField("field.blank", searchButtonField)
			)),
			node(BorderLayout.CENTER, containerOf())
		);
	}
	
	private void onSearch()
	{
		String searchTerm = searchField.getValue();
		String driverName = driverField.getValue();
		
		if (ObjectUtils.isEmpty(searchTerm))
		{
			SwingUtils.error(this, language.getText("doomfetch.search.error.blanksearch"));
			return;
		}
		
		final TextOutputPanel textOutputPanel = new TextOutputPanel();
		final FetchDriver driver = FetchDriver.DRIVER_LIST.get(driverName).apply(textOutputPanel.getPrintStream(), textOutputPanel.getErrorPrintStream());
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("                 ");

		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final Modal<Boolean> searchModal = modal(this, language.getText("doomfetch.search.title"), 
			containerOf(borderLayout(0, 8),
				node(BorderLayout.CENTER, textOutputPanel),
				node(BorderLayout.SOUTH, containerOf(dimension(1, 24), borderLayout(8, 0),
					node(BorderLayout.CENTER, progressBar),
					node(BorderLayout.LINE_END, progressLabel)
				))
			),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)true)
		);
		
		final AtomicReference<Response> driverResponse = new AtomicReference<>();
		
		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(true);
		});
		
		final AtomicReference<File> targetFile = new AtomicReference<>();
		
		taskManager.spawn(() -> {
			signal.poll();

			// Do fetch.
			
			if (cancelSwitch.get())
				return;
			
			try {
				driverResponse.set(driver.getStreamFor(searchTerm));
			} catch (IOException e) {
				SwingUtils.error(this, language.getText("doomfetch.search.error.ioerror"));
			}
			
			Response response = driverResponse.get();
			if (response == null)
			{
				textOutputPanel.getPrintStream().println("No match.");
				SwingUtils.invoke(() -> {
					progressBar.setIndeterminate(false);
					progressBar.setMinimum(0);
					progressBar.setMaximum(1);
					progressBar.setValue(1);
				});
				return;
			}

			// Do download to temp folder.

			if (cancelSwitch.get())
				return;

			targetFile.set(new File(DoomyEnvironment.getTempDirectoryPath() + File.separator + response.getFilename()));
			textOutputPanel.getPrintStream().println("Downloading file...");
			
			try (HTTPResponse httpResponse = response.getHTTPResponse(); FileOutputStream fos = new FileOutputStream(targetFile.get()))
			{
				if (!httpResponse.isSuccess())
				{
					SwingUtils.error(this, language.getText("doomfetch.download.file.badresp", httpResponse.getStatusCode(), httpResponse.getStatusMessage()));
					searchModal.dispose();
					return;
				}
				
				AtomicLong currentBytes = new AtomicLong(0L);
				AtomicLong lastDate = new AtomicLong(System.currentTimeMillis());
				httpResponse.decode().relayContent(fos, (cur, max) -> 
				{
					if (cancelSwitch.get())
						return;
					
					long next = System.currentTimeMillis();
					currentBytes.set(cur);
					if (next > lastDate.get() + 250L)
					{
						SwingUtils.invoke(() -> 
						{
							if (max != null)
							{
								progressBar.setIndeterminate(false);
								progressBar.setMinimum(0);
								progressBar.setValue((int)cur);
								progressBar.setMaximum((int)(long)max);
								progressLabel.setText((cur/1024) + " KB / " + (max/1024) + " KB");
							}
							else
							{
								progressLabel.setText((cur/1024) + " KB");
							}
						});
						lastDate.set(next);
					}
				});

			} 
			catch (IOException e) 
			{
				SwingUtils.error(this, language.getText("doomfetch.download.file.ioerror", e.getLocalizedMessage()));
			}
			
			searchModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		searchModal.openThenDispose();
		if (searchModal.getValue() == Boolean.TRUE)
			cancelSwitch.set(true);
		
		if (cancelSwitch.get())
		{
			if (targetFile.get() != null)
				targetFile.get().delete();
			return;
		}
		
		if (driverResponse.get() == null)
			return;

		// ask for download destination and WAD name.
		
		File initDir = config.getConvertedValue(ConfigManager.SETTING_IDGAMES_DOWNLOAD_DIR, (value) -> value != null ? new File(value) : null);
		
		JFormField<File> destinationDirField = fileField(initDir, "...",
			(current) -> {
				File chosen = gui.chooseDirectory(this, 
					language.getText("doomfetch.copy.browse.file.title"), 
					language.getText("doomfetch.copy.browse.file.select"),
					() -> current != null ? current : gui.getDefaultFile(),
					(select) -> config.setValue(ConfigManager.SETTING_IDGAMES_DOWNLOAD_DIR, select != null ? select.getAbsolutePath() : null)
				);
				return chosen != null ? chosen : current;
			}
		);
		JFormField<String> wadNameField = stringField(FileUtils.getFileNameWithoutExtension(driverResponse.get().getFilename()), true, true);
		
		Boolean ok = modal(this, language.getText("doomfetch.copy.title"),
			containerOf(dimension(320, 80), borderLayout(),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("doomfetch.copy.labelwidth")), 
					gui.formField("doomfetch.copy.dest", destinationDirField),
					gui.formField("doomfetch.copy.wadname", wadNameField)
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
				SwingUtils.error(this, language.getText("doomfetch.copy.wadname.error"));
				return;
			}
		}

		// check destination.

		final File destinationDir = destinationDirField.getValue();
		if (destinationDir == null)
		{
			SwingUtils.error(this, language.getText("doomfetch.copy.dir.error"));
			return;
		}
		if (!destinationDir.exists())
		{
			SwingUtils.error(this, language.getText("doomfetch.copy.dir.notexist"));
			return;
		}
		if (!destinationDir.isDirectory())
		{
			SwingUtils.error(this, language.getText("doomfetch.copy.dir.notdir"));
			return;
		}
		
		// copy file to destination.
		
		final Modal<Boolean> cancelCopyModal = modal(this, language.getText("doomfetch.copy.dialog.title"), 
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

		final File outFile = new File(destinationDir.getAbsolutePath() + File.separator + driverResponse.get().getFilename());
		
		taskManager.spawn(() -> 
		{
			signal.poll();
			if (cancelSwitch.get())
				return;

			int buf = 0;
			int max = (int)targetFile.get().length();
			byte[] buffer = new byte[16384];
			final AtomicInteger current = new AtomicInteger(0);
			
			try (FileInputStream fis = new FileInputStream(targetFile.get()); FileOutputStream fos = new FileOutputStream(outFile))
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
				SwingUtils.error(this, language.getText("doomfetch.copy.dialog.notfound"));
				cancelSwitch.set(true);
			} 
			catch (IOException e) 
			{
				SwingUtils.error(this, language.getText("doomfetch.copy.dialog.ioerror", e.getLocalizedMessage()));
				cancelSwitch.set(true);
			}
	
			cancelCopyModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		Boolean out = cancelCopyModal.openThenDispose();
		if (out == Boolean.TRUE)
			cancelSwitch.set(true);
		
		targetFile.get().delete();
		
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
				SwingUtils.info(language.getText("doomfetch.copy.success.wad"));
				messenger.publish(MessengerManager.CHANNEL_WADS_CHANGED, true);
			}
		}
		else
		{
			SwingUtils.info(language.getText("doomfetch.copy.success"));
		}
	}
	
}
