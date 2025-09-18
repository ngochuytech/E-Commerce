package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}

