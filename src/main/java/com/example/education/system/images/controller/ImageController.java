package com.example.education.system.images.controller;

import com.example.education.system.auth.service.JwtService;
import com.example.education.system.images.dto.GetImageListResponse;
import com.example.education.system.images.dto.UploadImageResponse;
import com.example.education.system.images.model.Image;
import com.example.education.system.images.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpServletRequest request;

    @Value("${file.upload.path:./uploads/images}")
    private String uploadPath;

    private Integer getUserIdFromToken() {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtService.getUserIdFromToken(token);
        }
        return null;
    }

    @PostMapping
    public UploadImageResponse uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        try {
            Integer userId = getUserIdFromToken();
            return imageService.uploadImage(file, type, userId);
        } catch (IOException e) {
            UploadImageResponse response = new UploadImageResponse();
            response.setCode(500);
            response.setMessage("文件上传失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping
    public GetImageListResponse getImageList(
            @RequestParam(required = false) String type,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        return imageService.getImageList(type, page, pageSize);
    }

    @GetMapping("/{imageId}/view")
    public ResponseEntity<Resource> viewImage(@PathVariable Integer imageId) {
        try {
            Image image = imageService.getImageById(imageId);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(uploadPath, image.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{imageId}")
    public UploadImageResponse deleteImage(@PathVariable Integer imageId) {
        try {
            imageService.deleteImage(imageId);
            UploadImageResponse response = new UploadImageResponse();
            response.setCode(200);
            response.setMessage("删除成功");
            return response;
        } catch (IOException e) {
            UploadImageResponse response = new UploadImageResponse();
            response.setCode(500);
            response.setMessage("删除失败: " + e.getMessage());
            return response;
        }
    }
}
