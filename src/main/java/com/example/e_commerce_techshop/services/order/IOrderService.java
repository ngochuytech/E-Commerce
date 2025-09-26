package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.buyer.order.CheckoutDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.OrderResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.OrderSummaryDTO;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IOrderService {
    
    /**
     * Tạo đơn hàng từ cart
     * @param userEmail Email của user
     * @param checkoutDTO Thông tin checkout
     * @return OrderResponseDTO
     */
    OrderResponseDTO checkout(String userEmail, CheckoutDTO checkoutDTO) throws Exception;
    
    /**
     * Lấy lịch sử đơn hàng
     * @param userEmail Email của user
     * @param page Số trang (0-based)
     * @param size Kích thước trang
     * @param status Trạng thái đơn hàng
     * @return Page<OrderSummaryDTO>
     */
    Page<OrderSummaryDTO> getOrderHistory(String userEmail, int page, int size, String status) throws Exception;
    
    /**
     * Lấy chi tiết đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     * @return OrderResponseDTO
     */
    OrderResponseDTO getOrderDetail(String userEmail, String orderId) throws Exception;
    
    /**
     * Hủy đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     * @return OrderResponseDTO
     */
    OrderResponseDTO cancelOrder(String userEmail, String orderId) throws Exception;
    
    /**
     * Đếm số đơn hàng theo trạng thái
     * @param userEmail Email của user
     * @return Map<String, Long>
     */
    Map<String, Long> getOrderCount(String userEmail) throws Exception;
}