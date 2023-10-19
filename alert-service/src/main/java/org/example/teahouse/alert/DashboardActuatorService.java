package org.example.teahouse.alert;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Component
public class DashboardActuatorService {

	private final RestTemplate rest;
	private final AlertProperties props;

	public DashboardActuatorService(RestTemplateBuilder builder, AlertProperties props) {
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

	public Dashboard getDashboard() {
		try {
			DashboardDto dash = rest.exchange(RequestEntity.get(URI.create(props.getUrl() + "dashboard")).build(),
			DashboardDto.class).getBody();
			Dashboard result = Dashboard.example();
			Map<String,Integer> ids = new HashMap<>();
			int count = 0;
			for (PanelDto dto : dash.panels) {
				result.getPanels().add(panel(count, dto));
				ids.put(dto.id, count++);
			}
			String format = dash.format;
			for (String id : ids.keySet()) {
				format = format.replace(id, "" + ids.get(id));
			}
			result.arrange(format);
			result.setApplication(dash.application);
			result.setTitle(dash.title);
			result.normalize();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Panel panel(int count, PanelDto dto) {
		Panel result = switch (dto.type) {
			case LATENCY_GRAPH -> Panel.graph();
			case LATENCY_HEATMAP -> Panel.heatmap();
		};
		result.setId(count);
		return result;
	}

	public static class DashboardDto {
	 	public String title;
		public String application = "application";
		public List<PanelDto> panels = new ArrayList<>();
		public String format;
	}
	
	public static class PanelDto {
	 	public String id;
		public String uri = "/";
		public Type type;
		public static enum Type {
			LATENCY_GRAPH, LATENCY_HEATMAP;
		}
	}
}