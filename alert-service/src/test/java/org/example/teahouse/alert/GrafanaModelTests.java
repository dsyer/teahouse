package org.example.teahouse.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GrafanaModelTests {

	@Test
	public void testAlert() throws Exception {
		Folder folder = new Folder(1, UUID.randomUUID().toString(), "Another Tea Error Rate");
		Alert alert = Alert.forFolder(folder).title("My Alert").uri("/foo").build();
		assertThat(alert.getDetails().get("id")).isNotNull();
		assertThat(alert.getFolderUID()).isEqualTo(folder.uid());
		assertThat(alert.getTitle()).isEqualTo("My Alert");
		String tree = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(alert);
		assertThat(tree).doesNotContain("\"details\" :");
		assertThat(tree).doesNotContain("${folderUid}");
		assertThat(tree).doesNotContain("${application}");
		assertThat(tree).doesNotContain("${uri}");
	}

	@Test
	public void testDashboard() throws Exception {
		Dashboard dash = Dashboard.example();
		assertThat(dash.getDetails().get("annotations")).isNotNull();
		assertThat(dash.getVersion()).isEqualTo(0);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(dash);
		dash.setVersion(2);
		dash.setUid(UUID.nameUUIDFromBytes(dash.toString().getBytes()).toString());
		assertThat(mapper.writeValueAsString(dash))
				.isEqualTo(json.replace("\"version\":0", "\"version\":2").replace("\"uid\":null",
						"\"uid\":\"" + dash.getUid() + "\""));
	}

	@Test
	public void testDashboardPanels() throws Exception {
		Dashboard dash = Dashboard.example();
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.heatmap());
		assertThat(dash.getDetails().get("annotations")).isNotNull();
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(dash);
		assertThat(json).contains("\"panels\":[{");
	}

		@Test
	public void testLayoutOnePanel() throws Exception {
		Dashboard dash = Dashboard.example();
		dash.getPanels().add(Panel.graph());
		dash.arrange("0");
		assertThat(grid(dash.getPanels())).isEqualTo("0:(24x27@0,0)");
		assertThat(dash.getPanels().get(0).getId()).isEqualTo(0);
	}


	@Test
	public void testLayoutPanelsSlimOverFat() throws Exception {
		Dashboard dash = Dashboard.example();
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.heatmap());
		dash.arrange("0/1");
		assertThat(grid(dash.getPanels())).isEqualTo("0:(24x9@0,0),1:(24x18@0,9)");
		assertThat(dash.getPanels().get(0).getId()).isEqualTo(0);
		assertThat(dash.getPanels().get(1).getId()).isEqualTo(1);
		assertThat(dash.getPanels().get(1).getGridPos().getW()).isEqualTo(24);
		assertThat(dash.getPanels().get(1).getGridPos().getH()).isEqualTo(18);
	}

	@Test
	public void testLayoutPanelsSquare() throws Exception {
		Dashboard dash = Dashboard.example();
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.graph());
		dash.getPanels().add(Panel.heatmap());
		dash.arrange("0,2/1,3");
		assertThat(grid(dash.getPanels())).isEqualTo("0:(12x13@0,0),1:(12x13@0,13),2:(12x13@12,0),3:(12x13@12,13)");
	}

	private static String grid(List<Panel> panels) {
		StringBuilder result = new StringBuilder();
		for (Panel panel : panels) {
			result.append(panel.getId() + ":" + panel.getGridPos().toString() + ",");
		}
		String value = result.toString();
		return value.length() > 0 ? value.substring(0, value.length() - 1) : value;
	}

}
