package de.ruu.lib.util;

import static de.ruu.lib.util.Files.copyDirectoryWithContent;
import static de.ruu.lib.util.Files.copyDirectoryWithContentReplaceExisting;
import static de.ruu.lib.util.Files.createFileIfNotExists;
import static de.ruu.lib.util.Files.deleteRecursively;
import static de.ruu.lib.util.Files.isDirectoryEmpty;
import static de.ruu.lib.util.Files.readFromFile;
import static de.ruu.lib.util.Files.toDirectoryName;
import static de.ruu.lib.util.Files.toSourceFilePath;
import static de.ruu.lib.util.Files.writeToFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.ruu.lib.util.Files.DeleteMode;

class FilesTest
{
	@TempDir Path tempDir;

	private Path testFile;
	private Path testDir;
	private Path nestedDir;
	private Path nestedFile;
	
	@BeforeEach void beforeEach() throws IOException
	{
		testFile   = tempDir  .resolve("test.txt"  );
		testDir    = tempDir  .resolve("testDir"   );
		nestedDir  = testDir  .resolve("nested"    );
		nestedFile = nestedDir.resolve("nested.txt");
		
		// Create test directory structure
		java.nio.file.Files.createDirectories(nestedDir);
		java.nio.file.Files.write(testFile  , "test content"  .getBytes(StandardCharsets.UTF_8));
		java.nio.file.Files.write(nestedFile, "nested content".getBytes(StandardCharsets.UTF_8));
	}
	
	@AfterEach void tearDown() throws IOException
	{
		// Clean up test directories
		if (java.nio.file.Files.exists(tempDir))
		{
			java.nio.file.Files
					.walk(tempDir)
					.sorted(java.util.Comparator.reverseOrder())
					.forEach
					(
						path ->
						{
							try { java.nio.file.Files.deleteIfExists(path); }
							catch (IOException e) { /* Ignore */ }
						}
					);
		}
	}
	
	@Test void testWriteToFileAndReadFromFile() throws IOException
	{
		String content  = "content";
		Path   filePath = tempDir.resolve("testWrite.txt");
		
		// Test writing to file
		writeToFile(content, filePath);
		
		assertThat(java.nio.file.Files.exists    (filePath)).isEqualTo(true);
		assertThat(java.nio.file.Files.readString(filePath)).isEqualTo(content);

		// Test reading from file
		Optional<String> readContent = readFromFile(filePath);
		assertThat(readContent.isPresent()).isEqualTo(true);
		assertThat(readContent.get()).isEqualTo(content);

		// Test reading non-existent file
		Path nonExistentFile = tempDir.resolve("non-existent.txt");
		assertThat(readFromFile(nonExistentFile).isPresent()).isEqualTo(false);
	}
	
	@Test void testCreateFileIfNotExists() throws IOException
	{
		Path newFile = tempDir.resolve("newFile.txt");
		
		// Test creating new file
		Path createdFile = createFileIfNotExists(newFile);
		assertThat(createdFile).isEqualTo(newFile);
		assertThat(java.nio.file.Files.exists(createdFile)).isEqualTo(true);
		
		// Test with existing file
		Path existingFile = createFileIfNotExists(testFile);
		assertThat(existingFile).isEqualTo(testFile);
	}
	
	@Test void testDeleteRecursivelyStrict() throws IOException
	{
		// Test strict mode - should throw on failure
		assertThat(deleteRecursively(testDir, DeleteMode.STRICT)).isEqualTo(true);
		assertThat(java.nio.file.Files.exists(testDir)).isEqualTo(false);
		
		// Test with non-existent directory
		assertThat(deleteRecursively(Paths.get("non-existent"), DeleteMode.STRICT)).isEqualTo(false);
	}
	
	@Test void testDeleteRecursivelyQuiet() throws IOException
	{
		// Test quiet mode
		assertThat(deleteRecursively(testDir, DeleteMode.QUIET)).isEqualTo(true);
		assertThat(java.nio.file.Files.exists(testDir)).isEqualTo(false);
	}
	
	@Test void testDeleteRecursivelyEfficient() throws IOException
	{
		// Test efficient mode
		assertThat(deleteRecursively(testDir, DeleteMode.EFFICIENT)).isEqualTo(true);
		assertThat(java.nio.file.Files.exists(testDir)).isEqualTo(false);
	}
	
	@Test void testIsDirectoryEmpty() throws IOException
	{
		// Test with non-empty directory
		assertThat(isDirectoryEmpty(testDir)).isEqualTo(false);
		
		// Test with empty directory
		Path emptyDir = tempDir.resolve("emptyDir");
		java.nio.file.Files.createDirectories(emptyDir);
		assertThat(isDirectoryEmpty(emptyDir)).isEqualTo(true);
	}
	
	@Test void testCopyDirectoryWithContent() throws IOException
	{
		Path targetDir = tempDir.resolve("targetDir");
		
		// Test copy directory with content
		copyDirectoryWithContent(testDir, targetDir);
		
		assertThat(java.nio.file.Files.exists(targetDir)).isEqualTo(true);
		assertThat(java.nio.file.Files.exists(targetDir.resolve("nested/nested.txt"))).isEqualTo(true);
		
		// Test with replace existing
		copyDirectoryWithContentReplaceExisting(testDir, targetDir);
		assertThat(java.nio.file.Files.exists(targetDir.resolve("nested/nested.txt"))).isEqualTo(true);
	}
	
	@Test void testToSourceFilePath()
	{
		// Test package to directory conversion
		String packageName  = "com.example.test";
		String expectedPath = "com/example/test".replace('/', File.separatorChar);
		assertThat(toDirectoryName(packageName)).isEqualTo(expectedPath);

		// Test class to source file path
		Class<?> testClass = getClass();
		String expectedFilePath = 
			toDirectoryName(testClass.getPackage().getName()) + 
			File.separatorChar + 
			testClass.getSimpleName() + 
			".java";
		assertThat(toSourceFilePath(testClass)).isEqualTo(expectedFilePath);
	}
}