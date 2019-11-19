CREATE TABLE EngineTemplateSettings (
	engineTemplateId INTEGER,
	name TEXT NOT NULL,
	value TEXT NULL,
	PRIMARY KEY (engineTemplateId, name),
	FOREIGN KEY (engineTemplateId) 
		REFERENCES EngineTemplates(id)
)
