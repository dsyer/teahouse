package org.example.teahouse.tea.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "dashboard")
public class DashboardEndpoint {

	private final DashboardDiscover discoverer;

	public DashboardEndpoint(DashboardDiscover discoverer) {
		this.discoverer = discoverer;
	}

	@ReadOperation
	public Dashboard dashboard() {
		return discoverer.getDashboard();
	}

}

class Dashboard {
	private String title;
	private String application = "application";
	private String format = "0";
	
	private List<Panel> panels = new ArrayList<>();
	
	public Dashboard() {
	}
	
	public Dashboard(String title) {
		this.title = title;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
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

	public List<Panel> getPanels() {
		return panels;
	}

}

class Panel {
	private String id;
	private String uri;
	private PanelType type;
	
	public Panel(String id, PanelType type, String uri) {
		this.id = id;
		this.type = type;
		this.uri = uri;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public PanelType getType() {
		return type;
	}
	public void setType(PanelType type) {
		this.type = type;
	}
	
}