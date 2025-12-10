package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Shipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {

    /**
     * Tìm shipment theo order ID
     */
    Optional<Shipment> findByOrderId(String orderId);

    /**
     * Tìm shipment theo store ID
     */
    List<Shipment> findByStoreId(String storeId);

    /**
     * Tìm shipment theo trạng thái
     */
    List<Shipment> findByStatus(String status);

    /**
     * Tìm shipment theo trạng thái và được tạo trước ngày chỉ định
     */
    @Query("{ 'status': ?0, 'createdAt': { $lt: ?1 } }")
    List<Shipment> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

    /**
     * Tìm shipment theo store và trạng thái
     */
    List<Shipment> findByStoreIdAndStatus(String storeId, String status);

    @Query(value = "{ 'store.$id': ObjectId(?0) }", count = true)
    long countByStoreId(String storeId);

    @Query(value = "{ 'store.$id': ObjectId(?0), 'status': ?1 }", count = true)
    long countByStoreIdAndStatus(String storeId, String status);
}
