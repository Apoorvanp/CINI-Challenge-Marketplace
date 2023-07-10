package it.univaq.se4gd.rec.marketplace.api

import it.univaq.se4gd.rec.marketplace.domain.InventoryItem
import it.univaq.se4gd.rec.marketplace.inventory.Consumption
import it.univaq.se4gd.rec.marketplace.inventory.InventoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class InventoryController(
        val inventoryService: InventoryService,
) {
    @PostMapping("/api/v1/inventory/current")
    fun getInventory(@RequestBody request: ProductionDataRequest): List<InventoryItem> {
        return inventoryService.getCurrentInventory(request.communityId, request.houseId)
    }

    @PostMapping("/api/v1/inventory:produce")
    fun sendProduce(@RequestBody request: InventoryItem): Double {
        return inventoryService.produce(request.communityId, request.houseId, request.energyProduced, request.productionTime)
    }

    @PostMapping("/api/v1/inventory:consume")
    fun consume(@RequestBody request: ConsumptionRequest): ResponseEntity<List<Consumption>> {
        val consumptions = inventoryService.consume(request.communityId, request.houseId, request.energyNeed)
        return ResponseEntity(consumptions, HttpStatus.OK)
    }
}

data class ProductionDataRequest(val communityId: Int?, val houseId: Int?)

data class ConsumptionRequest(val communityId: Int, val houseId: Int, val energyNeed: Double)