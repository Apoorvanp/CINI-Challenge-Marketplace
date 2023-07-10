package it.univaq.se4gd.rec.marketplace.inventory

import it.univaq.se4gd.rec.marketplace.domain.InventoryItem
import khttp.async.Companion.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Timestamp

@Component
class InventoryService(val db: JdbcTemplate) {
    fun getCurrentInventory(communityId: Int?, houseId: Int?): List<InventoryItem> {
        if(communityId == null && houseId == null) {
            return db.query("select * from inventory", inventoryItemRowMapper)
        } else {
            return db.query(
                "select * from inventory where communityId=$communityId and houseId=$houseId",
                inventoryItemRowMapper
            )
        }
    }

    fun produce(communityId: Int, houseId: Int, energyProduced: Double, productionTime: Timestamp?): Double {
        val results = db.query("select * from inventory where houseId=$houseId and communityId=$communityId", inventoryItemRowMapper)
        if(results.size > 0) {
            val newCapacity = (results.first()?.energyProduced ?: 0.0) + energyProduced
            db.update("update inventory set energyProduced=$newCapacity where communityId=$communityId and houseId=$houseId")
        } else {
            db.update("insert into inventory values (?, ?, ?, ?)", communityId, houseId, energyProduced, Timestamp(System.currentTimeMillis()))
        }
        logProduction(communityId, houseId, energyProduced)
        return energyProduced
    }

    @Transactional
    fun consume(communityId: Int, houseId: Int, energyNeed: Double): List<Consumption> {
        val consumptions = consume(communityId, houseId, energyNeed, Level.HOUSE)
        logConsumptions(communityId, houseId, consumptions)
        return consumptions
    }

    fun consume(communityId: Int, houseId: Int, energyNeed: Double, level: Level): List<Consumption> {
        if(level == Level.HOUSE) {
            val results = db.query("select * from inventory where houseId=$houseId and communityId=$communityId", inventoryItemRowMapper)
            val energyAvailable = results.firstOrNull()?.energyProduced ?: 0.0
            return if(energyAvailable > energyNeed) {
                val energyLeft = energyAvailable - energyNeed
                db.update("update inventory set energyProduced=$energyLeft where communityId=$communityId and houseId=$houseId")
                listOf(Consumption(communityId, houseId, energyNeed, Level.HOUSE))
            } else {
                val energyLeft = 0.0
                db.update("update inventory set energyProduced=$energyLeft where communityId=$communityId and houseId=$houseId")
                val communityEnergy = consume(communityId, houseId, energyNeed - energyAvailable, Level.SAME_COMMUNITY)
                communityEnergy + Consumption(communityId, houseId, energyAvailable, Level.HOUSE)
            }
        } else if (level == Level.SAME_COMMUNITY) {
            val results = db.query("select * from inventory where houseId!=$houseId and communityId=$communityId", inventoryItemRowMapper)
            var currentEnergyNeed = energyNeed
            val consumptions = mutableListOf<Consumption>()
            for(result in results) {
                if(!(currentEnergyNeed > 0.0)) {
                    break
                }
                val energyAvailable = result.energyProduced
                if(energyAvailable > currentEnergyNeed) {
                    val energyLeft = energyAvailable - currentEnergyNeed
                    db.update("update inventory set energyProduced=$energyLeft where communityId=${result.communityId} and houseId=${result.houseId}")
                    consumptions.add(Consumption(result.communityId, result.houseId, currentEnergyNeed, Level.SAME_COMMUNITY))
                    currentEnergyNeed = 0.0
                } else {
                    val energyLeft = 0.0
                    db.update("update inventory set energyProduced=$energyLeft where communityId=${result.communityId} and houseId=${result.houseId}")
                    currentEnergyNeed = energyNeed - energyAvailable
                    consumptions.add(Consumption(result.communityId, result.houseId, energyAvailable, Level.SAME_COMMUNITY))
                }
            }
            if(currentEnergyNeed > 0.0) {
                return consumptions + consume(communityId, houseId, currentEnergyNeed, Level.OTHER_COMMUNITY)
            }
            return consumptions
        } else if(level == Level.OTHER_COMMUNITY) {
            val results = db.query("select * from inventory where communityId!=$communityId", inventoryItemRowMapper)
            var currentEnergyNeed = energyNeed
            val consumptions = mutableListOf<Consumption>()
            for(result in results) {
                if(!(currentEnergyNeed > 0.0)) {
                    break
                }
                val energyAvailable = result.energyProduced
                if(energyAvailable > currentEnergyNeed) {
                    val energyLeft = energyAvailable - currentEnergyNeed
                    db.update("update inventory set energyProduced=$energyLeft where communityId=${result.communityId} and houseId=${result.houseId}")
                    consumptions.add(Consumption(result.communityId, result.houseId, currentEnergyNeed, Level.OTHER_COMMUNITY))
                    currentEnergyNeed = 0.0
                } else {
                    val energyLeft = 0.0
                    db.update("update inventory set energyProduced=$energyLeft where communityId=${result.communityId} and houseId=${result.houseId}")
                    currentEnergyNeed = energyNeed - energyAvailable
                    consumptions.add(Consumption(result.communityId, result.houseId, energyAvailable, Level.OTHER_COMMUNITY))
                }
            }
            if(currentEnergyNeed > 0.0) {
                return consumptions + consume(communityId, houseId, currentEnergyNeed, Level.COMPANY)
            }
            return consumptions
        }
        return listOf(Consumption(-1, -1, energyNeed, Level.COMPANY))
    }


    val inventoryItemRowMapper: RowMapper<InventoryItem> = RowMapper<InventoryItem> { resultSet: ResultSet, _: Int ->
        InventoryItem(resultSet.getInt("communityId"), resultSet.getInt("houseId"), resultSet.getDouble("energyProduced"), resultSet.getTimestamp("productionTime"))
    }

    fun logConsumptions(communityId: Int, houseId: Int, consumptions: List<Consumption>) {
        val url = "http://consumption-log:8080/api/v1/consumptions"
        val headers = mapOf("Content-Type" to "application/json")

        val request = ConsumptionRequest(communityId, houseId, consumptions)

        val payload = Json.encodeToString(ConsumptionRequest.serializer(), request)

        val response = post(
            url = url,
            headers = headers,
            data = payload,
        ) {
            println("${this.statusCode}")
            println("${this.text}")
        }
    }

    fun logProduction(communityId: Int, houseId: Int, energyProduced: Double) {
        val url = "http://production-log:8080/api/v1/productions"
        val headers = mapOf("Content-Type" to "application/json")

        val request = ProductionRequest(communityId, houseId, energyProduced)

        val payload = Json.encodeToString(ProductionRequest.serializer(), request)

        val response = post(
            url = url,
            headers = headers,
            data = payload,
        ) {
            println("${this.statusCode}")
            println("${this.text}")
        }
    }
}

@Serializable
data class ConsumptionRequest(
    val buyerCommunityId: Int,
    val buyerHouseId: Int,
    val consumptions: List<Consumption>
)

@Serializable
data class ProductionRequest(
    val communityId: Int,
    val houseId: Int,
    val energyProduced: Double
)

@Serializable
data class Consumption(val communityId: Int, val houseId: Int, val consumed: Double, val from: Level)

enum class Level(val next: Level?) {
    COMPANY(null), OTHER_COMMUNITY(COMPANY), SAME_COMMUNITY(OTHER_COMMUNITY),HOUSE(SAME_COMMUNITY)
}