package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.managers.EngineConfigManager;
import net.mtrop.doomy.managers.EngineConfigManager.EngineSettings;
import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.TaskManager;
import net.mtrop.doomy.struct.swing.ComponentFactory.ProgressBarOrientation;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

public class EngineTableControlPanel extends JPanel 
{
	private static final long serialVersionUID = -4932594840213904302L;

	private final GUIManager gui;
	private final EngineManager engineManager;
	private final EngineConfigManager engineConfigManager;
	private final TaskManager taskManager;
	private final LanguageManager language;
	
	private EngineTablePanel engineTable;
	private Action addAction;
	private Action copyAction;
	private Action editAction;
	private Action removeAction;
	private Action openFolderAction;

	public EngineTableControlPanel()
	{
		this.gui = GUIManager.get();
		this.engineManager = EngineManager.get();
		this.engineConfigManager = EngineConfigManager.get();
		this.taskManager = TaskManager.get();
		this.language = LanguageManager.get();
		
		this.engineTable = new EngineTablePanel(SelectionPolicy.MULTIPLE_INTERVAL, (model, event) -> onSelection(), (event) -> onOpen());

		this.addAction = actionItem(language.getText("engine.add"), (e) -> onAdd());
		this.copyAction = actionItem(language.getText("engine.copy"), (e) -> onCopy());
		this.editAction = actionItem(language.getText("engine.edit"), (e) -> onEdit());
		this.removeAction = actionItem(language.getText("engine.remove"), (e) -> onRemove());
		this.openFolderAction = actionItem(language.getText("engine.open"), (e) -> onOpen());

		onSelection();

		containerOf(this, borderLayout(8, 0),
			node(BorderLayout.CENTER, engineTable),
			node(BorderLayout.EAST, containerOf(dimension(language.getInteger("engine.actions.width"), 1), borderLayout(),
				node(BorderLayout.NORTH, containerOf(gridLayout(0, 1, 0, 2),
					node(button(openFolderAction)),
					node(button(addAction)),
					node(button(copyAction)),
					node(button(editAction)),
					node(button(removeAction))
				)),
				node(BorderLayout.CENTER, containerOf())
			))
		);
	}

	private void addEngine(String name, EngineSettings settings)
	{
		if (ObjectUtils.isEmpty(name))
		{
			SwingUtils.error(this, language.getText("engine.add.error.name.blank"));
			return;
		}
	
		if (engineManager.getEngine(name) != null)
		{
			SwingUtils.error(this, language.getText("engine.add.error.name.exists"));
			return;
		}
	
		if (ObjectUtils.isEmpty(settings.exePath))
		{
			SwingUtils.error(this, language.getText("engine.add.error.path.blank"));
			return;
		}
	
		File path = new File(settings.exePath);
		
		if (!path.exists())
		{
			SwingUtils.error(this, language.getText("engine.add.error.path.noexist"));
			return;
		}
		
		if (path.isDirectory())
		{
			SwingUtils.error(this, language.getText("engine.add.error.path.dir"));
			return;
		}
		
		if (ObjectUtils.isEmpty(settings.fileSwitch))
		{
			SwingUtils.error(this, language.getText("engine.add.error.switch.file"));
			return;
		}
	
		engineManager.addEngine(name);
		if (!engineConfigManager.setEngineSettings(name, settings))
		{
			SwingUtils.error(this, language.getText("engine.add.error.settings.save"));
			engineManager.removeEngine(name);
		}
	}

	// Called when adding an Engine.
	private void onAdd()
	{
		final JFormField<String> engineNameField = stringField(false, true);
		EngineSettingsEditorPanel editorPanel = new EngineSettingsEditorPanel(new EngineSettings());
		
		Boolean doAdd = modal(this, language.getText("engine.add.title"), containerOf(dimension(400, 675), borderLayout(),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("engine.settings.labelwidth")), 
					gui.formField("engine.add.form.name", engineNameField)
				)),
				node(BorderLayout.CENTER, editorPanel)
			),
			gui.createChoiceFromLanguageKey("engine.add.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doAdd != Boolean.TRUE)
			return;
		
		String name = engineNameField.getValue();
		EngineSettings settings = editorPanel.getSettings();
		
		addEngine(name, settings);
		
		engineTable.refreshEngines();
	}

	// Called when copying an engine.
	private void onCopy()
	{
		Engine source = engineTable.getSelectedEngines().get(0);
		EngineSettings settings = engineConfigManager.getEngineSettings(source.name);
		
		final JFormField<String> engineNameField = stringField(false, true);
		EngineSettingsEditorPanel editorPanel = new EngineSettingsEditorPanel(new EngineSettings(settings));
		
		Boolean doCopy = modal(this, language.getText("engine.copy.title"), containerOf(dimension(400, 660), borderLayout(),
				node(BorderLayout.NORTH, gui.createForm(form(LabelSide.LEADING, LabelJustification.LEADING, language.getInteger("engine.settings.labelwidth")), 
					gui.formField("engine.copy.form.name", engineNameField)
				)),
				node(BorderLayout.CENTER, editorPanel)
			),
			gui.createChoiceFromLanguageKey("engine.copy.choice.add", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doCopy != Boolean.TRUE)
			return;
		
		String name = engineNameField.getValue();
		settings = editorPanel.getSettings();
		
		addEngine(name, settings);
		
		engineTable.refreshEngines();
	}
	
	// Called when editing an engine.
	private void onEdit()
	{
		Engine source = engineTable.getSelectedEngines().get(0);
		EngineSettings settings = engineConfigManager.getEngineSettings(source.name);
		
		EngineSettingsEditorPanel editorPanel = new EngineSettingsEditorPanel(new EngineSettings(settings));
		
		Boolean doSave = modal(this, language.getText("engine.edit.title"), containerOf(dimension(400, 660), borderLayout(),
				node(BorderLayout.NORTH, editorPanel)
			),
			gui.createChoiceFromLanguageKey("engine.edit.choice.save", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (doSave != Boolean.TRUE)
			return;
		
		String name = source.name;
		settings = editorPanel.getSettings();
		
		if (!engineConfigManager.setEngineSettings(name, settings))
		{
			SwingUtils.error(this, language.getText("engine.edit.error.settings.save"));
		}
		
		engineTable.refreshEngines();
	}
	
	// Called when removing engines.
	private void onRemove()
	{
		final List<Engine> selected = engineTable.getSelectedEngines();
		if (SwingUtils.noTo(this, language.getText("engine.remove.message", selected.size())))
			return;

		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final BlockingQueue<Boolean> signal = new LinkedBlockingQueue<>();
		final JProgressBar progressBar = progressBar(ProgressBarOrientation.HORIZONTAL);
		final JLabel progressLabel = label("0%");
	
		final Modal<Boolean> cancelProgressModal = modal(this, language.getText("engine.remove.title"), 
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
			for (Engine engine : selected)
			{
				if (cancelSwitch.get())
					return;
	
				final int count = c;
				SwingUtils.invoke(() -> {
					progressBar.setValue(count);
					progressBar.setIndeterminate(false);
					progressLabel.setText((int)((float)count / selected.size() * 100) + "%");
				});
				
				engineManager.removeEngine(engine.name);
				c++;
			}
			cancelProgressModal.dispose();
		});
		
		signal.offer(true); // alert thread.
		cancelProgressModal.openThenDispose();
		engineTable.refreshEngines();
	}
	
	// Called when opening an engine's folder.
	private void onOpen()
	{
		List<Engine> selectedEngines = engineTable.getSelectedEngines();
		if (selectedEngines.isEmpty())
			return;
		
		Engine source = selectedEngines.get(0);
		EngineSettings settings = engineConfigManager.getEngineSettings(source.name);
		DoomyCommon.openInSystemBrowser(new File(settings.exePath));
	}
	
	private void onSelection()
	{
		List<Engine> engines = engineTable.getSelectedEngines();
		removeAction.setEnabled(!engines.isEmpty());
		copyAction.setEnabled(engines.size() == 1);
		editAction.setEnabled(engines.size() == 1);
		openFolderAction.setEnabled(engines.size() == 1);
	}
	
}
