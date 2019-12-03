-- exe.path: filepath					// Engine executable path.
-- dosbox.path: filepath				// [OPT] If present (and not empty), starts DOSBox, mounts temp, and calls the EXE via it.
-- setup.exe.name: filename				// [OPT] If present, "engine setup" will run this executable in the engine's parent directory.
-- server.exe.name: filename			// [OPT] If present, "run --server" will run this executable in the engine's parent directory.
-- work.dir: path						// [OPT] If NOT present (or empty), set to either DOSBox dir or the EXE parent.
-- switch.iwad: string					// [OPT] If present (and not empty), this engine requires an IWAD and this is the switch for loading it.
-- switch.file: string					// The switch to use for loading PWAD data (might be "-merge" if Chocolate Doom).
-- switch.dehacked: string				// [OPT] The switch to use for loading DeHackEd patches (blank for unsupported).
-- switch.dehlump: string				// [OPT] The switch to use for loading DeHackEd lumps (blank for unsupported).
-- switch.save.dir: string				// [OPT] If present (and not empty), this switch is used to map to preset directories for saves.
-- switch.screenshots.dir: string		// [OPT] If present (and not empty), this switch is used to map to preset directories for screenshots.
-- regex.screenshots: string			// [OPT] If present (and not empty), this regex pattern is used to find screenshot files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir.
-- regex.demos: string					// [OPT] If present (and not empty), this regex pattern is used to find demo files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir.
-- regex.saves: string					// [OPT] If present (and not empty), this regex pattern is used to find savegame files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir.
-- cmdline: string						// [OPT] If present (and not empty), this command line is appended (but before the as-is passed-in options).

CREATE TABLE EngineSettings (
	engineId INTEGER,
	name TEXT NOT NULL COLLATE NOCASE,
	value TEXT NULL,
	PRIMARY KEY (engineId, name),
	FOREIGN KEY (engineId) 
		REFERENCES Engines(id)
)
