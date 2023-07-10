CREATE TABLE IF NOT EXISTS consumptions (
    buyerCommunityId INT NOT NULL,
    buyerHouseId INT NOT NULL,
    sellerCommunityId INT NOT NULL,
    sellerHouseId INT NOT NULL,
    energyConsumed DECIMAL NOT NULL,
    consumptionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
