package dev.bodewig.hibernate_based_migration.plugin.util;

/**
 * A simple tuple type with two values
 * 
 * @param <L>
 *            the type for the left value
 * @param <R>
 *            the type for the right value
 */
public record Pair<L, R>(L left, R right) {
}
