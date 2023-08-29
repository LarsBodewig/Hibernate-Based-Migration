package dev.bodewig.java_based_migration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class MigrationAnnotationProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		List<ElementKind> typeKinds = Arrays.asList(ElementKind.ENUM, ElementKind.INTERFACE, ElementKind.CLASS);
		// let's gather all types we are interested in
		Set<String> allElements = env.getRootElements().stream().filter(e -> typeKinds.contains(e.getKind())) // keep
																												// only
																												// interesting
																												// elements
				.map(e -> e.asType().toString()) // get their full name
				.collect(Collectors.toCollection(() -> new HashSet<>()));
		Set<String> typesWithMigration = new HashSet<>();

		annotations.forEach(te -> {
			if (Migration.class.getName().equals(te.asType().toString())) {
				// We collect elements with an already declared ownership
				env.getElementsAnnotatedWith(te).forEach(e -> typesWithMigration.add(e.asType().toString()));
			}
		});

		allElements.removeAll(typesWithMigration);
		allElements.forEach(cname -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				cname + " must be annotated with @" + Migration.class.getName()));
		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(AnnotatedJavaMigration.class.getCanonicalName());
	}
}
