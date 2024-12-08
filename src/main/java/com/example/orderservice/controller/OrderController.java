package com.example.orderservice.controller;

import com.example.orderservice.dto.FinishOrderDto;
import com.example.orderservice.dto.ProductOrderDetailDto;
import com.example.orderservice.dto.StartOrderDto;
import com.example.orderservice.dto.StartOrderResponseDto;
import com.example.orderservice.entity.ProductOrder;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController   {

    private final OrderService orderService;

    @PostMapping("/order/start-order")
    public StartOrderResponseDto startOrder(@RequestBody StartOrderDto dto){
        return orderService.startOrder(dto.userId,dto.productId,dto.count);
    }

    @PostMapping("/order/finish-order")
    public ProductOrder finishOrder(@RequestBody FinishOrderDto dto){
        return orderService.finishOrder(dto.orderId,dto.paymentMethodId, dto.addressId);
    }

    @GetMapping("/order/users/{userId}/orders")
    public List<ProductOrder> getUserOrders(@PathVariable Long userId){
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/order/users/{orderId}")
    public ProductOrderDetailDto getOrder(@PathVariable Long orderId){
        return orderService.getOrderDetail(orderId);
    }
}
