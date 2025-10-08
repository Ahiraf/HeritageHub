package com.HeritageHub.repository;

import com.HeritageHub.model.Bidding;
import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BiddingRepository extends JpaRepository<Bidding, Long> {
    List<Bidding> findByProduct(Product product);
    List<Bidding> findByConsumer(Consumer consumer);
    List<Bidding> findByBidStatus(BidStatus bidStatus);
}
