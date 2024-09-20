package com.z0736190100.cardcashian.repo;

import com.z0736190100.cardcashian.model.CashCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends CrudRepository<CashCard, Long>,
        PagingAndSortingRepository<CashCard, Long> {
}
