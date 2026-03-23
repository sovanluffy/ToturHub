package com.rental_api.ServiceBooking.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "djdfm5rrk",
                "api_key", "912446969263991",
                "api_secret", "GvxiFD2FO-jWsfHQhZ0i3GIXxXU",
                "secure", true
        ));
    }
}
