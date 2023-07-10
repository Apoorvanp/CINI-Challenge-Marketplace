package it.univaq.se4gd.rec.marketplace.api

import it.univaq.se4gd.rec.marketplace.domain.InventoryItem
import it.univaq.se4gd.rec.marketplace.production.ProductionLogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/productions")
class ProductionLogController(
        val productionLogService: ProductionLogService
) {
    @PostMapping
    fun sendProduce(@RequestBody request: InventoryItem): InventoryItem {
        return productionLogService.logProduction(request)
    }

    @GetMapping("/{communityId}/{houseId}")
    fun getProductions(@PathVariable("communityId") communityId: Int, @PathVariable("houseId") houseId: Int, @RequestParam("numberOfTransactions", required = false) numberOfTransactions: Int = 5): List<InventoryItem> {
        return productionLogService.getTransactions(communityId, houseId, numberOfTransactions)
    }
}