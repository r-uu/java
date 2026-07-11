module de.ruu.lib.mapstruct.spi
{
    requires org.mapstruct.processor;   // Automatic-Module-Name aus mapstruct-processor MANIFEST.MF
    requires java.compiler;             // javax.lang.model.* (element, type)

    exports de.ruu.lib.mapstruct.spi;

    // JPMS-idiomatische Deklaration; META-INF/services bleibt für javac-Processor-Discovery
    provides org.mapstruct.ap.spi.AccessorNamingStrategy
        with de.ruu.lib.mapstruct.spi.FluentAccessorNamingStrategy;
}
