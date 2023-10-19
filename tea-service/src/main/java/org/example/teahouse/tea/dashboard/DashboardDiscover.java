package org.example.teahouse.tea.dashboard;

import java.util.Map;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

public class DashboardDiscover {

	private final RequestMappingInfoHandlerMapping mapping;
	private final ConfigurableEnvironment environment;
	private final EnableDashboard enableDashboard;

	public DashboardDiscover(RequestMappingInfoHandlerMapping mapping,
			EnableDashboard enableDashboard, ConfigurableEnvironment environment) {
		this.mapping = mapping;
		this.enableDashboard = enableDashboard;
		this.environment = environment;
	}

	public Dashboard getDashboard() {
		Dashboard dash = new Dashboard();
		String application = enableDashboard.application();
		if (!StringUtils.hasText(application)) {
			// TODO: maybe find this on the remote side
			application = environment.getProperty("spring.application.name", dash.getApplication());
		}
		dash.setApplication(application);
		dash.setTitle(enableDashboard.title());
		dash.setFormat(enableDashboard.format());
		Map<RequestMappingInfo, HandlerMethod> mappings = mapping.getHandlerMethods();
		for (RequestMappingInfo requestMethod : mappings.keySet()) {
			System.out.println(requestMethod);
			HandlerMethod handlerMethod = mappings.get(requestMethod);
			DashboardPanels panels = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(),
					DashboardPanels.class);
			if (panels != null) {
				for (DashboardPanel item : panels.value()) {
					if (item != null) {
						// TODO: use this more smartly
						Panel panel = new Panel(item.id(), item.type(),
								requestMethod.getPatternValues().iterator().next());
						dash.getPanels().add(panel);
					}
				}
			}
		}
		return dash;
	}
}
