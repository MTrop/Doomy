DELETE FROM EngineTemplateSettings 
	WHERE name IN (
		'dosbox.path'
		'work.dir'
		'exe.path'
		'setup.exe.name'
		'server.exe.name'
		'cmdline'
	)
