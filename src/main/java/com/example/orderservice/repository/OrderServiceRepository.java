package com.example.orderservice.repository;

import com.example.orderservice.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderServiceRepository extends JpaRepository<ProductOrder,Long>
{
    List<ProductOrder> findByUserId(Long userId);

}
