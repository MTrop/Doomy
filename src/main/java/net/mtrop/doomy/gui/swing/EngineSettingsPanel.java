package net.mtrop.doomy.gui.swing;

import java.io.File;

import javax.swing.JPanel;

import net.mtrop.doomy.gui.swing.struct.FormFactory.JFormField;

/**
 * Engine settings panel.
 * @author Matthew Tropiano
 */
public class EngineSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 4101589295524923215L;

	/*
	SETTING_EXEPATH : String
	SETTING_DOSBOXPATH : String
	SETTING_DOSBOXCOMMANDLINE : String
	SETTING_SETUPFILENAME : String
	SETTING_SERVERFILENAME : String
	SETTING_WORKDIRPATH : String
	SETTING_FILESWITCH : String
	SETTING_IWADSWITCH : String
	SETTING_DEHSWITCH : String
	SETTING_DEHLUMPSWITCH : String
	SETTING_SAVEDIRSWITCH : String
	SETTING_SHOTDIRSWITCH : String
	SETTING_SAVEPATTERN : String
	SETTING_SHOTPATTERN : String
	SETTING_DEMOPATTERN : String
	SETTING_COMMANDLINE : String
	 */
	
	private JFormField<File> exePath; 
	private JFormField<File> dosboxPath; 
	private JFormField<String> dosboxCommandLine; 
	private JFormField<String> setupFilename; 
	private JFormField<String> serverFilename; 
	private JFormField<File> workingDirPath; 
	private JFormField<String> fileSwitch; 
	private JFormField<String> iwadSwitch; 
	private JFormField<String> dehSwitch; 
	private JFormField<String> dehlumpSwitch; 
	private JFormField<String> saveDirSwitch; 
	private JFormField<String> shotDirSwitch; 
	private JFormField<String> savegamePattern; 
	private JFormField<String> screenshotPattern; 
	private JFormField<String> demoPattern; 
	private JFormField<String> commandLine;
	
}
