CREATE TABLE IF NOT EXISTS inventory (
    communityId INT NOT NULL,
    houseId INT NOT NULL,
    energyProduced DECIMAL NOT NULL,
    productionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (houseId, communityId)
);
