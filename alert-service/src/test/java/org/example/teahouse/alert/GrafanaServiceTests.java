package org.example.teahouse.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GrafanaServiceTests {
	
	@Test
	public void testAlert() throws Exception {
		Folder folder = new Folder(1, UUID.randomUUID().toString(), "Another Tea Error Rate");
		Alert alert = Alert.forFolder(folder);
		assertThat(alert.getDetails().get("id")).isNotNull();
		assertThat(alert.getDetails().get("folderUID")).isEqualTo(folder.uid());
		String tree = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(alert);
		assertThat(tree).contains("Another Tea Error Rate");
		assertThat(tree).doesNotContain("\"details\" :");
		assertThat(tree).doesNotContain("${folderUid}");
	}
}
