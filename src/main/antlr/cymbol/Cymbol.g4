grammar Cymbol;

@header {
package com.symtest.cymbol;
}

file    :   (functionDecl | varDecl)+;

varDecl :   type ID ('=' expr)? ';'
        ;

type    :   'boolean' | 'int' | 'void' | 'real';

functionDecl
    :       type ID '('(formalParameters)?')' block
    ;

formalParameters
    :   formalParameter (',' formalParameter)*
    ;

formalParameter
    :   type ID
    ;


/** one block contains many statement */
block:  '{' stat* '}'    
     ;

/** stat : statement */
stat    :   block                           #StatBlock
        |   varDecl                         #Decl
        |   'while' expr stat               #While
        |   'if' '('expr')' stat ('else' stat)?   #If
        |   'return' expr? ';'              #Return
        |   expr '=' expr  ';'              #Assign
        |   expr ';'                        #Exp
        ;

expr    :   ID '(' exprList? ')'            #Call
        |   'input()'                       #Input
	|   'booleaninput()'                #BooleanInput
	|   expr '[' expr ']'               #Index
        |   '!' expr                        #Not
        |   expr '*' expr                   #Mult
        |   expr ('+' | '-') expr           #AddSub
        |   expr '==' expr                  #Equal
        |   expr '!=' expr                  #NotEqual
        |   expr '>' expr                   #GreaterThan
        |   expr '>=' expr                  #GreaterThanEqual
        |   expr '<' expr                   #LessThan
        |   expr '<=' expr                  #LessThanEqual
        |   expr '&&' expr                  #AndExp
        |   expr '||' expr                  #OrExp
        |   ID                              #Var
        |   MINUS? INT                      #Int
	|   MINUS? REAL                     #Real
	|   BOOL				#Bool
	|   '(' expr ')'                    #Parens
        ;

exprList:   expr (',' expr)*;

MINUS   : '-';
ID      : LETTER(LETTER | [0-9])*;
fragment
LETTER  :   [a-zA-Z_];
INT     :   [0-9]+;
WS      :   [ \t\n\r]+ -> skip ;

SL_COMMENT
    :   '//' .*? '\n' -> skip
    ;
