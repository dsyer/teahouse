package org.example.teahouse.tea.dashboard;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DashboardConfiguration.class)
public @interface EnableDashboard {

	String title();

	String application() default "";

	String format() default "0";

}
