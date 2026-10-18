package com.josemaba.marquesitasapi.config;

import com.josemaba.marquesitasapi.storage.LocalImageStorage;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves locally stored images. Keys contain a UUID, so they are immutable and safe to cache for a long time. */
@Configuration
@ConditionalOnBean(LocalImageStorage.class)
@RequiredArgsConstructor
public class LocalMediaConfig implements WebMvcConfigurer {

    private final LocalImageStorage storage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(LocalImageStorage.MEDIA_PATH + "/**")
                .addResourceLocations(storage.getRoot().toUri().toString())
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }
}
