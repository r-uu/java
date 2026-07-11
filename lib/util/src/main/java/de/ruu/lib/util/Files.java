package de.ruu.lib.util;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static de.ruu.lib.util.BooleanFunctions.not;
import static java.io.File.separatorChar;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.isNull;
import static javax.tools.JavaFileObject.Kind.SOURCE;

public interface Files
{
	public final static char FILE_SEPARATOR_UNIX    = '/';
	public final static char FILE_SEPARATOR_WINDOWS = '\\';
	public final static char FILE_SEPARATOR_DEFAULT = FILE_SEPARATOR_UNIX;

	/** @see #toSourceFilePath(Class) */
	public static File toSourceFile(Class<?> clazz) { return new File(toSourceFilePath(clazz)); }

	/** @return source file path "a/b/c/Type.java" or "a\b\c\Type.java" respectively for type "a.b.c.Type" */
	public static String toSourceFilePath(Class<?> clazz)
	{
		return toDirectoryName(clazz) + separatorChar + clazz.getSimpleName() + SOURCE.extension;
	}

	/** @return directory name "a/b/c" or "a\b\c" respectively for type "a.b.c.Type" */
	public static String toDirectoryName(Class<?> clazz) { return toDirectoryName(clazz.getPackage().getName()); }

	/** @see #toDirectoryName(String) */
	public static String toDirectoryName(Package package_) { return toDirectoryName(package_.getName()); }

	/** @return directory name "a/b/c" or "a\b\c" respectively for package name "a.b.c" */
	public static String toDirectoryName(String packagename) { return packagename.replace('.', separatorChar); }

	/**
	 * Writes {@code string} into file specified by {@code path}. If directories for {@code path} do not already exist it
	 * will attempt to create them. If creating the directories fails, it will throw {@code IOException}. Content of an
	 * existing file in {@code path} will be overwritten.
	 *
	 * @param string to write into file
	 * @param path specifying file
	 * @throws IOException if io operation fails
	 */
	public static void writeToFile(String string, Path path) throws IOException
	{
		Path parent = path.getParent();

		// create directories for path (if they do not already exist)
		try
		{
			if (not(isNull(parent)))
			{
				java.nio.file.Files.createDirectories(parent);
			}
		}
		catch (IOException e)
		{
			throw new IOException("failure creating directories for " + parent, e);
		}

		// if exists, delete file in path
		try
		{
			java.nio.file.Files.delete(path);
		}
		catch (NoSuchFileException e)
		{
			// ok, nothing to delete
		}

		// create file in path and write string into it
		java.nio.file.Files.createFile(path);
		java.nio.file.Files.writeString(path, string, CREATE, WRITE);
	}

	public static Optional<String> readFromFile(Path path) throws IOException
	{
		if (java.nio.file.Files.exists(path, LinkOption.NOFOLLOW_LINKS))
		{
			return Optional.of(java.nio.file.Files.readString(path));
		}
		return Optional.empty();
	}

	public static Path createFileIfNotExists(Path path) throws IOException
	{
		java.nio.file.Files.createDirectories(path.getParent());

		if (!java.nio.file.Files.exists(path, LinkOption.NOFOLLOW_LINKS))
		{
			return java.nio.file.Files.createFile(path);
		}
		else
		{
			return path;
		}
	}

	public enum DeleteMode
	{
		STRICT,   // Fail on first error
		QUIET,    // Log errors, continue
		EFFICIENT // Walk file tree, low memory (quiet mode)
	}

	public static boolean deleteRecursively(Path path, DeleteMode mode) throws IOException
	{
		if (!java.nio.file.Files.exists(path)) return false; // Nothing to delete

		switch (mode)
		{
			case EFFICIENT: return deleteEfficient(path); // Quiet mode only
			case STRICT   : return deleteStrict(path);
			case QUIET    : return deleteQuiet(path);
			default       : throw new IllegalArgumentException("Unsupported mode: " + mode);
		}
	}

	// 1. Strict mode: fail immediately if something cannot be deleted
	private static boolean deleteStrict(Path path) throws IOException
	{
		try (Stream<Path> walk = java.nio.file.Files.walk(path))
		{
			walk
					.sorted(Comparator.reverseOrder())
					.forEach
					(
							p ->
							{
								try
								{
									java.nio.file.Files.delete(p);
									log().debug("deleted: {}", p);
								}
								catch (IOException e) { throw new UncheckedIOException("failed to delete: " + p, e); }
							}
					);
		}

		return true;
	}

	// 2. Quiet mode: log failures but continue
	private static boolean deleteQuiet(Path path) throws IOException
	{
		try (Stream<Path> walk = java.nio.file.Files.walk(path))
		{
			walk
					.sorted(Comparator.reverseOrder())
					.forEach
					(
							p ->
							{
								try
								{
									java.nio.file.Files.delete(p);
									log().debug("deleted: " + p);
								}
								catch (IOException e) { log().error("failed to delete: " + p + " (" + e.getMessage() + ")"); }
							}
					);
		}

		return true;
	}

	// 3. Efficient mode: low-memory, quiet deletion using FileVisitor
	private static boolean deleteEfficient(Path path)
	{
		try
		{
			java.nio.file.Files.walkFileTree
			(
					path, new SimpleFileVisitor<Path>()
					{
						@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						{
							try
							{
								java.nio.file.Files.delete(file);
								log().debug("deleted: {}", file);
							}
							catch (IOException e) { log().error("failed to delete file: " + file + " (" + e.getMessage() + ")"); }
							return FileVisitResult.CONTINUE;
						}

						@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc)
						{
							try
							{
								java.nio.file.Files.delete(dir);
								log().debug("deleted: {}", dir);
							}
							catch (IOException e) { log().error("failed to delete directory: {}", dir + " (" + e.getMessage() + ")"); }
							return FileVisitResult.CONTINUE;
						}
					}
			);
		}
		catch (IOException e)
		{
			log().error("failed to walk file tree: {}", e.getMessage());
			return false;
		}

		return true;
	}

	public static boolean isDirectoryEmpty(Path path) throws IOException
	{
		try (DirectoryStream<Path> directoryStream = java.nio.file.Files.newDirectoryStream(path))
		{
			return not(directoryStream.iterator().hasNext());
		}
	}

	public static void copyDirectoryWithContent(Path source, Path target, CopyOption... options) throws IOException
	{
		java.nio.file.Files.walkFileTree(source, new CopyDirectoryWithContentVisitor(source, target, options));
	}

	public static void copyDirectoryWithContentReplaceExisting(Path source, Path target) throws IOException
	{
		java.nio.file.Files.walkFileTree(
				source, new CopyDirectoryWithContentVisitor(source, target, StandardCopyOption.REPLACE_EXISTING));
	}

	static class CopyDirectoryWithContentVisitor extends SimpleFileVisitor<Path>
	{
		private Path source;
		private Path target;
		private CopyOption[] copyOptions;

		private CopyDirectoryWithContentVisitor(Path source, Path target, CopyOption... options)
		{
			this.source = source;
			this.target = target;
			this.copyOptions = options;
		}

		@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
		{
			Path targetFile = target.resolve(source.relativize(file));
			java.nio.file.Files.copy(file, targetFile, copyOptions);
			return FileVisitResult.CONTINUE;
		}

		@Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException
		{
			Path newDir = target.resolve(source.relativize(dir));

			try
			{
				java.nio.file.Files.copy(dir, newDir);
			}
			catch (FileAlreadyExistsException e)
			{
				return FileVisitResult.CONTINUE;
			}

			return FileVisitResult.CONTINUE;
		}
	}

	private static Logger log() { return Slf4jLogProvider.logger(Files.class); }
}