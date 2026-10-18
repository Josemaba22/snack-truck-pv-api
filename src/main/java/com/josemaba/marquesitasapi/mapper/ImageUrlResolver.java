package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageUrlResolver {

    private final ImageStorage imageStorage;

    @Named("imageUrl")
    public String imageUrl(String imageKey) {
        return imageKey == null ? null : imageStorage.urlFor(imageKey);
    }
}
