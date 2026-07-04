package com.turkcell.subscriptionservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.subscriptionservice.entity.SimCard;

public interface SimCardRepository extends JpaRepository<SimCard, String> {

    List<SimCard> findByMsisdn(String msisdn);
}
