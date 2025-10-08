package com.HeritageHub.controller;

import com.HeritageHub.model.CustomerOrder;
import com.HeritageHub.model.enums.OrderStatus;
import com.HeritageHub.service.CustomerOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerOrder>> getOrders(@RequestParam(value = "status", required = false) OrderStatus status,
                                                         @RequestParam(value = "consumerNid", required = false) String consumerNid) {
        if (status != null) {
            return ResponseEntity.ok(orderService.findByStatus(status));
        }
        if (consumerNid != null) {
            return ResponseEntity.ok(orderService.findByConsumer(consumerNid));
        }
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> createOrder(@RequestBody CustomerOrder order,
                                                     @RequestParam("consumerNid") String consumerNid,
                                                     @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(orderService.create(order, consumerNid, productId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerOrder> updateOrderStatus(@PathVariable Long id,
                                                           @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
