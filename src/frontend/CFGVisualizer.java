package frontend;

/**
 * Uses GraphViz to create the CFG diagram. All the edges in the CFG created in
 * the code is labeled as L1->L2 to make it easier to generate the DOT code.
 * 
 */

import java.io.File;
import java.io.PrintWriter;

import configuration.SymTestConfiguration;

public class CFGVisualizer {

	// TODO Place into central config file
	private String DOT_OPTIONS = "-Tpng -oresources/cfg.png";

	private String inputFile = "resources/cfg.dot";
	private String command = SymTestConfiguration.DOT_PATH + " " + DOT_OPTIONS + " " + inputFile;

	private StringBuilder dotContent = new StringBuilder();

	public CFGVisualizer() {
		dotContent.append("digraph G {");
	}

	public void addLink(String link, boolean isTarget) {
		dotContent.append(link);
		if (isTarget)
			dotContent.append("[color=red, pendwidth=3.0];");
		else
			dotContent.append(";");
	}

	public void execute() {
		try {
			dotContent.append("}");
			PrintWriter writer = new PrintWriter(new File(inputFile));
			writer.write(dotContent.toString());
			writer.close();
			Process p = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
