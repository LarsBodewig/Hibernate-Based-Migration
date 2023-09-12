package dev.bodewig.hibernate_based_migration.plugin.util;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * A class representing a glob pattern to be used in the maven plugin
 * configuration
 */
public class Glob {

	/**
	 * Called by Plexus
	 */
	public Glob() {
	}

	private File basePath;

	private String pattern;

	/**
	 * Setter for the Plexus mechanism
	 * 
	 * @param glob
	 *            the glob String
	 */
	public void set(String glob) {
		this.basePath = new File(".");
		this.pattern = glob;
	}

	/**
	 * Setter for the Plexus mechanism
	 * 
	 * @param basePath
	 *            the base path file
	 */
	public void setBasePath(File basePath) {
		this.basePath = basePath;
	}

	/**
	 * Setter for the Plexus mechanism
	 * 
	 * @param pattern
	 *            the pattern String
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Get the list of files the glob matches
	 * 
	 * @return matched files
	 */
	public Collection<File> listMatches() {
		IOFileFilter fileFilter = new WildcardFileFilter(pattern);
		return FileUtils.listFiles(this.basePath, fileFilter, TrueFileFilter.TRUE);
	}
}
