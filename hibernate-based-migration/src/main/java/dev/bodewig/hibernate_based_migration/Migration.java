package dev.bodewig.hibernate_based_migration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Migration {
	String from();
	String to();
	String fromCfg() default "/hibernate.cfg.xml";
	String toCfg() default "/hibernate.cfg.xml";
}