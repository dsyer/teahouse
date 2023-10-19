package org.example.teahouse.tea.dashboard;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(DashboardPanels.class)
public @interface DashboardPanel {

	String id();

	PanelType type() default PanelType.LATENCY_GRAPH;

}
