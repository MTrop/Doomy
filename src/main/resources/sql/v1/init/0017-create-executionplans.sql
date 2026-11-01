CREATE TABLE ExecutionPlans (
	engineId INTEGER,
	planTypeId INTEGER NOT NULL,          -- Enum ordinal in Doomy
	workDir TEXT NULL,                    -- If RAW or DOSBOX, use parent if NULL, this dir if present for working dir.
	packageName TEXT NULL,                -- NULL if RAW, path to DOSBOX if DOSBOX, package name if image runner
	clientName TEXT NOT NULL,             -- Path to exe if RAW or DOSBOX, app client/main image name if image runner
	setupName TEXT NULL,                  -- Exe name RAW or DOSBOX, app setup image name if image runner
	serverName TEXT NULL,                 -- Exe name if RAW or DOSBOX, app server image name if image runner
	commandLine TEXT NOT NULL DEFAULT '', -- If not empty, this command line is appended (but before the as-is passed-in options).
	PRIMARY KEY (engineId),
	FOREIGN KEY (engineId)
		REFERENCES Engines(id)
			ON DELETE CASCADE
			ON UPDATE CASCADE
)
