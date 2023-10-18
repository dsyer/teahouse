package org.example.teahouse.tea;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = TeaServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeaServiceApplicationTest {
    @Autowired
    private TestRestTemplate rest;
    @LocalServerPort
    private long port;
    @Test void contextLoads() {
        ResponseEntity<String> result = rest.getForEntity(URI.create("http://localhost:" + port + "/actuator/alert"), String.class);
        assertThat(result.getBody()).contains("Tea Errors");
    }
}
