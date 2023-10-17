package org.example.teahouse.alert;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GrafanaService {

	private static Log log = LogFactory.getLog(GrafanaService.class);

	private final RestTemplate rest;
	private final GrafanaProperties props;

	public GrafanaService(RestTemplateBuilder builder, GrafanaProperties props) {
		this.props = props;
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

	public void deleteAlert(String uid) {
		String url = alertRules();
		ResponseEntity<String> result = rest.exchange(RequestEntity.delete(URI.create(url + uid)).build(),
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			log.info("Alert successfully deleted: " + uid);
		} else {
			log.warn("Alert failed to delete: " + uid + ", " + result.getBody());
		}
	}

	private String alertRules() {
		return props.getUrl() + "/api/v1/provisioning/alert-rules/";
	}

	public Folder findFolder(String name) {
		String url = props.getUrl() + "/api/folders";
		ResponseEntity<List<Folder>> folders = rest.exchange(
				RequestEntity.get(URI.create(url)).build(),
				new ParameterizedTypeReference<List<Folder>>() {
				});
		Optional<Folder> folder = folders.getBody().stream().filter(f -> name.equals(f.title())).findFirst();
		if (folder.isPresent()) {
			return folder.get();
		}
		return null;
	}

	public void addAlert(Alert alert) {
		ResponseEntity<String> result = rest.exchange(RequestEntity.post(URI.create(alertRules()))
				.contentType(MediaType.APPLICATION_JSON).body(alert),
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			log.info("Alert successfully provisioned: " + alert);
		} else {
			log.info("Alert failed to provision: " + result.getBody());
		}

	}

}

@Component
@ConfigurationProperties(prefix = "grafana")
class GrafanaProperties {

	private String url;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url.endsWith("/") ? url.substring(0, url.length()-1) : url;
	}

}

record Folder(long id, String uid, String title) {
}

class Alert {
	
	@JsonAnySetter
	@JsonAnyGetter
	private Map<String, Object> details = new LinkedHashMap<>();
	private static ObjectMapper mapper = new ObjectMapper();

	public static Alert forFolder(Folder folder) {
		Alert alert = new Alert();
		try {
			alert = mapper.readValue(TEMPLATE, Alert.class);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		alert.getDetails().put("folderUID", folder.uid() == null ? "null" : folder.uid());
		return alert;
	}

	@JsonIgnore
	public Map<String, Object> getDetails() {
		return details;
	}

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
}