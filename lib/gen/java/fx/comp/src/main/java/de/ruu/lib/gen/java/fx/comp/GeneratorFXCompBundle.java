package de.ruu.lib.gen.java.fx.comp;

import de.ruu.lib.gen.GeneratorException;

import java.io.IOException;

public class GeneratorFXCompBundle
{
	private GeneratorFXCView           generatorView;
	private GeneratorFXCViewService    generatorViewService;
	private GeneratorFXCViewController generatorViewController;
	private GeneratorFXCViewFXML       generatorViewFXML;
	private GeneratorFXCViewCSS        generatorViewCSS;
	private GeneratorFXCApp            generatorApp;
	private GeneratorFXCAppRunner      generatorAppRunner;

	public GeneratorFXCompBundle(String packageName, String simpleFileName)
	{
		generatorView           = new GeneratorFXCView          (packageName, simpleFileName               );
		generatorViewService    = new GeneratorFXCViewService   (packageName, simpleFileName + "Service"   );
		generatorViewController = new GeneratorFXCViewController(packageName, simpleFileName + "Controller");
		generatorViewFXML       = new GeneratorFXCViewFXML      (packageName, simpleFileName               );
		generatorViewCSS        = new GeneratorFXCViewCSS       (packageName, simpleFileName               );
		generatorApp            = new GeneratorFXCApp           (packageName, simpleFileName + "App"       );
		generatorAppRunner      = new GeneratorFXCAppRunner
		                          (
		                          		packageName,
		                          		simpleFileName + "AppRunner",
		                          		packageName + "." + simpleFileName + "App"
		                          );
	}

	public void run() throws GeneratorException, IOException
	{
		generatorView          .run();
		generatorViewService   .run();
		generatorViewController.run();
		generatorViewFXML      .run();
		generatorViewCSS       .run();
		generatorApp           .run();
		generatorAppRunner     .run();
	}

	public static void main(String[] args) throws GeneratorException, IOException
	{
		GeneratorFXCompBundle generator;
		generator = new GeneratorFXCompBundle(GeneratorFXCompBundle.class.getPackageName() + ".demo" , "Demo" );
		generator.run();
//		generator = new GeneratorFXCompBundle("de.ruu.app.taggable.client.fx.tagmngmnt.member", "Members");
//		generator.run();
//		generator = new GeneratorFXCompBundle("de.ruu.app.taggable.client.fx.tagmngmnt.tag"   , "Tags"   );
//		generator.run();
	}
}