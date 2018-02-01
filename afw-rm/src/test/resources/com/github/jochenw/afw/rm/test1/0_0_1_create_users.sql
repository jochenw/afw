-- @Resource(version="0.0.1", type="sql", description="Create Table Users" title="Create_Table_Users")

DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
	id BIGINT NOT NULL PRIMARY KEY,
	uid VARCHAR(32) NOT NULL,
	email VARCHAR(256) NOT NULL
);
ALTER TABLE Users ADD CONSTRAINT UX_Users_Uid UNIQUE(uid);
ALTER TABLE Users ADD CONSTRAINT UX_Users_Email UNIQUE(email);
