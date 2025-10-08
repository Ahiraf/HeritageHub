package com.HeritageHub.repository;

import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.CustomerOrder;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByConsumer(Consumer consumer);
    List<CustomerOrder> findByProduct(Product product);
    List<CustomerOrder> findByStatus(OrderStatus status);
}
