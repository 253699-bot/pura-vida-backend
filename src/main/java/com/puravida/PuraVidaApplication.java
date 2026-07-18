package com.puravida;

import com.puravida.shared.config.CorsConfig;
import com.puravida.shared.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Import({CorsConfig.class, SecurityConfig.class})
public class PuraVidaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuraVidaApplication.class, args);
    }
}
