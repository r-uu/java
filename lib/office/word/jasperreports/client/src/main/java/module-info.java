module de.ruu.lib.jasperreports.client
{
    requires com.google.gson;
    requires java.net.http;

    exports de.ruu.lib.jasperreports.client;

    // Open package for Gson reflection (JavaBeans serialization)
    opens de.ruu.lib.jasperreports.client to com.google.gson;
}
