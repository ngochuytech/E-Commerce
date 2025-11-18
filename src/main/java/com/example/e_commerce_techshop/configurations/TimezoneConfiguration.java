package com.example.e_commerce_techshop.configurations;

import org.springframework.context.annotation.Configuration;
import java.util.TimeZone;

/**
 * Configuration class to set the application timezone to GMT+7 (Asia/Ho_Chi_Minh)
 * 
 * This ensures all timestamps, date operations, and serialization use GMT+7 timezone
 * which corresponds to Vietnam Standard Time (VST).
 */
@Configuration
public class TimezoneConfiguration {
    
    static {
        // Set the default timezone for the entire JVM to Asia/Ho_Chi_Minh (GMT+7)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
}
