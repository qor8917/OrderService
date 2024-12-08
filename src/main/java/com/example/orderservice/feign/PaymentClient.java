package com.example.orderservice.feign;

import com.example.orderservice.dto.ProcessDeliveryDto;
import com.example.orderservice.dto.ProcessPaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "paymentClient",url = "http://payment-service:8080")
public interface PaymentClient
{
    @PostMapping("/payment/process-payment")
    Map<String,Object> processPayment(@RequestBody ProcessPaymentDto dto);

    @GetMapping("/payment/users/{userId}/first-method")
    Map<String,Object> getPaymentMethod(@PathVariable Long userId);

    @GetMapping("/payment/users/{paymentId}")
    Map<String,Object> getPayment(@PathVariable Long paymentId);
}
