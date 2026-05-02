package org.tonylimps.liberreach.core;

import java.util.Comparator;

public class Comparators {
	public static final Comparator<CustomPath> name = Comparator
		.comparing(CustomPath::isDirectory)
		.reversed()
		.thenComparing(CustomPath::getFileName);
	public static final Comparator<CustomPath> type = Comparator
		.comparing(CustomPath::isDirectory)
		.reversed()
		.thenComparing(CustomPath::getType);
	public static final Comparator<CustomPath> size = Comparator
		.comparing(CustomPath::isDirectory)
		.reversed()
		.thenComparing(CustomPath::getSize);
	public static final Comparator<CustomPath> sizeReserve = Comparator
		.comparing(CustomPath::isDirectory)
		.thenComparing(CustomPath::getSize)
		.reversed();
	public static final Comparator<CustomPath> lastModified = Comparator
		.comparing(CustomPath::isDirectory)
		.reversed()
		.thenComparing(CustomPath::getLastModified);
	public static final Comparator<CustomPath> lastModifiedReserve = Comparator
		.comparing(CustomPath::isDirectory)
		.thenComparing(CustomPath::getLastModified)
		.reversed();
}
