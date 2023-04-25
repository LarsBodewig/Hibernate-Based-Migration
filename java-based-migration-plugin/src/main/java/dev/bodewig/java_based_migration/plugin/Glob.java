package dev.bodewig.java_based_migration.plugin;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Glob {

	private File basePath;

	private String pattern;

	public void set(String glob) {
		this.basePath = new File(".");
		this.pattern = glob;
	}

	public void setBasePath(File basePath) {
		this.basePath = basePath;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Collection<File> listMatches() {
		IOFileFilter fileFilter = new WildcardFileFilter(pattern);
		return FileUtils.listFiles(this.basePath, fileFilter, TrueFileFilter.TRUE);
	}
}
