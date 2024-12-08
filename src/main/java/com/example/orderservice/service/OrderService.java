package com.example.orderservice.service;

import com.example.orderservice.dto.*;
import com.example.orderservice.entity.ProductOrder;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.feign.CatalogClient;
import com.example.orderservice.feign.DeliveryClient;
import com.example.orderservice.feign.PaymentClient;
import com.example.orderservice.repository.OrderServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderServiceRepository orderServiceRepository;
    private final CatalogClient catalogClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    public StartOrderResponseDto startOrder(Long userId, Long productId, Long count) {

        // 1.상품정보조회
        var product = catalogClient.getProduct(productId);
        // 2.결제수단정보조회
        var paymentMethod = paymentClient.getPaymentMethod(userId);
        // 3.배송지정보 조회
        var address = deliveryClient.getUserAddress(userId);
        // 4.주문정보 생성
        var order = new ProductOrder(
                userId,
                productId,
                count,
                OrderStatus.INITIATE,
                null,
                null
        );
        orderServiceRepository.save(order);

        var startOrderDto = new StartOrderResponseDto();
        startOrderDto.orderId = order.id;
        startOrderDto.paymentMethod = paymentMethod;
        startOrderDto.address = address;

        return startOrderDto;
    }

    public ProductOrder finishOrder(Long orderId,Long paymentMethodId ,Long addressId) {
        var order = orderServiceRepository.findById(orderId).orElseThrow();
        // 1. 상품 정보 조회
        var product = catalogClient.getProduct(order.productId);

        // 2. 결제
        var processPaymentDto = new ProcessPaymentDto();
        processPaymentDto.paymentMethodId = paymentMethodId;
        processPaymentDto.orderId = order.id;
        processPaymentDto.userId = order.userId;
        processPaymentDto.amountKRW = Long.parseLong(product.get("price").toString()) * order.count;

        var payment = paymentClient.processPayment(processPaymentDto);

        // 3. 배송 요청
        var address = deliveryClient.getUserAddress(addressId);

        var processDeliveryDto = new ProcessDeliveryDto();
        processDeliveryDto.orderId = order.id;
        processDeliveryDto.productName = product.get("name").toString();
        processDeliveryDto.productCount = order.count;
        processDeliveryDto.address = address.get("address").toString();

        var delivery = deliveryClient.processDelivery(processDeliveryDto);

        // 4. 상품 재고 감소
        var decreaseStockCountDto = new DecreaseStockCountDto();
        decreaseStockCountDto.decreaseCount = order.count;

        catalogClient.decreaseStockCount(order.productId, decreaseStockCountDto);

        // 5. 주문 정보 업데이트
        order.paymentId = Long.parseLong(payment.get("id").toString());
        order.deliveryId = Long.parseLong(delivery.get("id").toString());
        order.orderStatus = OrderStatus.DELIVERY_REQUESTED;

        return orderServiceRepository.save(order);

    };

    public List<ProductOrder> getUserOrders(Long userId) {
        return orderServiceRepository.findByUserId(userId);
    }

    public ProductOrderDetailDto getOrderDetail(Long orderId) {
        var order = orderServiceRepository.findById(orderId).orElseThrow();

        var paymentRes = paymentClient.getPayment(order.paymentId);
        var deliveryRes = deliveryClient.getDelivery(order.deliveryId);

        var dto = new ProductOrderDetailDto(
                order.id,
                order.userId,
                order.productId,
                order.paymentId,
                order.deliveryId,
                order.orderStatus,
                paymentRes.get("paymentStatus").toString(),
                deliveryRes.get("status").toString()
        );

        return dto;

    }


}
