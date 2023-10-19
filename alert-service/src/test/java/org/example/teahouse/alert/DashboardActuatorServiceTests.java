package org.example.teahouse.alert;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

public class DashboardActuatorServiceTests {
	
	@Test
	public void testNoDashboard() throws Exception {
		AlertProperties props = new AlertProperties();
		props.setUrl("http://garbage/actuator/");
		DashboardActuatorService service = new DashboardActuatorService(new RestTemplateBuilder(), props);
		// No errors, no alerts
		assertThat(service.getDashboard()).isNull();
	}

	@Test
	public void testDashboard() throws Exception {
		AlertProperties props = new AlertProperties();
		props.setUrl("http://localhost:8090/actuator/");
		DashboardActuatorService service = new DashboardActuatorService(new RestTemplateBuilder(), props);
		// No errors, alerts
		assertThat(service.getDashboard()).isNotNull();
	}
}
