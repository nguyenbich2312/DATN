package com.bzone.repository;

import com.bzone.model.Product;
import com.bzone.model.Review;
import com.bzone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findByUserAndProduct(User user, Product product);

    List<Review> findByProduct(Product product);

    int countAllByProduct(Product product);

    List<Review> findAllByProduct(Product product);
}
