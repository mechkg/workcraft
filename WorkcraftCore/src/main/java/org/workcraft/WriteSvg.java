package org.workcraft;

import java.io.File;

import org.workcraft.interop.ModelServices;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;

public class WriteSvg {

	@Deprecated // never call this from java code. will terminate the program!
	public static void main(String[] args)  throws Throwable { 
		int result = _main(args);
		System.exit(result);
	}
	
	public static int _main(String[] args) throws Throwable {
		if(args.length != 2) {
			System.out.println("need two arguments: path to source .work; path to destination .svg");
			return 1;
		}
		else {
			Framework framework = new Framework();
			framework.initPlugins();
			ModelServices loaded = framework.load(args[0]);
			Export.exportToFile(loaded, new File(args[1]), Format.SVG, framework.getPluginManager());
			System.out.println("svg saved to '" + args[1]+"'");
			return 0;
		}
	}
}
