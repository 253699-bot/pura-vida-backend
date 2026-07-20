package com.puravida;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "debug=false",
        "PURAVIDA_DB_URL=jdbc:mysql://localhost:3306/puravida_test?createDatabaseIfNotExist=false&useSSL=false&serverTimezone=UTC",
        "PURAVIDA_DB_USERNAME=puravida_test",
        "PURAVIDA_DB_PASSWORD=test-password-not-used",
        "spring.datasource.hikari.initialization-fail-timeout=0"
})
class PuraVidaApplicationTests {

    @Test
    void contextLoads() {
    }
}
