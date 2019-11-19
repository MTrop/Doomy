CREATE TABLE Preset (
	id INTEGER,
	hash TEXT NOT NULL,
	name TEXT NULL,
	engineName TEXT NOT NULL,
	iwadName TEXT NOT NULL,
	PRIMARY KEY (id),
	UNIQUE (hash),
	UNIQUE (name),
	FOREIGN KEY (engineName) 
		REFERENCES Engines(name),
	FOREIGN KEY (iwadName) 
		REFERENCES IWADs(name)
)
