package org.example.teahouse.alert;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import com.fasterxml.jackson.core.JsonProcessingException;
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

	public void deleteDashboard(String uid) {
		String url = dashboards() + "uid/";
		ResponseEntity<String> result = rest.exchange(RequestEntity.delete(URI.create(url + uid)).build(),
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			log.info("Dashboard successfully deleted: " + uid);
		} else {
			log.warn("Dashboard failed to delete: " + uid + ", " + result.getBody());
		}
	}

	private String dashboards() {
		return props.getUrl() + "/api/dashboards/";
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
			log.info("Alert successfully provisioned: " + alert.getUid());
		} else {
			log.info("Alert failed to provision: " + alert.getUid() + ", " + result.getBody());
		}

	}

	public void updateDashboard(Dashboard dash) {
		ResponseEntity<String> result = rest.exchange(RequestEntity.post(URI.create(dashboards() + "db"))
				.contentType(MediaType.APPLICATION_JSON).body(new DashboardImport(dash)),
				String.class);
		if (result.getStatusCode().is2xxSuccessful()) {
			log.info("Dashboard successfully provisioned: " + dash.getUid());
		} else {
			log.info("Dashboard failed to provision: " + dash.getUid() + ", " + result.getBody());
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
		this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

}

record Folder(long id, String uid, String title) {
}

class Alert {

	@JsonAnySetter
	@JsonAnyGetter
	private Map<String, Object> details = new LinkedHashMap<>();
	private String folderUID;
	private String uid;
	private String title;
	@JsonIgnore
	private String application = "application";
	private static ObjectMapper mapper = new ObjectMapper();

	static class AlertBuilder {

		private Folder folder;
		private String application = "application";
		private String uri = "/";
		private String title = "Error Rate";
		private String message = "Service is experiencing high error rates";

		public Alert build() {
			Alert alert = new Alert();
			try {
				alert = mapper.readValue(
						TEMPLATE.replace("${application}", application).replace("${uri}", uri).replace("${message}",
								message),
						Alert.class);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			alert.title = title;
			alert.application = application;
			alert.folderUID = folder.uid() == null ? "null" : folder.uid();
			return alert;
		}

		public AlertBuilder folder(Folder folder) {
			this.folder = folder;
			return this;
		}

		public AlertBuilder application(String application) {
			this.application = application;
			return this;
		}

		public AlertBuilder uri(String uri) {
			this.uri = uri;
			return this;
		}

		public AlertBuilder title(String title) {
			this.title = title;
			return this;
		}

		public AlertBuilder message(String message) {
			this.message = message;
			return this;
		}

	}

	public static AlertBuilder forFolder(Folder folder) {
		AlertBuilder alert = new AlertBuilder();
		return alert.folder(folder);
	}

	@JsonIgnore
	public Map<String, Object> getDetails() {
		return details;
	}

	public String getFolderUID() {
		return folderUID;
	}

	public void setFolderUID(String folderUid) {
		this.folderUID = folderUid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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
							"expr": "rate(http_server_requests_seconds_count{outcome!=\\"SUCCESS\\", application=\\"${application}\\", uri=\\"${uri}\\"}[$__rate_interval])",
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
					"summary": "${message}"
				},
				"isPaused": false
			}
			""";

	@Override
	public String toString() {
		return "Alert [application=" + application + ", uri=" + title + "]";
	}

}

class DashboardImport {
	private Dashboard dashboard;

	public DashboardImport(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}
}

class Dashboard {
	@JsonAnySetter
	@JsonAnyGetter
	private Map<String, Object> details = new LinkedHashMap<>();

	private List<Panel> panels = new ArrayList<>();

	private String uid;
	private String title = "Dashboard";
	private int version;

	@JsonIgnore
	private String application = "application";

	private static ObjectMapper mapper = new ObjectMapper();

	public static Dashboard example() {
		Dashboard dash = new Dashboard();
		try {
			dash = mapper.readValue(
					TEMPLATE,
					Dashboard.class);
			dash.version = 0;
			dash.uid = null;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return dash;
	}

	public void normalize() {
		if (this.uid == null) {
			this.uid = UUID.nameUUIDFromBytes((application + title).getBytes()).toString();
		}
	}

	public void arrange(String format) {
		int height = 27;
		int width = 24;
		int count = 0;
		Map<Integer, Panel> map = new HashMap<>();
		for (Panel panel : panels) {
			map.put(count, panel);
			panel.setId(count++);
		}
		String[] rows = format.split("/");
		int nrows = rows.length;
		int[] counts = new int[nrows];
		for (int i = 0; i < nrows; i++) {
			counts[i] = rows[i].split(",").length;
		}
		int[] heights = new int[nrows];
		if (nrows == 2 && counts[1] == 1) {
			heights[0] = height / 3;
			heights[1] = height - heights[0];
		} else {
			for (int i = 0; i < nrows; i++) {
				heights[i] = height / nrows;
			}
		}
		int h = 0;
		for (int i = 0; i < nrows; i++) {
			String row = rows[i];
			int[] ids = Arrays.asList(row.split(",")) //
					.stream() //
					.mapToInt(Integer::parseInt) //
					.toArray();
			int w = 0;
			for (int id : ids) {
				Panel panel = map.get(id);
				GridPosition position = panel.getGridPos();
				position.setH(heights[i]);
				position.setW(width / counts[i]);
				position.setX(w);
				position.setY(h);
				w = w + position.getW();
			}
			h = h + heights[i];
		}
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public List<Panel> getPanels() {
		return panels;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	private static final String TEMPLATE = """
			{
				"annotations": {
					"list": [
						{
							"builtIn": 1,
							"datasource": {
								"type": "grafana",
								"uid": "-- Grafana --"
							},
							"enable": true,
							"hide": true,
							"iconColor": "rgba(0, 211, 255, 1)",
							"name": "Annotations & Alerts",
							"target": {
								"limit": 100,
								"matchAny": false,
								"tags": [],
								"type": "dashboard"
							},
							"type": "dashboard"
						}
					]
				},
				"editable": true,
				"fiscalYearStartMonth": 0,
				"graphTooltip": 0,
				"links": [],
				"liveNow": false,
				"panels": [],
				"refresh": "5s",
				"schemaVersion": 37,
				"style": "dark",
				"tags": [],
				"templating": {
					"list": [
						{
							"allValue": ".*",
							"current": {
								"selected": false,
								"text": "tea-service",
								"value": "tea-service"
							},
							"datasource": {
								"type": "prometheus",
								"uid": "prometheus"
							},
							"definition": "label_values(application)",
							"hide": 0,
							"includeAll": true,
							"label": "Application",
							"multi": false,
							"name": "application",
							"options": [],
							"query": {
								"query": "label_values(application)",
								"refId": "StandardVariableQuery"
							},
							"refresh": 1,
							"regex": "",
							"skipUrlSync": false,
							"sort": 1,
							"type": "query"
						},
						{
							"allValue": ".*",
							"current": {
								"selected": false,
								"text": "/tea/{name}",
								"value": "/tea/{name}"
							},
							"datasource": {
								"type": "prometheus",
								"uid": "prometheus"
							},
							"definition": "label_values(http_server_requests_seconds_count{application=\\"$application\\"}, uri)",
							"hide": 0,
							"includeAll": true,
							"label": "URI",
							"multi": false,
							"name": "uri",
							"options": [],
							"query": {
								"query": "label_values(http_server_requests_seconds_count{application=\\"$application\\"}, uri)",
								"refId": "StandardVariableQuery"
							},
							"refresh": 1,
							"regex": "",
							"skipUrlSync": false,
							"sort": 0,
							"type": "query"
						}
					]
				},
				"time": {
					"from": "now-5m",
					"to": "now"
				},
				"timepicker": {},
				"timezone": "",
				"title": "Tea API Copy",
				"weekStart": "sunday",
				"version": 0
			}
					""";

	@Override
	public String toString() {
		return "Dashboard [uid=" + uid + ",application=" + application + ",title='" + title +  "'']";
	}
}

class GridPosition {
	private int h = 27;
	private int w = 24;
	private int x = 0;
	private int y = 0;

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + w + "x" + h + "@" + x + "," + y + ")";
	}

}

class Panel {

	@JsonAnySetter
	@JsonAnyGetter
	private Map<String, Object> details = new LinkedHashMap<>();
	private static ObjectMapper mapper = new ObjectMapper();
	private GridPosition gridPos = new GridPosition();
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public GridPosition getGridPos() {
		return gridPos;
	}

	public void setGridPos(GridPosition gridPos) {
		this.gridPos = gridPos;
	}

	public static Panel graph() {
		try {
			return mapper.readValue(GRAPH_TEMPLATE, Panel.class);
		} catch (Exception e) {
			return new Panel();
		}
	}

	public static Panel heatmap() {
		try {
			return mapper.readValue(HEATMAP_TEMPLATE, Panel.class);
		} catch (Exception e) {
			return new Panel();
		}
	}

	private final static String GRAPH_TEMPLATE = """
					{
						"datasource": "Prometheus",
						"fieldConfig": {
							"defaults": {
								"color": {
									"mode": "palette-classic"
								},
								"custom": {
									"axisCenteredZero": false,
									"axisColorMode": "text",
									"axisLabel": "",
									"axisPlacement": "auto",
									"barAlignment": 0,
									"drawStyle": "line",
									"fillOpacity": 0,
									"gradientMode": "none",
									"hideFrom": {
										"legend": false,
										"tooltip": false,
										"viz": false
									},
									"lineInterpolation": "linear",
									"lineWidth": 1,
									"pointSize": 5,
									"scaleDistribution": {
										"type": "linear"
									},
									"showPoints": "auto",
									"spanNulls": false,
									"stacking": {
										"group": "A",
										"mode": "none"
									},
									"thresholdsStyle": {
										"mode": "off"
									}
								},
								"mappings": [],
								"thresholds": {
									"mode": "absolute",
									"steps": [
										{
											"color": "green",
											"value": null
										},
										{
											"color": "red",
											"value": 80
										}
									]
								},
								"unit": "s"
							},
							"overrides": []
						},
						"gridPos": {
							"h": 9,
							"w": 24,
							"x": 0,
							"y": 0
						},
						"id": 6,
						"options": {
							"legend": {
								"calcs": [],
								"displayMode": "list",
								"placement": "bottom",
								"showLegend": true
							},
							"tooltip": {
								"mode": "single",
								"sort": "none"
							}
						},
						"targets": [
							{
								"datasource": "Prometheus",
								"editorMode": "code",
								"exemplar": true,
								"expr": "histogram_quantile(1.00, sum(rate(http_server_requests_seconds_bucket{application=~\\"$application\\", uri=~\\"$uri\\"}[$__rate_interval])) by (le))",
								"legendFormat": "max",
								"range": true,
								"refId": "A"
							},
							{
								"datasource": "Prometheus",
								"editorMode": "code",
								"exemplar": true,
								"expr": "histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket{application=~\\"$application\\", uri=~\\"$uri\\"}[$__rate_interval])) by (le))",
								"hide": false,
								"legendFormat": "tp99",
								"range": true,
								"refId": "B"
							},
							{
								"datasource": "Prometheus",
								"editorMode": "code",
								"exemplar": true,
								"expr": "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application=~\\"$application\\", uri=~\\"$uri\\"}[$__rate_interval])) by (le))",
								"hide": false,
								"legendFormat": "tp95",
								"range": true,
								"refId": "C"
							}
						],
						"title": "$application latency for $uri",
						"type": "timeseries"
					}
			""";

	private static final String HEATMAP_TEMPLATE = """
			{
				"cards": {},
				"color": {
					"cardColor": "#b4ff00",
					"colorScale": "sqrt",
					"colorScheme": "interpolateSpectral",
					"exponent": 0.5,
					"mode": "spectrum"
				},
				"dataFormat": "tsbuckets",
				"datasource": "Prometheus",
				"fieldConfig": {
					"defaults": {
						"custom": {
							"hideFrom": {
								"legend": false,
								"tooltip": false,
								"viz": false
							},
							"scaleDistribution": {
								"type": "linear"
							}
						}
					},
					"overrides": []
				},
				"gridPos": {
					"h": 18,
					"w": 24,
					"x": 0,
					"y": 9
				},
				"heatmap": {},
				"hideZeroBuckets": true,
				"highlightCards": true,
				"id": 2,
				"legend": {
					"show": true
				},
				"maxDataPoints": 25,
				"options": {
					"calculate": false,
					"calculation": {},
					"cellGap": 2,
					"cellValues": {},
					"color": {
						"exponent": 0.5,
						"fill": "#b4ff00",
						"mode": "scheme",
						"reverse": false,
						"scale": "exponential",
						"scheme": "Spectral",
						"steps": 128
					},
					"exemplars": {
						"color": "rgba(255,0,255,0.7)"
					},
					"filterValues": {
						"le": 1e-9
					},
					"legend": {
						"show": true
					},
					"rowsFrame": {
						"layout": "auto"
					},
					"showValue": "never",
					"tooltip": {
						"show": true,
						"yHistogram": false
					},
					"yAxis": {
						"axisPlacement": "left",
						"reverse": false,
						"unit": "s"
					}
				},
				"pluginVersion": "9.2.6",
				"reverseYBuckets": true,
				"targets": [
					{
						"datasource": "Prometheus",
						"editorMode": "code",
						"exemplar": true,
						"expr": "sum(increase(http_server_requests_seconds_bucket{application=~\\"$application\\", uri=~\\"$uri\\"}[$__interval])) by (le)",
						"format": "heatmap",
						"instant": false,
						"legendFormat": "{{le}}",
						"range": true,
						"refId": "A"
					}
				],
				"title": "$application latency heatmap for $uri",
				"tooltip": {
					"show": true,
					"showHistogram": false
				},
				"type": "heatmap",
				"xAxis": {
					"show": true
				},
				"yAxis": {
					"format": "s",
					"logBase": 1,
					"show": true
				},
				"yBucketBound": "auto"
			}
			""";

}