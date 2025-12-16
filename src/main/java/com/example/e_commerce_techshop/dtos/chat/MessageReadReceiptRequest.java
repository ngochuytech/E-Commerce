package com.example.e_commerce_techshop.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageReadReceiptRequest {
    
    private String conversationId;
    private String messageId;
    private String userId;
}
