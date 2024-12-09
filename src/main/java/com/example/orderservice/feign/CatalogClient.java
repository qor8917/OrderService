package com.example.orderservice.feign;

import com.example.orderservice.dto.DecreaseStockCountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "catalogClient",url = "http://catalog-service-rest:8080")
public interface CatalogClient
{
    @GetMapping("/catalog/products/{productId}")
    Map<String,Object> getProduct(@PathVariable Long productId);

    @PostMapping("catalog/products/{productId}/decreaseStockCount")
    void decreaseStockCount(@PathVariable Long productId, @RequestBody DecreaseStockCountDto dto);
}
