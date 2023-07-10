CREATE TABLE IF NOT EXISTS productions (
    communityId INT NOT NULL,
    houseId INT NOT NULL,
    energyProduced DECIMAL NOT NULL,
    productionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
