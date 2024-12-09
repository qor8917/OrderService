package com.example.orderservice.feign;

import com.example.orderservice.dto.DecreaseStockCountDto;
import com.example.orderservice.dto.ProcessDeliveryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "deliveryClient",url = "http://delivery-service-rest:8080")
public interface DeliveryClient
{
    @PostMapping("/delivery/process-delivery")
    Map<String,Object> processDelivery (@RequestBody ProcessDeliveryDto dto);

    @GetMapping("/delivery/deliveries/{deliveryId}")
    Map<String,Object> getDelivery(@PathVariable Long deliveryId);

    @GetMapping("/delivery/address/{addressId}")
    Map<String,Object> getAddress(@PathVariable Long addressId);

    @GetMapping("/delivery/users/{userId}/first-address")
    Map<String,Object> getUserAddress(@PathVariable Long userId);
}
