package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
    // Chỉ cần các method cơ bản vì User-Address là OneToOne relationship
    // Address được quản lý thông qua User entity
}

