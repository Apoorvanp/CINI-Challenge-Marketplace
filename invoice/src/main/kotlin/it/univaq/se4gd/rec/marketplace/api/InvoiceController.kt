package it.univaq.se4gd.rec.marketplace.api

import it.univaq.se4gd.rec.marketplace.invoice.ConsumptionHistory
import it.univaq.se4gd.rec.marketplace.invoice.InvoiceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/invoice")
class InvoiceController(
    val invoiceService: InvoiceService
) {

    @GetMapping("/{communityId}/{houseId}")
    fun getInvoice(@PathVariable("communityId") communityId: Int, @PathVariable("houseId") houseId: Int): ResponseEntity<Invoice> {
        val consumptions = invoiceService.getConsumptionHistory(communityId, houseId)
        val credits = invoiceService.getCreditHistory(communityId, houseId)
        val totalConsumptions = consumptions.sumOf { it.price }
        val totalCredits = credits.sumOf { it.price }
        val finalBill = totalConsumptions - totalCredits

        return ResponseEntity(Invoice(finalBill, consumptions, credits), HttpStatus.OK)
    }
}

data class Invoice(val amount: Double, val consumptions: List<ConsumptionHistory>, val credits: List<ConsumptionHistory>)