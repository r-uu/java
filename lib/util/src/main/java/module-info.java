module de.ruu.lib.util
{
    requires static jakarta.annotation;
    requires transitive org.slf4j;
    requires transitive java.compiler;
    
    requires static transitive org.jspecify;
    requires static java.instrument;
    requires static com.fasterxml.jackson.databind;

    exports de.ruu.lib.util;
    exports de.ruu.lib.util.bimapped;
    exports de.ruu.lib.util.json;
    exports de.ruu.lib.util.lang.model;
    exports de.ruu.lib.util.classpath;
    exports de.ruu.lib.util.annotation;
    exports de.ruu.lib.util.csv;
    exports de.ruu.lib.util.file;
    
    // Open packages for reflection (e.g., JSON serialization)
    opens de.ruu.lib.util to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.classpath to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.annotation to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.csv to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.file to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.json to com.fasterxml.jackson.databind;
    opens de.ruu.lib.util.lang.model to com.fasterxml.jackson.databind;
}