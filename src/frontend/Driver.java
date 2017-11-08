package frontend;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class Driver {
        public static void main(String[] args) throws Exception {
                String inputFile = null;
                InputStream is = System.in;
//                inputFile = "/Users/athul/src/SKC/symtest/src/testcases/Timer.cymbol";
                inputFile = "/Users/athul/src/SKC/symtest/src/testcases/Temp.cymbol";
//                inputFile = "/Users/athul/src/SKC/symtest/src/testcases/SimpleInput.cymbol";
                if ( inputFile!=null ) is = new FileInputStream(inputFile);
                ANTLRInputStream input = new ANTLRInputStream(is);
                CymbolLexer lexer = new CymbolLexer(input); 
                CommonTokenStream tokens = new CommonTokenStream(lexer); 
                CymbolParser parser = new CymbolParser(tokens); 
                ParseTree tree = parser.file(); 
                CFGVisitor cfg = new CFGVisitor(inputFile + ".target");
                cfg.visit(tree);
        }
}
