package com.example.education.system.images.service;

import com.example.education.system.images.dto.GetImageListResponse;
import com.example.education.system.images.dto.UploadImageResponse;
import com.example.education.system.images.model.Image;
import com.example.education.system.images.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 图片管理服务
 * 
 * 负责图片的上传、存储和管理，包括：
 * - 图片上传（支持头像和其他类型）
 * - 图片列表查询（支持分页和类型筛选）
 * - 根据ID获取图片信息
 * - 删除图片（同时删除文件）
 * - 文件存储路径管理
 */
@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Transactional
    public UploadImageResponse uploadImage(MultipartFile file, String type, Integer userId) throws IOException {
        UploadImageResponse response = new UploadImageResponse();

        if (file.isEmpty()) {
            response.setCode(400);
            response.setMessage("文件不能为空");
            return response;
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + newFileName;

        Path fullPath = Paths.get(uploadPath, datePath);
        if (!Files.exists(fullPath)) {
            Files.createDirectories(fullPath);
        }

        Path filePath = fullPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);

        Image image = new Image();
        image.setFilePath(relativePath);
        image.setFileName(originalFilename);
        image.setFileType(file.getContentType());
        image.setType(type);
        image.setUserId(userId);

        imageRepository.insertImage(image);

        UploadImageResponse.Data data = new UploadImageResponse.Data();
        data.setImageId(image.getImageId());
        data.setFilePath(relativePath);
        data.setFileName(originalFilename);
        data.setFileType(file.getContentType());

        response.setCode(200);
        response.setMessage("上传成功");
        response.setData(data);

        return response;
    }

    public GetImageListResponse getImageList(String type, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Image> images = imageRepository.findImages(type, offset, pageSize);
        Integer total = imageRepository.countImages(type);

        GetImageListResponse response = new GetImageListResponse();
        response.setCode(200);
        response.setMessage("获取成功");

        GetImageListResponse.Data data = new GetImageListResponse.Data();
        List<GetImageListResponse.ImageInfo> imageInfos = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Image image : images) {
            GetImageListResponse.ImageInfo info = new GetImageListResponse.ImageInfo();
            info.setImageId(image.getImageId());
            info.setFilePath(image.getFilePath());
            info.setFileName(image.getFileName());
            info.setFileType(image.getFileType());
            if (image.getUploadTime() != null) {
                info.setUploadTime(image.getUploadTime().format(formatter));
            }
            imageInfos.add(info);
        }

        data.setList(imageInfos);
        data.setTotal(total);
        data.setPage(page);
        data.setPageSize(pageSize);

        response.setData(data);
        return response;
    }

    public Image getImageById(Integer imageId) {
        return imageRepository.findImageById(imageId);
    }

    @Transactional
    public void deleteImage(Integer imageId) throws IOException {
        Image image = imageRepository.findImageById(imageId);
        if (image != null) {
            Path filePath = Paths.get(uploadPath, image.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            imageRepository.deleteImage(imageId);
        }
    }
}
