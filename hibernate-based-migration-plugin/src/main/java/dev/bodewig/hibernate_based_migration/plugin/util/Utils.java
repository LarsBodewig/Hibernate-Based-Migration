package dev.bodewig.hibernate_based_migration.plugin.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
	private Utils() {
	}

	public static <T, R> List<R> flatMapList(Collection<T> values,
			Function<? super T, ? extends Collection<? extends R>> mapper) {
		return values.stream().flatMap(v -> mapper.apply(v).stream()).collect(Collectors.toList());
	}

	public static String toJavaIdentifier(String identifier) {
		char[] idChars = identifier.toCharArray();
		for (int i = 0; i < idChars.length; i++) {
			if (!Character.isJavaIdentifierPart(idChars[i])) {
				idChars[i] = '_';
			}
		}
		String result = String.valueOf(idChars);
		if (!Character.isJavaIdentifierStart(idChars[0])) {
			result = "_" + result;
		}
		return result;
	}
}
