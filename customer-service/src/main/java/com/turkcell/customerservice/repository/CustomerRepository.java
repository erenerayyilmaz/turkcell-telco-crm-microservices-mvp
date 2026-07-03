package com.turkcell.customerservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.turkcell.customerservice.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /** Ad/soyad (buyuk-kucuk duyarsiz) veya TCKN icinde arama (CSR musteri arama ekrani). */
    @Query("""
            select c from Customer c
            where lower(c.firstName) like lower(concat('%', :q, '%'))
               or lower(c.lastName)  like lower(concat('%', :q, '%'))
               or c.identityNumber   like concat('%', :q, '%')
            """)
    Page<Customer> search(@Param("q") String q, Pageable pageable);
}
