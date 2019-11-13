CREATE TABLE Config (
	id INTEGER,
	name TEXT NOT NULL,
	value TEXT NULL,
	PRIMARY KEY (id),
	UNIQUE (name)
);
