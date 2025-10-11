package com.example.e_commerce_techshop.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("title")
    @NotBlank(message = "Tiêu đề không được bỏ trống")
    private String title;

    @JsonProperty("message")
    @NotBlank(message = "Nội dung không được bỏ trống")
    private String message;

    @JsonProperty("is_read")
    private Boolean isRead;
}
