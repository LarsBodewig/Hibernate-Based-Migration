package dev.bodewig.hibernate_based_migration.plugin.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.support.JavaOutputProcessor;

public class Spoon {

	private final Log log;
	private final Launcher launcher;
	private CtModel model;

	public Spoon(Log log) {
		this.log = log;
		this.launcher = new Launcher();
	}

	public void addClasses(Collection<File> classFiles) {
		classFiles.forEach(classFile -> {
			this.log.debug("Reading " + classFile.getAbsolutePath());
			this.launcher.addInputResource(classFile.getAbsolutePath());
		});
		this.model = this.launcher.buildModel();
		this.log.info("Read " + getElements(this.model, CtType.class).count() + " types");
	}

	public Pair<String, String> rewritePackages(String name) {
		CtPackage basePkg = getBasePackage(this.model);
		String oldPkgName = basePkg.getQualifiedName();
		this.log.debug("Identified base package " + oldPkgName);
		CtPackage newPkg = basePkg.getFactory().createPackage().setSimpleName(name);
		List<Pair<CtTypeReference<?>, CtType<?>>> refs = getInternalReferences(basePkg);
		movePackageContents(basePkg, newPkg);
		basePkg.addPackage(newPkg);
		String newPkgName = newPkg.getQualifiedName();
		this.log.debug("Created new base package " + newPkgName);
		basePkg.updateAllParentsBelow();
		updateInternalReferences(refs);
		this.log.debug("Rewrote package internal references");
		return new Pair<>(oldPkgName, newPkgName);
	}

	public void writeClassModel(File outDir) {
		Environment env = this.launcher.getFactory().getEnvironment();
		env.setSourceOutputDirectory(outDir);
		env.setAutoImports(true);
		JavaOutputProcessor writer = this.launcher.createOutputWriter();
		getElements(this.model, CtType.class).forEach(type -> {
			this.log.debug("Writing type " + type.getQualifiedName());
			writer.createJavaFile(type);
		});
		this.log.info("Wrote " + writer.getCreatedFiles().size() + " frozen files");
	}

	private static CtPackage getBasePackage(CtModel model) {
		Iterator<CtPackage> itr = model.getAllPackages().iterator();
		CtPackage basePackage = model.getRootPackage();
		CtPackage next = itr.next();
		while (!next.hasTypes() && itr.hasNext()) {
			basePackage = next;
			next = itr.next();
		}
		if (basePackage.isUnnamedPackage()) {
			basePackage = next;
		}
		return basePackage;
	}

	private static void movePackageContents(CtPackage from, CtPackage to) {
		from.getPackages().forEach(pkg -> {
			pkg.delete();
			to.addPackage(pkg);
		});
		from.getTypes().forEach(type -> {
			type.delete();
			to.addType(type);
		});
	}

	private static List<Pair<CtTypeReference<?>, CtType<?>>> getInternalReferences(CtPackage pkg) {
		List<Pair<CtTypeReference<?>, CtType<?>>> referencedTypes = getElements(pkg, CtTypeReference.class)
				.filter(ref -> ref.getTypeDeclaration() != null && ref.getTypeDeclaration().hasParent(pkg))
				.map(ref -> new Pair<CtTypeReference<?>, CtType<?>>(ref, ref.getTypeDeclaration())).toList();
		return referencedTypes;
	}

	@SuppressWarnings("unchecked")
	private static <T> Stream<T> getElements(CtQueryable base, Class<T> type) {
		return base.filterChildren(e -> type.isAssignableFrom(e.getClass())).list().stream().map(e -> (T) e);
	}

	private static void updateInternalReferences(List<Pair<CtTypeReference<?>, CtType<?>>> referencedTypes) {
		referencedTypes.forEach(entry -> entry.left().replace(entry.right().getReference()));
	}
}
