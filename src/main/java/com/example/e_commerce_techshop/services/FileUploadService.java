package com.example.e_commerce_techshop.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, "general");
    }
    
    public List<String> uploadFiles(List<MultipartFile> files) throws Exception {
        return uploadFiles(files, "general");
    }
    
    public List<String> uploadFiles(List<MultipartFile> files, String category) throws Exception {
        List<String> uploadedUrls = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return uploadedUrls;
        }
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String uploadedUrl = uploadFile(file, category);
                if (uploadedUrl != null) {
                    uploadedUrls.add(uploadedUrl);
                }
            }
        }
        
        return uploadedUrls;
    }

    public String uploadFile(MultipartFile file, String category) throws Exception {
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
        
        // Tạo đường dẫn theo category: uploads/images/stores/, uploads/images/products/, etc.
        String categoryDir = uploadDir + "/" + category;
        Path filePath = Paths.get(categoryDir, fileName);

        // Tạo thư mục nếu chưa tồn tại
        Files.createDirectories(filePath.getParent());

        // Lưu file vào local storage
        Files.write(filePath, file.getBytes());

        // Trả về URL với category: /image/stores/filename.jpg
        return baseUrl + "/" + category + "/" + fileName;
    }

    public void deleteFile(String imageUrl) throws IOException {
        if(imageUrl == null || imageUrl.isEmpty()){
            return;
        }
        // Chuyển imageUrl thành đường dẫn file trong local storage
        // imageUrl có dạng /image/stores/filename.jpg hoặc /image/filename.jpg
        String relativePath = imageUrl.replace(baseUrl + "/", "");
        Path filePath = Paths.get(uploadDir, relativePath);

        // Xóa file nếu tồn tại
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
    
    public void deleteFiles(List<String> imageUrls) throws IOException {
        if(imageUrls == null || imageUrls.isEmpty()){
            return;
        }
        
        for (String imageUrl : imageUrls) {
            deleteFile(imageUrl);
        }
    }
}
