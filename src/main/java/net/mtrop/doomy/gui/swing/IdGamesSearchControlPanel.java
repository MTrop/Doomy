package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.IconManager;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.FieldType;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesFileContent;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doomy.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTable;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;
import static net.mtrop.doomy.struct.swing.TableFactory.*;


/**
 * A search panel for the idGames Archive.
 */
public class IdGamesSearchControlPanel extends JPanel 
{
	private static final long serialVersionUID = 5500042924527901695L;

	private final GUIManager gui;
	private final LanguageManager language;
	private final IconManager icons;
	private final IdGamesManager idGames;
	
	private JFormField<String> searchField;
	private JFormField<FieldType> fieldTypeField;
	private JFormField<Void> searchButtonField;
	
	private JObjectTable<IdGamesFileContent> resultsTable;
	
	private Action downloadAction;
	private Action fileInfoAction;
	
	private JLabel statusLabel;
	
	public IdGamesSearchControlPanel()
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		this.icons = IconManager.get();
		this.idGames = IdGamesManager.get();
		
		this.searchField = stringField(false, true);
		this.fieldTypeField = comboField(comboBox(Arrays.asList(FieldType.values())));
		this.fieldTypeField.setValue(FieldType.FILENAME);
		this.searchButtonField = buttonField(button(language.getText("idgames.search.button"), (b) -> onSearch()));
		
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
					onDownload();
			}
		});
		
		this.resultsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
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
	
	private void onFileInfo()
	{
		// TODO Auto-generated method stub
	}

	private void onDownload()
	{
		// TODO Auto-generated method stub
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

		resultsTable.getTableModel().clear();
		printActivityStatus(language.getText("idgames.messages.fetch.search"));
		
		IdGamesSearchResponse response;
		try {
			response = idGames.searchBy(criteria, fieldType).get();
			
			if (response.error != null)
				printErrorStatus(language.getText("idgames.messages.error", response.error.message));
			else if (response.warning != null)
				printSuccessStatus(response.warning.message);
			else
				printSuccessStatus(language.getText("idgames.messages.done"));
			
			if (response.content != null)
				resultsTable.getTableModel().setRows(Arrays.asList(response.content.files));
			else
				resultsTable.getTableModel().clear();
			
		} catch (CancellationException e) {
			printErrorStatus(language.getText("idgames.messages.error.cancel"));
		} catch (InterruptedException e) {
			printErrorStatus(language.getText("idgames.messages.error.interrupt"));
		} catch (ExecutionException e) {
			printErrorStatus(language.getText("idgames.messages.error", e.getCause().getLocalizedMessage()));
		} catch (SocketTimeoutException e) {
			printErrorStatus(language.getText("idgames.messages.error.timeout"));
		} catch (IOException e) {
			printErrorStatus(language.getText("idgames.messages.error.io"));
		}
	}
	
	private void onSelection()
	{
		List<IdGamesFileContent> selected = resultsTable.getSelectedObjects();
		downloadAction.setEnabled(selected.size() == 1);
		fileInfoAction.setEnabled(selected.size() == 1);
	}

}
