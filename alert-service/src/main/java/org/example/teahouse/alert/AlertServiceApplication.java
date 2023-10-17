package org.example.teahouse.alert;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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

    private RestTemplate rest;

    private static String TEMPLATE = """
            {
                "id": 3,
                "uid": "b44bb7ff-24f7-41c2-afee-14503b2c928b",
                "orgID": 1,
                "folderUID": "${folderUid}",
                "ruleGroup": "10s",
                "title": "Another Tea Error Rate",
                "condition": "C",
                "data": [
                    {
                        "refId": "A",
                        "queryType": "",
                        "relativeTimeRange": {
                            "from": 60,
                            "to": 0
                        },
                        "datasourceUid": "prometheus",
                        "model": {
                            "datasource": {
                                "type": "prometheus",
                                "uid": "prometheus"
                            },
                            "editorMode": "code",
                            "exemplar": false,
                            "expr": "rate(http_server_requests_seconds_count{outcome!=\\"SUCCESS\\", application=\\"tea-service\\", uri=\\"/tea/{name}\\"}[$__rate_interval])",
                            "hide": false,
                            "instant": false,
                            "intervalMs": 1000,
                            "maxDataPoints": 43200,
                            "range": true,
                            "refId": "A"
                        }
                    },
                    {
                        "refId": "B",
                        "queryType": "",
                        "relativeTimeRange": {
                            "from": 60,
                            "to": 0
                        },
                        "datasourceUid": "__expr__",
                        "model": {
                            "conditions": [
                                {
                                    "evaluator": {
                                        "params": [],
                                        "type": "gt"
                                    },
                                    "operator": {
                                        "type": "and"
                                    },
                                    "query": {
                                        "params": [
                                            "B"
                                        ]
                                    },
                                    "reducer": {
                                        "params": [],
                                        "type": "last"
                                    },
                                    "type": "query"
                                }
                            ],
                            "datasource": {
                                "type": "__expr__",
                                "uid": "__expr__"
                            },
                            "expression": "A",
                            "hide": false,
                            "intervalMs": 1000,
                            "maxDataPoints": 43200,
                            "reducer": "last",
                            "refId": "B",
                            "settings": {
                                "mode": "dropNN"
                            },
                            "type": "reduce"
                        }
                    },
                    {
                        "refId": "C",
                        "queryType": "",
                        "relativeTimeRange": {
                            "from": 60,
                            "to": 0
                        },
                        "datasourceUid": "__expr__",
                        "model": {
                            "conditions": [
                                {
                                    "evaluator": {
                                        "params": [
                                            0.1
                                        ],
                                        "type": "gt"
                                    },
                                    "operator": {
                                        "type": "and"
                                    },
                                    "query": {
                                        "params": [
                                            "C"
                                        ]
                                    },
                                    "reducer": {
                                        "params": [],
                                        "type": "last"
                                    },
                                    "type": "query"
                                }
                            ],
                            "datasource": {
                                "type": "__expr__",
                                "uid": "__expr__"
                            },
                            "expression": "B",
                            "hide": false,
                            "intervalMs": 1000,
                            "maxDataPoints": 43200,
                            "refId": "C",
                            "type": "threshold"
                        }
                    }
                ],
                "noDataState": "OK",
                "execErrState": "Error",
                "for": "10s",
                "annotations": {
                    "__dashboardUid__": "280lKAr7k",
                    "__panelId__": "4",
                    "summary": "Tea error rate is high"
                },
                "isPaused": false
            }
            """;

    public AlertScraperService(RestTemplateBuilder builder) {
        this.rest = builder.errorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        }).build();
    }

    @PostConstruct
    public void scrape() {
        String uid = "b44bb7ff-24f7-41c2-afee-14503b2c928b";
        String url = grafanaUrl + "/api/v1/provisioning/alert-rules/";
        ResponseEntity<List<Folder>> folders = rest.exchange(
                RequestEntity.get(URI.create(grafanaUrl + "/api/folders")).build(),
                new ParameterizedTypeReference<List<Folder>>() {
                });
        String folderUid = "";
        Optional<Folder> folder = folders.getBody().stream().filter(f -> "Teahouse".equals(f.title())).findFirst();
        if (folder.isPresent()) {
            folderUid = folder.get().uid();
        }
        ResponseEntity<String> result = rest.exchange(RequestEntity.delete(URI.create(url + uid)).build(),
                String.class);
        System.out.println("Alerts successfully deleted: " + result.getStatusCode());
        result = rest.exchange(RequestEntity.post(URI.create(url))
                .contentType(MediaType.APPLICATION_JSON).body(TEMPLATE.replace("${folderUid}", folderUid)),
                String.class);
        if (result.getStatusCode().is2xxSuccessful()) {
            System.out.println("Alerts successfully provisioned");
        } else {
            System.out.println("Alerts failed to provision: " + result.getBody());
        }
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

record Folder(long id, String uid, String title) {
}