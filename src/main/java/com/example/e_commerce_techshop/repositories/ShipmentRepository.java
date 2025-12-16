package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Shipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {

    Optional<Shipment> findByOrderId(String orderId);

    List<Shipment> findByStoreId(String storeId);

    List<Shipment> findByStatus(String status);
    Page<Shipment> findByStatus(String status, Pageable pageable);

    /**
     * Tìm shipment theo trạng thái và được tạo trước ngày chỉ định
     */
    @Query("{ 'status': ?0, 'createdAt': { $lt: ?1 } }")
    List<Shipment> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

    List<Shipment> findByStoreIdAndStatus(String storeId, String status);

    @Query(value = "{ 'store.$id': ObjectId(?0) }", count = true)
    long countByStoreId(String storeId);

    @Query(value = "{ 'store.$id': ObjectId(?0), 'status': ?1 }", count = true)
    long countByStoreIdAndStatus(String storeId, String status);

    Page<Shipment> findByCarrier(String carrierId, Pageable pageable);

    /**
     * Đếm tất cả shipment theo trạng thái (không phân biệt store)
     */
    long countByStatus(String status);

    /**
     * Lấy danh sách shipment đã giao hoặc thất bại (DELIVERED + FAILED)
     */
    @Query("{ 'status': { $in: ['DELIVERED', 'FAILED'] } }")
    Page<Shipment> findDeliveredOrFailed(Pageable pageable);
}
