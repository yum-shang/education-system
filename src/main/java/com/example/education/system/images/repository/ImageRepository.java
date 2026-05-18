package com.example.education.system.images.repository;

import com.example.education.system.images.model.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImageRepository {
    void insertImage(Image image);
    List<Image> findImages(@Param("type") String type, @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer countImages(@Param("type") String type);
    Image findImageById(@Param("imageId") Integer imageId);
    void deleteImage(@Param("imageId") Integer imageId);
}
