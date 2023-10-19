package org.example.teahouse.tea.dashboard;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

@Configuration
public class DashboardConfiguration implements ImportAware {

	private EnableDashboard enable;

	@Bean
	public DashboardEndpoint dashboardEndpoint(DashboardDiscover discoverer) {
		return new DashboardEndpoint(discoverer);
	}

	@Bean
	public DashboardDiscover dashdoardDiscoverer(
			@Qualifier("requestMappingHandlerMapping") RequestMappingInfoHandlerMapping mapping,
			ConfigurableEnvironment environment) {
		return new DashboardDiscover(mapping, enable, environment);
	}

	@Override
	public void setImportMetadata(AnnotationMetadata metadata) {
		this.enable = AnnotatedElementUtils
				.getMergedAnnotation(ClassUtils.resolveClassName(metadata.getClassName(), null), EnableDashboard.class);
	}
}
