CREATE TABLE Engines (
	id INTEGER,
	name TEXT NOT NULL,
	templateSource TEXT NULL,
	PRIMARY KEY (id),
	UNIQUE (name)
)
