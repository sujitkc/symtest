package frontend;

import java.io.File;
import java.io.PrintWriter;

public class CFGVisualizer {
	
	//TODO Place into central config file
	private String DOT_PATH = "/usr/local/bin/dot";
	private String DOT_OPTIONS = "-Tpng -oresources/cfg.png";
	private String inputFile = "resources/cfg.dot";
	private String command = DOT_PATH + " " + DOT_OPTIONS + " " 
			+ inputFile;
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
			System.out.println("EXECUTED");
			Process p = Runtime.getRuntime().exec(command);
			System.out.println("@@@@ EXECUTED");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
