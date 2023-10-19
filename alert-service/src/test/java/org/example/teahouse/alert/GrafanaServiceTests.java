package org.example.teahouse.alert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

public class GrafanaServiceTests {
	
	@Test
	public void testUpsert() throws Exception {
		GrafanaProperties props = new GrafanaProperties();
		props.setUrl("http://localhost:3000");
		GrafanaService service = new GrafanaService(new RestTemplateBuilder(), props);
		Dashboard dash = Dashboard.example();
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.heatmap());
		dash.arrange("0,1/2");
		dash.normalize();
		service.deleteDashboard(dash.getUid());
		service.updateDashboard(dash);
	}
}
