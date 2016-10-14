package net.byyd.archive.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ArchiveModel {
	/**
	 * Specifies the line prefix of a file.
	 * 
	 * @return
	 */
	public String prefix();
}
