package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromCardUserIdOrToCardUserId(Integer fromUserId, Integer toUserId);

    Page<Transaction> findByFromCardUserIdOrToCardUserId(Integer fromUserId, Integer toUserId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromCard.user.id = :userId OR t.toCard.user.id = :userId) AND " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Transaction> findByUserIdWithSearch(@Param("userId") Integer userId,
                                             @Param("search") String search,
                                             Pageable pageable);
}