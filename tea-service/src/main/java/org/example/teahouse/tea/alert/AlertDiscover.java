package org.example.teahouse.tea.alert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

@Component
public class AlertDiscover {

	private final RequestMappingInfoHandlerMapping mapping;
	private final ConfigurableEnvironment environment;

	public AlertDiscover(@Qualifier("requestMappingHandlerMapping") RequestMappingInfoHandlerMapping mapping, ConfigurableEnvironment environment) {
		this.mapping = mapping;
		this.environment = environment;
	}

	public Collection<Alert> getErrorAlerts() {
		Set<Alert> alerts = new HashSet<>();
		Map<RequestMappingInfo, HandlerMethod> mappings = mapping.getHandlerMethods();
		for (RequestMappingInfo requestMethod : mappings.keySet()) {
			System.out.println(requestMethod);
			HandlerMethod handlerMethod = mappings.get(requestMethod);
			ErrorAlert error = AnnotatedElementUtils.getMergedAnnotation(handlerMethod.getMethod(), ErrorAlert.class);
			if (error!=null) {
				Alert alert = new Alert(error.title(), error.message(), error.severity());
				// TODO: maybe find this on the remote side
				alert.setApplication( environment.getProperty("spring.application.name", alert.getApplication()));
				// TODO: use this more smartly
				alert.setUri(requestMethod.getPatternValues().iterator().next());
				alerts.add(alert);
			}
		}
		return alerts;
	}
}
