module de.ruu.lib.cdi.se.demo
{
	exports de.ruu.lib.cdi.se.demo;
	exports de.ruu.lib.cdi.se.demo.parameters;
	
	opens de.ruu.lib.cdi.se.demo;
	opens de.ruu.lib.cdi.se.demo.parameters;

	requires jakarta.annotation;
	requires jakarta.cdi;
	requires jakarta.el;
	requires jakarta.inject;
	requires org.slf4j;
	requires de.ruu.lib.cdi.se;

}