CREATE TABLE WADDependencies (
	wadId INTEGER NOT NULL,
	needsWadId INTEGER NOT NULL,
	UNIQUE (wadId, needsWadId),
	FOREIGN KEY (wadId) 
		REFERENCES WADs(id),
	FOREIGN KEY (needsWadId) 
		REFERENCES WADs(id)
)
