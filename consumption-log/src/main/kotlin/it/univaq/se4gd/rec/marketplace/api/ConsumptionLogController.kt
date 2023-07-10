package it.univaq.se4gd.rec.marketplace.api

import it.univaq.se4gd.rec.marketplace.consumption.Consumption
import it.univaq.se4gd.rec.marketplace.consumption.ConsumptionHistory
import it.univaq.se4gd.rec.marketplace.consumption.ConsumptionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/consumptions")
class ConsumptionLogController(
        val consumptionService: ConsumptionService
) {

    @PostMapping
    fun recordConsumption(@RequestBody request: ConsumptionLogRequest): ResponseEntity<List<Consumption>> {
        val consumptions = consumptionService.consume(request.buyerCommunityId, request.buyerHouseId, request.consumptions)
        return ResponseEntity(consumptions, HttpStatus.OK)
    }

    @GetMapping("/{communityId}/{houseId}")
    fun getConsumptions(@PathVariable("communityId") buyerCommunityId: Int, @PathVariable("houseId") buyerHouseId: Int): ResponseEntity<List<ConsumptionHistory>> {
        val consumptions = consumptionService.getConsumptionHistory(buyerCommunityId, buyerHouseId)
        return ResponseEntity(consumptions, HttpStatus.OK)
    }

    @GetMapping("/credits/{communityId}/{houseId}")
    fun getCredits(@PathVariable("communityId") sellerCommunityId: Int, @PathVariable("houseId") sellerHouseId: Int): ResponseEntity<List<ConsumptionHistory>> {
        val credits = consumptionService.getCreditHistory(sellerCommunityId, sellerHouseId)
        return ResponseEntity(credits, HttpStatus.OK)
    }
}

data class ConsumptionLogRequest(val buyerCommunityId: Int, val buyerHouseId: Int, val consumptions: List<Consumption>)