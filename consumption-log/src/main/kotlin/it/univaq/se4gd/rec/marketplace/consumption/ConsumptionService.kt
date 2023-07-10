package it.univaq.se4gd.rec.marketplace.consumption

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Timestamp

val SAME_COMMUNITY_PRICE = 1.0
val OTHER_COMMUNITY_PRICE = 2.0
val COMPANY_PRICE = 3.0

@Component
class ConsumptionService(val db: JdbcTemplate) {
    val consumptionHistoryRowMapper: RowMapper<ConsumptionHistory> = RowMapper<ConsumptionHistory> { resultSet: ResultSet, _: Int ->
        ConsumptionHistory(resultSet.getInt("sellerCommunityId"), resultSet.getInt("sellerHouseId"), resultSet.getInt("buyerCommunityId"), resultSet.getInt("buyerHouseId"), resultSet.getDouble("energyConsumed"), resultSet.getDouble("energyConsumed"), resultSet.getTimestamp("consumptionTime"))
    }

    @Transactional
    fun consume(buyerCommunityId: Int, buyerHouseId: Int, consumptions: List<Consumption>): List<Consumption> {
        consumptions.filter { it.consumed > 0.0 }
                .forEach {
                    db.update("insert into consumptions values (?, ?, ?, ?, ?, ?)", buyerCommunityId, buyerHouseId, it.communityId, it.houseId, it.consumed, Timestamp(System.currentTimeMillis()))
                }
        return consumptions
    }

    fun getConsumptionHistory(buyerCommunityId: Int, buyerHouseId: Int): List<ConsumptionHistory> {
        val consumptionHistories = db.query("select * from consumptions where buyerCommunityId=$buyerCommunityId and buyerHouseId=$buyerHouseId ORDER BY consumptionTime DESC", consumptionHistoryRowMapper)
        return updatePrice(buyerCommunityId, buyerHouseId, consumptionHistories)
    }

    fun getCreditHistory(sellerCommunityId: Int, sellerHouseId: Int): List<ConsumptionHistory> {
        val creditHistories = db.query("select * from consumptions where sellerCommunityId=$sellerCommunityId and sellerHouseId=$sellerHouseId ORDER BY consumptionTime DESC", consumptionHistoryRowMapper)
        return updateCredit(sellerCommunityId, sellerHouseId, creditHistories)
    }

    private fun updatePrice(communityId: Int, houseId: Int, histories: List<ConsumptionHistory>): List<ConsumptionHistory> {
        return histories.map {
            it.copy(price = getPrice(communityId, houseId, it.sellerCommunityId, it.sellerHouseId, it.units))
        }
    }

    private fun updateCredit(communityId: Int, houseId: Int, histories: List<ConsumptionHistory>): List<ConsumptionHistory> {
        return histories.map {
            it.copy(price = getPrice(communityId, houseId, it.buyerCommunityId, it.buyerHouseId, it.units))
        }
    }

    private fun getPrice(communityId: Int, houseId: Int, transactingCommunityId: Int, transactingHouseId: Int, units: Double): Double {
        return if(communityId == transactingCommunityId && houseId == transactingHouseId) {
            0.0
        } else if(communityId == transactingCommunityId) {
            SAME_COMMUNITY_PRICE * units
        } else if(transactingCommunityId != -1) {
            OTHER_COMMUNITY_PRICE * units
        } else {
            COMPANY_PRICE * units
        }
    }
}

data class ConsumptionHistory(val sellerCommunityId: Int, val sellerHouseId: Int, val buyerCommunityId: Int, val buyerHouseId: Int, val units: Double, val price: Double, val consumedAt: Timestamp)

data class Consumption(val communityId: Int, val houseId: Int, val consumed: Double)