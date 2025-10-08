package com.HeritageHub.service;

import com.HeritageHub.model.Bidding;
import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.enums.BidStatus;
import com.HeritageHub.repository.BiddingRepository;
import com.HeritageHub.repository.ConsumerRepository;
import com.HeritageHub.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiddingService {

    private final BiddingRepository biddingRepository;
    private final ProductRepository productRepository;
    private final ConsumerRepository consumerRepository;

    public BiddingService(BiddingRepository biddingRepository,
                          ProductRepository productRepository,
                          ConsumerRepository consumerRepository) {
        this.biddingRepository = biddingRepository;
        this.productRepository = productRepository;
        this.consumerRepository = consumerRepository;
    }

    public List<Bidding> findAll() {
        return biddingRepository.findAll();
    }

    public Bidding findById(Long id) {
        return biddingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found: " + id));
    }

    public List<Bidding> findByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return biddingRepository.findByProduct(product);
    }

    public List<Bidding> findByConsumer(String consumerNid) {
        Consumer consumer = consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
        return biddingRepository.findByConsumer(consumer);
    }

    public Bidding create(Bidding bidding, String consumerNid, Long productId) {
        Consumer consumer = consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        bidding.setConsumer(consumer);
        bidding.setProduct(product);
        if (bidding.getBidDate() == null) {
            bidding.setBidDate(LocalDateTime.now());
        }
        return biddingRepository.save(bidding);
    }

    public Bidding updateStatus(Long biddingId, BidStatus status) {
        Bidding bidding = findById(biddingId);
        bidding.setBidStatus(status);
        return biddingRepository.save(bidding);
    }

    public void delete(Long id) {
        if (!biddingRepository.existsById(id)) {
            throw new IllegalArgumentException("Bid not found: " + id);
        }
        biddingRepository.deleteById(id);
    }
}
