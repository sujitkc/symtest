import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.FileInputStream;
import java.io.InputStream;

public class Driver {
        public static void main(String[] args) throws Exception {
                String inputFile = null;
                if ( args.length>0 ) inputFile = args[0];
                InputStream is = System.in;
                if ( inputFile!=null ) is = new FileInputStream(inputFile);
                ANTLRInputStream input = new ANTLRInputStream(is);
                CymbolLexer lexer = new CymbolLexer(input); 
                CommonTokenStream tokens = new CommonTokenStream(lexer); 
                CymbolParser parser = new CymbolParser(tokens); 
                ParseTree tree = parser.file(); 
                //System.out.println(tree.toStringTree(parser)); // print tree as text <label id="code.tour.main.7"/>
                CFGVisitor cfg = new CFGVisitor();
                cfg.visit(tree);
        }
}
