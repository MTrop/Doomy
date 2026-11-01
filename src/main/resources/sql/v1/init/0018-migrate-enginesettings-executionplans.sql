INSERT INTO ExecutionPlans
	settings.engineId,
	settings.dosboxPresent AS planTypeId, -- 0 is RAW, 1 is DOSBOX
	settings.workDir,
	settings.packageName,
	settings.clientName,
	settings.setupName,
	settings.serverName,
	settings.commandLine
	FROM (SELECT
		e.id AS engineId,
		w.value AS workDir,
		(CASE WHEN d.name IS NULL THEN 0 ELSE 1 END) AS dosboxPresent,
		d.value AS packageName,
		c.value AS clientName,
		s.value AS setupName,
		v.value AS serverName,
		l.value AS commandLine
		FROM Engines e
		LEFT JOIN EngineSettings d ON e.id = d.engineId AND d.name = 'dosbox.path'
		LEFT JOIN EngineSettings w ON e.id = w.engineId AND w.name = 'work.dir'
		LEFT JOIN EngineSettings c ON e.id = c.engineId AND c.name = 'exe.path'
		LEFT JOIN EngineSettings s ON e.id = s.engineId AND s.name = 'setup.exe.name'
		LEFT JOIN EngineSettings v ON e.id = v.engineId AND v.name = 'server.exe.name'
		LEFT JOIN EngineSettings l ON e.id = l.engineId AND l.name = 'cmdline'
	) AS settings
