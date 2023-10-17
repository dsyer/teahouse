package org.example.teahouse.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

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
@ConfigurationProperties(prefix = "alerts")
class AlertScraperService {

    private String teaServiceUrl;

    private String grafanaUrl;

    private final GrafanaService grafana;

    public AlertScraperService(RestTemplateBuilder builder, GrafanaService grafana) {
        this.grafana = grafana;
    }

    @PostConstruct
    public void scrape() {
        String uid = "b44bb7ff-24f7-41c2-afee-14503b2c928b";
        grafana.deleteAlert(uid);
        Folder folder = grafana.findFolder("Teahouse");
        if (folder==null) {
            folder = new Folder(0, "", "UNKNOWN");
        }
        Alert alert = Alert.forFolder(folder);
        grafana.addAlert(alert);
    }

    public String getTeaServiceUrl() {
        return teaServiceUrl;
    }

    public void setTeaServiceUrl(String teaServiceUrl) {
        this.teaServiceUrl = teaServiceUrl;
    }

    public String getGrafanaUrl() {
        return grafanaUrl;
    }

    public void setGrafanaUrl(String grafanaUrl) {
        this.grafanaUrl = grafanaUrl;
    }

}
