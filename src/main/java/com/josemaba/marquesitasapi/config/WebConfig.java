package com.josemaba.marquesitasapi.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JsonMapper jsonMapper;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * Swagger UI (and some clients) send the JSON part of a multipart request without a
     * Content-Type, so it arrives as application/octet-stream. Read such parts as JSON.
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new OctetStreamJsonReader(jsonMapper));
    }

    private static class OctetStreamJsonReader extends JacksonJsonHttpMessageConverter {

        OctetStreamJsonReader(JsonMapper mapper) {
            super(mapper);
            setSupportedMediaTypes(List.of(MediaType.APPLICATION_OCTET_STREAM));
        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return false;
        }
    }
}
