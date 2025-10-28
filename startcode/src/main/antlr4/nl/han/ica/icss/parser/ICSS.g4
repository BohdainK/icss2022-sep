grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---

stylesheet: EOF;

// A Declaration defines a style property. Declarations are things like "width: 100px"
declaration: propertyName COLON expression SEMICOLON;

// Name of property
propertyName: LOWER_IDENT;


// The rule of a selector, like "{color: pink;}"
styleRule: selector OPEN_BRACE ruleBody CLOSE_BRACE;

// the body of the style rule
ruleBody: (declaration | ifClause | variableAssignment)*;

// The CSS class selector matches elements based on the contents of their class attribute. e.g. ".button"
classSelector: CLASS_IDENT;

// same for id selector, like "#header"
idSelector: ID_IDENT;

// tag selector, like "p"
tagSelector: LOWER_IDENT;

// defines a selector that can be a class, id or tag selector
selector: classSelector | idSelector | tagSelector;


// defines a variable assignment, e.g. "LinkColor := #ff0000;"
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression+ SEMICOLON;
variableReference: CAPITAL_IDENT;


// value literals
boolLiteral: TRUE | FALSE;
colorLiteral: COLOR;
percentageLiteral: PERCENTAGE;
pixelLiteral: PIXELSIZE;
scalarLiteral: SCALAR;
literal: boolLiteral | colorLiteral | percentageLiteral | pixelLiteral | scalarLiteral | variableReference;

// defines expressions and order of operations
expression: literal | expression (MUL) expression | expression (PLUS | MIN) expression;

// if-else clause
ifClause: IF BOX_BRACKET_OPEN (variableReference | boolLiteral) BOX_BRACKET_CLOSE OPEN_BRACE ruleBody CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE ruleBody CLOSE_BRACE;

