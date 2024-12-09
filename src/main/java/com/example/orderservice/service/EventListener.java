package com.example.orderservice.service;

import blackfriday.protobuf.EdaMessage;
import com.example.orderservice.dto.DecreaseStockCountDto;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.feign.CatalogClient;
import com.example.orderservice.repository.OrderServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final OrderServiceRepository orderServiceRepository;
    private final CatalogClient catalogClient;

    @KafkaListener(topics = "payment_result")
    public void consumePaymentResult(byte[] message) throws Exception {
        var object = EdaMessage.PaymentResultV1.parseFrom(message);

        logger.info("[payment_result] consumed : {}", object);
        //결제 정보 업데이트
        var order = orderServiceRepository.findById(object.getOrderId()).orElseThrow();
        order.paymentId = object.getPaymentId();
        order.orderStatus = OrderStatus.DELIVERY_REQUESTED;
        orderServiceRepository.save(order);

        var product = catalogClient.getProduct(order.productId);
        var deliveryMessage = EdaMessage.DeliveryRequestV1.newBuilder()
                .setOrderId(order.id)
                .setProductName(product.get("name").toString())
                .setProductCount(order.count)
                .setAddress(order.deliveryAddress).build();


        kafkaTemplate.send("delivery_request", deliveryMessage.toByteArray());

    }

    @KafkaListener(topics = "delivery_status_update")
    public void consumeDeliveryStatusUpdate(byte[] message) throws Exception {
        var object = EdaMessage.DeliveryStatusUpdateV1.parseFrom(message);
        logger.info("[delivery_status_update] consumed : {}", object);

        if(object.getDeliveryStatus().equals("REQUESTED")){
            //상품 재고 감소

            var order = orderServiceRepository.findById(object.getOrderId()).orElseThrow();

            var decreaseStockCountDto = new DecreaseStockCountDto();
                decreaseStockCountDto.decreaseCount = order.count;
                catalogClient.decreaseStockCount(order.productId, decreaseStockCountDto);
        }
    }
//
//        var payment =  paymentService.processPayment(
//                object.getOrderId(),
//                object.getUserId(),
//                object.getAmountKWR(),
//                object.getPaymentMethodId());
//
//        var paymentResultMessage = EdaMessage.PaymentResultV1.newBuilder()
//                .setOrderId(payment.orderId)
//                .setPaymentId(payment.id)
//                .setPaymentStatus(payment.paymentStatus.toString())
//                .build();
//
//        kafkaTemplate.send("payment_result",paymentResultMessage.toByteArray());



}
