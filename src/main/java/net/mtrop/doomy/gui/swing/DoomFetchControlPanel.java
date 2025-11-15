package net.mtrop.doomy.gui.swing;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.doomfetch.FetchDriver;
import net.mtrop.doomy.doomfetch.FetchDriver.Response;
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
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The control panel for the DoomFetch driver.
 */
public class DoomFetchControlPanel extends JPanel
{
	private static final long serialVersionUID = 5326859198339596748L;
	
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

		// TODO: Do copy - get final destination and WAD name.
		
	}
	
}
