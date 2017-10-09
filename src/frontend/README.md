## FrontEnd

Uses ANTLR4 to parse the input which conforms to the Cymbol grammar.

### How to get up and running with the project
#### The easy way
* Install Eclipse and set up the [antlr4ide](https://github.com/antlr4ide/antlr4ide) plugin
* Follow the [tutorial](https://github.com/antlr4ide/antlr4ide) to configure the buildpath and project properties
* Run the `Driver.java` file with the required file name to get the output

#### The hard way
* Install ANTLR and set up the classpaths accordingly
* Compile the g4 file using `-visitor -no-listener` flags
* Compile and run the `Driver.java` file with required file name to get the output


### Stuff used to make this:

 * [ANTLR v4.7](http://www.antlr.org/)
 * [antlr4ide](https://github.com/antlr4ide/antlr4ide)
