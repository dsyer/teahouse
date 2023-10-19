package org.example.teahouse.alert;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Component
public class AlertActuatorService {

	private final RestTemplate rest;
	private final AlertProperties props;

	public AlertActuatorService(RestTemplateBuilder builder, AlertProperties props) {
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

	public List<Alert> getAlerts(Folder folder) {
		try {
			Map<String,List<AlertDto>> map = rest.exchange(RequestEntity.get(URI.create(props.getUrl() + "alert")).build(),
			new ParameterizedTypeReference<Map<String,List<AlertDto>>>() {
			}).getBody();
			List<AlertDto> list = map.get("error");
			List<Alert> result = new ArrayList<>();
			for (AlertDto dto : list) {
				result.add(Alert.forFolder(folder).title(dto.title).message(dto.message).application(dto.application)
				.uri(dto.uri).build());
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public static class AlertDto {
	 	public String title;
		public String message;
		public Severity severity = Severity.LOW;
		public String application = "application";
		public String uri = "/";

		public static enum Severity {
			LOW, MEDIUM, HIGH, CRITICAL
		}
	}
}

@Component
@ConfigurationProperties(prefix = "alerts")
class AlertProperties {

	private String url;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}