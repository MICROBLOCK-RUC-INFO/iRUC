{
  function buildTree(type, properties) {
    return Object.assign({ type: type }, properties);
  }
  
  function extractText(elements) {
    return elements.map(e => Array.isArray(e) ? e.join('') : e).join('');
  }
}

start
  = _ service:service _ { return service; }

service
  = "service" _ name:serviceName _ "{" _ statements:statement* _ "}"
    { return buildTree("service", { name: name, statements: statements }); }

statement
  = _ stmt:(dbOperation / pluginCall / outputStatement / callStatement / returnStatement / comment) _
    { return stmt; }

dbOperation
  = "new" _ name:variableName _ "=" _ operation:gqlOperation ";"
    { return buildTree("db_operation", { variable: name, operation: operation, terminated: true }); }

gqlOperation
  = "gql" _ type:("query" / "mutation") _ "{" _ body:gqlBody _ "}"
    { return buildTree("gql_operation", { operationType: type, body: body }); }

gqlBody
  = chars:gqlChar*
    { return chars.join("").trim(); }

gqlChar
  = [^}]

pluginCall
  = "new" _ name:variableName _ "=" _ plugin:pluginPath "/" func:identifier "(" params:parameterList? ")" ";"
    { return buildTree("plugin_call", { variable: name, plugin: plugin, function: func, parameters: params || [], terminated: true }); }

pluginPath
  = name:identifier "." ext:identifier
    { return name + "." + ext; }

outputStatement
  = "output" _ target:qualifiedName _ "=" _ source:qualifiedName ";"
    { return buildTree("output", { target: target, source: source, terminated: true }); }

callStatement
  = "call" _ service:serviceName ";"
    { return buildTree("call", { service: service, terminated: true }); }

returnStatement
  = "return" _ variable:variableName ";"
    { return buildTree("return", { variable: variable, terminated: true }); }

qualifiedName
  = service:identifier "." variable:identifier
    { return service + "." + variable; }
  / identifier

variableName
  = name:identifier digits:eightDigits
    { return name + digits; }

eightDigits
  = d1:[0-9] d2:[0-9] d3:[0-9] d4:[0-9] d5:[0-9] d6:[0-9] d7:[0-9] d8:[0-9]
    { return d1 + d2 + d3 + d4 + d5 + d6 + d7 + d8; }

parameterList
  = first:parameter rest:("," _ parameter)*
    { return [first].concat(rest.map(r => r[2])); }

parameter
  = qualifiedName / variableName / literal

literal
  = "${" content:literalContent "}"
    { return "${" + content + "}"; }
  / quotedString
  / number

literalContent
  = chars:literalChar*
    { return chars.join(""); }

literalChar
  = [^}]

quotedString
  = "\"" chars:quotedChar* "\""
    { return "\"" + chars.join("") + "\""; }

quotedChar
  = [^"]

number
  = digits:[0-9]+
    { return digits.join(""); }

serviceName
  = identifier

identifier
  = first:[a-zA-Z_] rest:[a-zA-Z0-9_-]*
    { return first + rest.join(""); }

comment
  = "//" content:[^\n]*
    { return buildTree("comment", { content: "//" + content.join("") }); }
  / "/*" content:multiLineComment "*/"
    { return buildTree("comment", { content: "/*" + content + "*/" }); }

multiLineComment
  = chars:multiLineChar*
    { return chars.join(""); }

multiLineChar
  = !"*/" char:.
    { return char; }

_ "whitespace"
  = [ \t\n\r]*
