package com.example.e_commerce_techshop.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    public String uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate định dạng và kích thước
        String contentType = file.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new RuntimeException("Only JPEG or PNG images are allowed");
        }
        if (file.getSize() > 30 * 1024 * 1024) { // Giới hạn 30MB
            throw new RuntimeException("Image size exceeds 30MB");
        }

        // Tạo tên file duy nhất
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        // Tạo thư mục nếu chưa tồn tại
        Files.createDirectories(filePath.getParent());

        // Lưu file vào local storage
        Files.write(filePath, file.getBytes());

        // Trả về URL hoặc đường dẫn tương đối
        return baseUrl + "/" + fileName;
    }

    public void deleteFile(String imageUrl) throws IOException {
        if(imageUrl == null || imageUrl.isEmpty()){
            return;
        }
        // Chuyển imageUrl thành đường dẫn file trong local storage
        // imageUrl có dạng /images/filename.jpg, cần lấy filename.jpg
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir, fileName);

        // Xóa file nếu tồn tại
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}
