; DoomTools Inno Setup Installer Script

#define DTAppName "Doomy"
#define DTAppPublisher "MTrop"
#define DTAppURL "https://mtrop.github.io/Doomy/"
#define DTAppSupportURL "https://mtrop.github.io/Doomy/"
#define DTAppRepoReleaseURL "https://mtrop.github.io/Doomy/"
#define DTAppExeName "doomy-gui.exe"

; For notes - defined on compile via /D
; #define DTAppVersion "0000.00.00"
; #define SrcDirectory "C:\SOURCEDIR"
; #define SrcLicensePath "C:\SOURCEDIR\docs\LICENSE.txt"
; #define BaseOutputFilename "doomy-setup-versionnum"

#include "environment.iss"

[Setup]
; App GUID
AppId={{4A3A4755-89E7-430C-921B-79FF4F0D3A10}

AppName={#DTAppName}
AppVerName={#DTAppName}
AppVersion={#DTAppVersion}
AppPublisher={#DTAppPublisher}
AppPublisherURL={#DTAppURL}
AppSupportURL={#DTAppSupportURL}
AppUpdatesURL={#DTAppRepoReleaseURL}
DefaultDirName={autopf}\{#DTAppName}
DefaultGroupName={#DTAppName}
LicenseFile={#SrcLicensePath}
OutputBaseFilename={#BaseOutputFilename}
UninstallDisplayIcon={app}\{#DTAppExeName}
UninstallFilesDir={app}\uninst

ArchitecturesInstallIn64BitMode=x64
ChangesEnvironment=true
Compression=lzma
DisableWelcomePage=no
DisableProgramGroupPage=no
SetupIconFile=doomy-setup.ico
SolidCompression=yes
WizardStyle=modern
WizardImageFile=installer-image.bmp
WizardImageStretch=yes

AppCopyright=(C) 2019-2025 Matt Tropiano


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "envPath"; Description: "Add Doomy to System PATH (for using the Command Line)"; GroupDescription: "More Actions:"

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "{#SrcDirectory}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#DTAppName}";                       Filename: "{app}\{#DTAppExeName}"
Name: "{group}\{#DTAppName} Shell";                 Filename: "{app}\doomy.cmd"
Name: "{group}\{#DTAppName} Documentation";         Filename: "{app}\docs"; Flags: foldershortcut
Name: "{group}\{#DTAppName} Website";               Filename: "https://mtrop.github.io/Doomy/"
Name: "{group}\{cm:UninstallProgram,{#DTAppName}}"; Filename: {uninstallexe}
Name: "{autodesktop}\{#DTAppName}";                 Filename: "{app}\{#DTAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#DTAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(DTAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\jar\*.jar"


; The following is responsible for listening for the install steps to inject PATH changing, if selected.
[Code]
procedure CurStepChanged(CurStep: TSetupStep);
var
    appPath: String;
begin
    if (CurStep = ssPostInstall) then
    begin
        appPath := ExpandConstant('{app}');

        if WizardIsTaskSelected('envPath') then
        begin
            EnvAddPath(appPath);
        end;
    end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
    if (CurUninstallStep = usPostUninstall) then
    begin
        EnvRemovePath(ExpandConstant('{app}'));
    end;
end;
