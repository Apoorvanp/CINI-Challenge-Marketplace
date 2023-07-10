package it.univaq.se4gd.rec.marketplace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
class InventoryApplication

fun main(args: Array<String>) {
	runApplication<InventoryApplication>(*args)
}

@Configuration
class WebConfiguration : WebMvcConfigurer {
	override fun addCorsMappings(registry: CorsRegistry) {
		registry.addMapping("/**").allowedMethods("*")
	}
}
