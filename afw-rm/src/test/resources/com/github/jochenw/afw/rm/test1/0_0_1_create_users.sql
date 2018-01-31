-- @Resource(version="0.0.1", type="sql", description="Create Table Users" title="Create_Table_Users")

DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
	ID BIGINT NOT NULL PRIMARY KEY,
	UID VARCHAR(32) NOT NULL UNIQUE,
	EMAIL VARCHAR(256) NOT NULL UNIQUE,
);

	
	