CREATE TABLE PresetItems (
	id INTEGER,
	presetId INTEGER NOT NULL,
	wadId INTEGER NOT NULL,
	sort INTEGER NOT NULL,
	PRIMARY KEY (id),
	UNIQUE (presetId, wadId),
	FOREIGN KEY (presetId) 
		REFERENCES Preset(id)
)
