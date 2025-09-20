package org.tonylimps.liberreach.core;

import java.net.URI;
import java.nio.file.*;

public class EmptyPath implements Path {

	public EmptyPath() {}

	@Override
	public FileSystem getFileSystem() {
		throw new UnsupportedOperationException("EmptyPath has no filesystem");
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public Path getRoot() {
		return this;
	}

	@Override
	public Path getFileName() {
		return this;
	}

	@Override
	public Path getParent() {
		return this;
	}

	@Override
	public int getNameCount() {
		return 0;
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmptyPath;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public Path getName(int index) {
		throw new IndexOutOfBoundsException("EmptyPath has no name components");
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean startsWith(Path other) {
		return false;
	}

	@Override
	public boolean endsWith(Path other) {
		return false;
	}

	@Override
	public Path normalize() {
		return this;
	}

	@Override public Path resolve(Path other) {
		return other;
	}

	@Override public Path relativize(Path other) {
		return other;
	}

	@Override public URI toUri() {
		throw new UnsupportedOperationException("EmptyPath cannot be converted to a URI");
	}

	@Override public Path toAbsolutePath() {
		return this;
	}

	@Override public Path toRealPath(LinkOption... options) {
		return this;
	}

	@Override
	public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) {
		throw new UnsupportedOperationException("EmptyPath cannot be registered");
	}

	@Override public int compareTo(Path other) {
		return other instanceof EmptyPath ? 0 : -1;
	}
}
