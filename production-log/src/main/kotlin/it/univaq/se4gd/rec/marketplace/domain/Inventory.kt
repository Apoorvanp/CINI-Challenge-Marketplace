package it.univaq.se4gd.rec.marketplace.domain

import org.springframework.data.annotation.Id
import java.sql.Timestamp

data class InventoryItem(@Id val communityId: Int, val houseId: Int, val energyProduced: Double, val productionTime: Timestamp?)
