CREATE TABLE Player (
    Username VARCHAR(50) PRIMARY KEY,
    Password VARCHAR(100) NOT NULL,
    ChampFound INT,
    AverageTry FLOAT
);

CREATE TABLE LOLChamp (
    Name VARCHAR(50) PRIMARY KEY,
    Gender VARCHAR(20) NOT NULL,
    Resource VARCHAR(50) NOT NULL,
    RangeType VARCHAR(20) NOT NULL,
    ReleaseYear INT NOT NULL
);

CREATE TABLE Assoc (
    ChampName VARCHAR(50),
    Type VARCHAR(20),    -- 'Position', 'Class', 'Species', 'Region'
    Name VARCHAR(50),
    PRIMARY KEY (ChampName, Type, Name),
    FOREIGN KEY (ChampName) REFERENCES LOLChamp(Name) ON DELETE CASCADE
);
