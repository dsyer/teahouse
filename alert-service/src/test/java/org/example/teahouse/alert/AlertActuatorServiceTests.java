package org.example.teahouse.alert;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

public class AlertActuatorServiceTests {
	
	@Test
	public void testNoAlerts() throws Exception {
		AlertProperties props = new AlertProperties();
		props.setUrl("http://garbage/actuator/alert");
		AlertActuatorService service = new AlertActuatorService(new RestTemplateBuilder(), props);
		// No errors, no alerts
		assertThat(service.getAlerts(new Folder(1, "uid", "title"))).hasSize(0);
	}

	@Test
	public void testAlerts() throws Exception {
		AlertProperties props = new AlertProperties();
		props.setUrl("http://localhost:8090/actuator/alert");
		AlertActuatorService service = new AlertActuatorService(new RestTemplateBuilder(), props);
		// No errors, alerts
		assertThat(service.getAlerts(new Folder(1, "uid", "title"))).hasSize(1);
	}
}
