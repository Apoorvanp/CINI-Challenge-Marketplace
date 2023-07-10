package it.univaq.se4gd.rec.marketplace.invoice

import khttp.get
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
class InvoiceService {
    fun getConsumptionHistory(communityId: Int, houseId: Int): List<ConsumptionHistory> {
        val url = "http://consumption-log:8080/api/v1/consumptions/$communityId/$houseId"

        val response = get(url)
        println(response.text)
        return Json.decodeFromString<List<ConsumptionHistory>>(response.text)
    }

    fun getCreditHistory(communityId: Int, houseId: Int): List<ConsumptionHistory> {
        val url = "http://consumption-log:8080/api/v1/consumptions/credits/$communityId/$houseId"

        val response = get(url)
        println(response.text)
        return Json.decodeFromString<List<ConsumptionHistory>>(response.text)
    }
}

@Serializable
data class ConsumptionHistory(val sellerCommunityId: Int, val sellerHouseId: Int, val buyerCommunityId: Int, val buyerHouseId: Int, val units: Double, val price: Double, val consumedAt: String)