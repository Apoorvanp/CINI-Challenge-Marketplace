package it.univaq.se4gd.rec.marketplace.production

import it.univaq.se4gd.rec.marketplace.domain.InventoryItem
import org.springframework.stereotype.Component
import java.sql.ResultSet
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.Timestamp

@Component
class ProductionLogService(val db: JdbcTemplate) {
    fun logProduction(inventoryItem: InventoryItem): InventoryItem {
        val timestamp = Timestamp(System.currentTimeMillis())
        db.update("insert into productions values (?, ?, ?, ?)", inventoryItem.communityId, inventoryItem.houseId, inventoryItem.energyProduced,
            timestamp
        )
        return inventoryItem.copy(productionTime = timestamp)
    }

    fun getTransactions(communityId: Int, houseId: Int, numberOfTransactions: Int): List<InventoryItem> {
            return db.query("select * from productions where communityId=$communityId and houseId=$houseId ORDER BY productionTime DESC limit $numberOfTransactions", inventoryItemRowMapper)
    }
    val inventoryItemRowMapper: RowMapper<InventoryItem> = RowMapper<InventoryItem> { resultSet: ResultSet, _: Int ->
        InventoryItem(resultSet.getInt("communityId"), resultSet.getInt("houseId"), resultSet.getDouble("energyProduced"), resultSet.getTimestamp("productionTime"))
    }
}