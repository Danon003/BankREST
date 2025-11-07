package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Integer> {

    Page<BankCard> findByUserId(Integer userId, Pageable pageable);

    Optional<BankCard> findByIdAndUserId(Integer id, Integer userId);

    List<BankCard> findByUserIdAndStatus(Integer userId, BankCard.CardStatus status);

    @Query("SELECT COUNT(b) > 0 FROM BankCard b WHERE b.id = :cardId AND b.user.id = :userId")
    boolean existsByIdAndUserId(@Param("cardId") Integer cardId, @Param("userId") Integer userId);

    @Query("SELECT b FROM BankCard b WHERE b.user.id = :userId AND " +
            "(LOWER(b.cardHolder) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "b.id = :searchId)")
    Page<BankCard> findByUserIdWithSearch(@Param("userId") Integer userId,
                                          @Param("search") String search,
                                          @Param("searchId") Integer searchId,

                                          Pageable pageable);

    Optional<BankCard> findById(Integer cardId);
}