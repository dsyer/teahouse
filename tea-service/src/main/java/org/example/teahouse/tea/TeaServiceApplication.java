package org.example.teahouse.tea;

import org.example.teahouse.tea.dashboard.EnableDashboard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@EnableFeignClients
@SpringBootApplication
@PropertySource("classpath:build.properties")
@ComponentScan(basePackages = { "org.example.teahouse" })
@EnableDashboard(title = "Tea Service Remote", format = "latency/heatmap")
public class TeaServiceApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(TeaServiceApplication.class);
        springApplication.setApplicationStartup(new BufferingApplicationStartup(10_000));
        springApplication.run(args);
    }
}
