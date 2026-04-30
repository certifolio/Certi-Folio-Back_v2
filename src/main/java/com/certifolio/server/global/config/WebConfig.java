package com.certifolio.server.global.config;

import com.certifolio.server.domain.notification.entity.NotificationType;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToNotificationTypeConverter());
    }

    private static class StringToNotificationTypeConverter implements Converter<String, NotificationType> {
        @Override
        public NotificationType convert(String source) {
            return NotificationType.fromString(source);
        }
    }
}
