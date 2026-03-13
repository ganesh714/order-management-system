package com.software.inventory_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {
	
	@Value("${fail.on.order.id}")
	private String failOnOrderId;
	
	@PostMapping("/inventory")
	public ResponseEntity<String> reserveInventory(@RequestParam String orderId){
		System.out.println("Received inventory reservation request for Order: " + orderId);
		
		if (orderId.equals(failOnOrderId)) {
			System.err.println("Simulating Inventory Failure for Order: " + orderId);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Inventory reservation failed: Out of stock.");
		}
		System.out.println("Inventory reserved successfully for Order: " + orderId);
		return ResponseEntity.ok("Inventory reserved.");
		
	}
}
