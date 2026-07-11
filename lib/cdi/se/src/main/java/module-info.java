module de.ruu.lib.cdi.se
{
	exports de.ruu.lib.cdi.se;
	opens   de.ruu.lib.cdi.se;

	requires transitive jakarta.cdi;
	requires jakarta.el;
	requires jakarta.inject;
	requires jakarta.interceptor;
	requires org.slf4j;
}