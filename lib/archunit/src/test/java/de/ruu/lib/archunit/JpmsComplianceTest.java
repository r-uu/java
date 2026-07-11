package de.ruu.lib.archunit;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prüft zur Build-Zeit (Test-Phase) JPMS-Grenzen, die Maven Surefire mit
 * {@code useModulePath=false} nicht durchsetzt.
 *
 * <p>Hintergrund: Surefire legt alle JARs auf den Classpath, auch wenn
 * {@code module-info.java} vorhanden ist. JPMS-Verletzungen (nicht-exportierte
 * Packages, Split Packages) werden erst zur Laufzeit auf dem Module Path sichtbar.
 * Diese Tests machen sie bereits im Build sichtbar.
 *
 * <p>Zur Laufzeit auf dem Module Path ist JPMS selbst der Durchsetzer — diese
 * Regeln sind dann redundant und nur ein Build-Time-Sicherheitsnetz.
 */
class JpmsComplianceTest
{
    // Modul-Root = Elternverzeichnis des laufenden Maven-Moduls (archunit/)
    private static final Path PROJECT_ROOT =
            Paths.get(System.getProperty("user.dir")).getParent();

    /**
     * Repräsentiert einen {@code exports}-Eintrag aus module-info.java.
     *
     * @param pkg         exportiertes Package
     * @param qualifiedTo leer = unqualifiziert (alle dürfen zugreifen);
     *                    gefüllt = nur diese Module dürfen zugreifen
     */
    record ExportEntry(String pkg, Set<String> qualifiedTo)
    {
        boolean isAccessibleFrom(String callerModule)
        {
            return qualifiedTo.isEmpty() || qualifiedTo.contains(callerModule);
        }
    }

    /** Modulname → Menge der deklarierten Exports */
    private static Map<String, Set<ExportEntry>> moduleExports;

    /**
     * Package → Modulname (für alle Packages aller eigener Module,
     * sowohl exportierte als auch interne).
     */
    private static Map<String, String> packageToModule;

    @BeforeAll
    static void parseAllModuleInfos() throws IOException
    {
        moduleExports  = new LinkedHashMap<>();
        packageToModule = new LinkedHashMap<>();

        try (Stream<Path> walk = Files.walk(PROJECT_ROOT, 4))
        {
            walk
                .filter(p -> p.getFileName().toString().equals("module-info.java"))
                .filter(p -> p.toString().contains("/src/main/java/module-info.java"))
                .forEach(JpmsComplianceTest::parseModuleInfo);
        }
    }

    // ----------------------------------------------------------------------------------
    // Regel 1: Keine Split Packages
    // ----------------------------------------------------------------------------------

    /**
     * Kein Package darf in zwei Modulen dieses Projekts vorkommen.
     *
     * <p>JPMS wirft beim Start eine {@link java.lang.module.FindException} /
     * {@link java.lang.LayerInstantiationException}, wenn dasselbe Package in
     * mehr als einem Modul einer Layer-Konfiguration existiert.
     *
     * <p>Diese Regel erkennt das bereits im Build, indem sie alle
     * {@code src/main/java}-Verzeichnisse scannt.
     */
    @Test
    void noSplitPackages()
    {
        Map<String, List<String>> pkgToModules = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : packageToModule.entrySet())
            pkgToModules.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());

        List<String> violations = pkgToModules.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(e -> "'" + e.getKey() + "' liegt in: " + e.getValue())
            .sorted()
            .collect(toList());

        assertThat(violations)
            .describedAs(
                "Split Packages gefunden — JPMS blockiert Modules in denen " +
                "dasselbe Package mehrfach vorkommt")
            .isEmpty();
    }

    // ----------------------------------------------------------------------------------
    // Regel 2: Zugriffe über Modulgrenzen nur auf exportierte Packages
    // ----------------------------------------------------------------------------------

    /**
     * Klassen in Modul A dürfen Klassen in Modul B nur aus Packages referenzieren,
     * die B in {@code module-info.java} mit {@code exports} deklariert.
     *
     * <p>JPMS erzwingt das zur Laufzeit mit {@link IllegalAccessError}.
     * Mit {@code useModulePath=false} passiert das auf dem Classpath lautlos —
     * diese Regel macht es im Build sichtbar.
     *
     * <p>Einschränkung: Zugriffe per Reflection ({@code opens}) werden hier
     * nicht geprüft, weil ArchUnit Reflection-Aufrufe nicht im Bytecode sieht.
     */
    @Test
    void crossModuleAccessTargetsOnlyExportedPackages() throws IOException
    {
        JavaClasses allClasses = importAllModuleClasses();

        List<String> violations = new ArrayList<>();

        for (JavaClass sourceClass : allClasses)
        {
            String sourcePkg    = sourceClass.getPackageName();
            String sourceModule = packageToModule.get(sourcePkg);
            if (sourceModule == null) continue; // nicht eines unserer Module

            for (JavaAccess<?> access : sourceClass.getAccessesFromSelf())
            {
                String targetPkg    = access.getTargetOwner().getPackageName();
                String targetModule = packageToModule.get(targetPkg);

                if (targetModule == null || targetModule.equals(sourceModule)) continue;

                // Modulgrenze überschritten: ist das Package exportiert?
                Set<ExportEntry> exports = moduleExports.get(targetModule);
                if (exports == null) continue;

                boolean accessible = exports.stream()
                    .anyMatch(e -> e.pkg().equals(targetPkg) && e.isAccessibleFrom(sourceModule));

                if (!accessible)
                    violations.add(
                        sourceClass.getSimpleName() + " [" + sourceModule + "]"
                        + " → " + access.getTargetOwner().getSimpleName()
                        + " [" + targetModule + "] — Package '" + targetPkg
                        + "' ist nicht exportiert");
            }
        }

        assertThat(violations)
            .describedAs(
                "Zugriffe auf nicht-exportierte Packages — JPMS würde diese " +
                "zur Laufzeit mit IllegalAccessError blockieren")
            .isEmpty();
    }

    // ----------------------------------------------------------------------------------
    // Regel 3: Qualified Exports respektiert
    // ----------------------------------------------------------------------------------

    /**
     * {@code exports X to Y} bedeutet: nur Modul Y darf auf Package X zugreifen.
     *
     * <p>Auf dem Classpath gilt diese Einschränkung nicht — jede Klasse kann
     * zugreifen. Diese Regel stellt sicher, dass die Deklaration eingehalten wird.
     *
     * <p>Typischer Anwendungsfall: Framework-interne Packages, die nur für
     * ein bestimmtes Geschwistermodul sichtbar sein sollen.
     */
    @Test
    void qualifiedExportsRespected() throws IOException
    {
        JavaClasses allClasses = importAllModuleClasses();

        List<String> violations = new ArrayList<>();

        for (JavaClass sourceClass : allClasses)
        {
            String sourcePkg    = sourceClass.getPackageName();
            String sourceModule = packageToModule.get(sourcePkg);
            if (sourceModule == null) continue;

            for (JavaAccess<?> access : sourceClass.getAccessesFromSelf())
            {
                String targetPkg    = access.getTargetOwner().getPackageName();
                String targetModule = packageToModule.get(targetPkg);

                if (targetModule == null || targetModule.equals(sourceModule)) continue;

                Set<ExportEntry> exports = moduleExports.get(targetModule);
                if (exports == null) continue;

                // Nur qualifizierte Exports prüfen (mit "to"-Klausel)
                exports.stream()
                    .filter(e -> e.pkg().equals(targetPkg) && !e.qualifiedTo().isEmpty())
                    .filter(e -> !e.qualifiedTo().contains(sourceModule))
                    .findFirst()
                    .ifPresent(e ->
                        violations.add(
                            sourceClass.getSimpleName() + " [" + sourceModule + "]"
                            + " greift auf '" + targetPkg + "' [" + targetModule + "] zu"
                            + " — nur freigegeben für: " + e.qualifiedTo())
                    );
            }
        }

        assertThat(violations)
            .describedAs(
                "Qualified-Export-Verletzungen — 'exports X to Y' bedeutet nur Y darf zugreifen")
            .isEmpty();
    }

    // ----------------------------------------------------------------------------------
    // Regel 4: Jedes src/main/java mit Java-Quellen hat eine module-info.java
    // ----------------------------------------------------------------------------------

    /**
     * Jedes {@code src/main/java}-Verzeichnis, das {@code .java}-Quellen enthält,
     * muss eine {@code module-info.java} besitzen.
     *
     * <p>Fehlt sie, wird das Modul beim Start als "automatic module" behandelt
     * (wenn auf dem Module Path) oder landet im unnamed module — beides ist
     * unbeabsichtigt und schwer zu debuggen.
     */
    @Test
    void everySourceModuleHasModuleInfoJava() throws IOException
    {
        List<String> missing;

        try (Stream<Path> walk = Files.walk(PROJECT_ROOT, 5))
        {
            missing = walk
                .filter(p -> p.endsWith("src/main/java"))
                .filter(Files::isDirectory)
                .filter(JpmsComplianceTest::containsJavaFiles)
                .filter(dir -> !Files.exists(dir.resolve("module-info.java")))
                .map(dir -> PROJECT_ROOT.relativize(dir).toString())
                .sorted()
                .collect(toList());
        }

        assertThat(missing)
            .describedAs(
                "src/main/java-Verzeichnisse ohne module-info.java " +
                "(würden als automatic module / unnamed module laufen)")
            .isEmpty();
    }

    // ----------------------------------------------------------------------------------
    // Hilfsmethoden
    // ----------------------------------------------------------------------------------

    private static final Pattern MODULE_NAME_PATTERN =
        Pattern.compile("module\\s+([\\w.]+)\\s*\\{");

    private static final Pattern EXPORT_PATTERN =
        Pattern.compile("exports\\s+([\\w.]+)(?:\\s+to\\s+([^;]+))?\\s*;");

    private static void parseModuleInfo(Path path)
    {
        try
        {
            String content = Files.readString(path);
            // Kommentare entfernen damit Regex nicht auf auskommentierten Code anspringt
            content = content.replaceAll("//[^\n]*", "");
            content = content.replaceAll("/\\*.*?\\*/", "");

            Matcher nameMatcher = MODULE_NAME_PATTERN.matcher(content);
            if (!nameMatcher.find()) return;
            String moduleName = nameMatcher.group(1).trim();

            Set<ExportEntry> exports = new LinkedHashSet<>();
            Matcher exportMatcher = EXPORT_PATTERN.matcher(content);

            while (exportMatcher.find())
            {
                String pkg       = exportMatcher.group(1).trim();
                String toClause  = exportMatcher.group(2);
                Set<String> to   = new LinkedHashSet<>();
                if (toClause != null)
                    Arrays.stream(toClause.split(",")).map(String::trim).forEach(to::add);
                exports.add(new ExportEntry(pkg, to));
            }

            moduleExports.put(moduleName, exports);

            // Alle Packages des Moduls aus dem Dateisystem ermitteln (auch nicht-exportierte)
            Path srcRoot = path.getParent(); // = src/main/java/
            try (Stream<Path> files = Files.walk(srcRoot))
            {
                files
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                    .map(p -> srcRoot.relativize(p.getParent()))
                    .map(rel -> rel.toString().replace(java.io.File.separatorChar, '.'))
                    .filter(pkg -> !pkg.isEmpty())
                    .distinct()
                    .forEach(pkg -> packageToModule.putIfAbsent(pkg, moduleName));
            }
        }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private static JavaClasses importAllModuleClasses() throws IOException
    {
        List<Path> classDirs = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(PROJECT_ROOT, 3))
        {
            walk
                .filter(p -> p.endsWith("target/classes"))
                .filter(Files::isDirectory)
                .forEach(classDirs::add);
        }

        return new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPaths(classDirs);
    }

    private static boolean containsJavaFiles(Path dir)
    {
        try (Stream<Path> stream = Files.walk(dir))
        {
            return stream
                .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                .anyMatch(p -> p.toString().endsWith(".java"));
        }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
