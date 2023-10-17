package org.example.teahouse.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GrafanaServiceTests {
	
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
}
