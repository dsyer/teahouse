package org.example.teahouse.alert;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class AlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(AlertServiceApplication.class);
        springApplication.setApplicationStartup(new BufferingApplicationStartup(10_000));
        springApplication.run(args);
    }
}

@Component
class AlertScraperService {

    private final GrafanaService grafana;

    private final AlertActuatorService actuators;

    public AlertScraperService(RestTemplateBuilder builder, GrafanaService grafana, AlertActuatorService actuators) {
        this.grafana = grafana;
        this.actuators = actuators;
    }

    @PostConstruct
    public void scrape() {
        Folder folder = grafana.findFolder("Teahouse");
        if (folder == null) {
            folder = new Folder(0, "", "UNKNOWN");
        }
        for (Alert alert : actuators.getAlerts(folder)) {
            String uid = UUID.nameUUIDFromBytes(alert.toString().getBytes()).toString();
            grafana.deleteAlert(uid);
            alert.setUid(uid);
            grafana.addAlert(alert);
        }
    }

}

@Component
class DashboardScraperService {

    private final GrafanaService grafana;

    private final DashboardActuatorService actuators;

    public DashboardScraperService(RestTemplateBuilder builder, GrafanaService grafana,
            DashboardActuatorService actuators) {
        this.grafana = grafana;
        this.actuators = actuators;
    }

    @PostConstruct
    public void scrape() {
        Dashboard dashboard = actuators.getDashboard();
        if (dashboard != null) {
            grafana.updateDashboard(dashboard);
        }
    }

}