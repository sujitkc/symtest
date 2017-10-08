## FrontEnd


### Roadmap
* Define a grammar for the subset of C language that should be supported by the front-end.
* Use (JLex + Java CUP) / ANTLR to parse the language and create the AST
* Convert the AST into CFG using Interfaces & Classes defined in SymTest
* Take Target Edges as input 


### Doubts
* Possible ways to take target edge set (Edges in the graph or line-numbers in the code?)


### Running the frontend
* Install [ANTLRv4](http://www.antlr.org/) and set up the environment variables:
```
LINUX
$ cd /usr/local/lib
$ wget http://www.antlr.org/download/antlr-4.7-complete.jar
$ export CLASSPATH=".:/usr/local/lib/antlr-4.7-complete.jar:$CLASSPATH"
$ alias antlr4='java -jar /usr/local/lib/antlr-4.7-complete.jar'
$ alias grun='java org.antlr.v4.gui.TestRig'
```
* Generating the lexer-parser:
```
$ antlr4 Cymbol.g4 -visitor -no-listener
$ javac *.java
```

### Stuff used to make this:

 * [ANTLR v4.7](http://www.antlr.org/)
