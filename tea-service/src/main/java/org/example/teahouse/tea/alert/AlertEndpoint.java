package org.example.teahouse.tea.alert;

import java.util.Collection;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "alert")
@Component
public class AlertEndpoint {

	private final AlertDiscover alerts;

	public AlertEndpoint(AlertDiscover alerts) {
		this.alerts = alerts;
	}

	@ReadOperation
	public Map<String, Collection<Alert>> alert() {
		return Map.of("error", alerts.getErrorAlerts());
	}

}

class Alert {
	private String title;
	private String message;
	private Severity severity = Severity.LOW;
	private String application = "application";
	private String uri = "/";

	public Alert(String title, String message, Severity severity) {
		this.title = title;
		this.message = message;
		this.severity = severity;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	
}
