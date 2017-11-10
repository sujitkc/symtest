package frontend;

/**
 * The driver function for SymTest. Parses the input file, and calls the
 * visitor. If the input file has the name xxx.cymbol, the target list is
 * expected to be present in a file xxx.cymbol.target.
 * Target file format is:
 * LINE_NO - COVERAGE_TYPE
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Driver {

	private static final Logger logger = Logger
			.getLogger(Driver.class.getName());

	public static void main(String[] args) throws Exception {
		InputStream is = null;
		//USE THIS to specify the input file
		String inputFile = "/Users/athul/src/SKC/symtest/test_programs/Test2.cymbol";
		if (inputFile != null)
			is = new FileInputStream(inputFile);
		logger.info("Input file: " + inputFile);

		// ANTLR Boilerplate code
		ANTLRInputStream input = new ANTLRInputStream(is);
		CymbolLexer lexer = new CymbolLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CymbolParser parser = new CymbolParser(tokens);
		ParseTree tree = parser.file();
		CFGVisitor visitor = new CFGVisitor(inputFile + ".target");
		visitor.visit(tree);
	}
}
