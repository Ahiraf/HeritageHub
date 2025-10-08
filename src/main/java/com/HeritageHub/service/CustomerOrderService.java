package com.HeritageHub.service;

import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.CustomerOrder;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.enums.OrderStatus;
import com.HeritageHub.repository.ConsumerRepository;
import com.HeritageHub.repository.CustomerOrderRepository;
import com.HeritageHub.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ConsumerRepository consumerRepository;

    public CustomerOrderService(CustomerOrderRepository orderRepository,
                                ProductRepository productRepository,
                                ConsumerRepository consumerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.consumerRepository = consumerRepository;
    }

    public List<CustomerOrder> findAll() {
        return orderRepository.findAll();
    }

    public CustomerOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public List<CustomerOrder> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<CustomerOrder> findByConsumer(String consumerNid) {
        Consumer consumer = consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
        return orderRepository.findByConsumer(consumer);
    }

    public CustomerOrder create(CustomerOrder order, String consumerNid, Long productId) {
        Consumer consumer = consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        order.setConsumer(consumer);
        order.setProduct(product);
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    public CustomerOrder updateStatus(Long orderId, OrderStatus status) {
        CustomerOrder order = findById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void delete(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}
