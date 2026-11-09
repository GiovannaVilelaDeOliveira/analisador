package br.com.gi.lexicalanalyzer.lexer;

import br.com.gi.lexicalanalyzer.model.TokenMaoMao;
import br.com.gi.lexicalanalyzer.parser.sym;

import java_cup.runtime.Symbol;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

%%

%{

public List<String> analize = new ArrayList<>();
public List<String> errosLexicos = new ArrayList<>();
private Map<String, TokenMaoMao> contagem = new LinkedHashMap<>();

public List<TokenMaoMao> getTokenMaoMao() {
    return new ArrayList<>(contagem.values());
}

private void imprimir(String descricao, String lexema) {
    analize.add(lexema + " - " + descricao);

    String chave = lexema + "|" + descricao;
    TokenMaoMao t = contagem.get(chave);
    if (t == null) {
        t = new TokenMaoMao();
        t.lexema = lexema;
        t.descricao = descricao;
        contagem.put(chave, t);
    }
    t.quantidade++;
}

/** Registra o token e devolve o Symbol correspondente para o parser (CUP). */
private Symbol token(int tipo, String descricao) {
    imprimir(descricao, yytext());
    return new Symbol(tipo, yyline + 1, yycolumn + 1, yytext());
}

/** Erro léxico: registra e segue a análise sem enviar token ao parser. */
private void erro(String descricao) {
    imprimir(descricao, yytext());
    errosLexicos.add("Linha " + (yyline + 1) + ", coluna " + (yycolumn + 1)
            + ": " + descricao + " '" + yytext() + "'");
}

%}

%class LexicalAnalyzer
%public
%cup
%line
%column
%unicode
%xstate VAR, CLASS

BRANCO           = [\n | \t | \r | \" \"]

INTEIRO          = [0-9]+
FLUTUANTE        = [0-9]+"."[0-9]+
BOOLEANO         = "true" | "false"
LITERAL_TEXTO    = \"[^\"]*\"

ID               = [a-zA-Z][a-zA-Z0-9_\-?!]*

%%

<YYINITIAL, VAR, CLASS> {BRANCO} { /* ignora */ }

<YYINITIAL> {
  "Mao_"             { yybegin(VAR); return token(sym.MAO_ABRE, "Abre variável"); }
  "_Mao"             { erro("ERRO - Fecha variável sem abrir"); }
  "\u{1F48A}"        { erro("ERRO - Marcador de tipo fora de contexto"); }

  "Garfield"         { return token(sym.T_BOOLEAN, "Tipo boolean"); }
  "Tom"              { return token(sym.T_STRING, "Tipo String"); }
  "Gumball"          { return token(sym.T_INT, "Tipo inteiro"); }
  "Frajola"          { return token(sym.T_FLOAT, "Tipo float"); }

  "meow"             { return token(sym.IF, "Palavra reservada (if)"); }
  "grrrrr"           { return token(sym.ELSE, "Palavra reservada (else)"); }
  "rrr-rrr"          { return token(sym.WHILE, "Palavra reservada (while)"); }
  "miau"             { return token(sym.MAIN, "Palavra reservada (main)"); }
  "ronron"           { return token(sym.PUBLIC, "Palavra reservada (public)"); }
  "fsssss"           { return token(sym.PRIVATE, "Palavra reservada (private)"); }
  "grooooor"         { return token(sym.FINAL, "Palavra reservada (final)"); }
  "raaaaah"          { return token(sym.STATIC, "Palavra reservada (static)"); }
  "Nham-Nham"        { return token(sym.INPUT, "Entrada de dados"); }
  "Ptooey"           { return token(sym.OUTPUT, "Saída de dados"); }
  "Pspsps"           { return token(sym.ASSIGN, "Atribuição"); }

  "[w]"              { yybegin(CLASS); return token(sym.CLASS, "Palavra reservada (class)"); }
  "<w<"              { return token(sym.RETURN, "Palavra reservada (return)"); }
  "0w0"              { return token(sym.VOID, "Palavra reservada (void)"); }

  /* ---- Esses códigos são para os emojis ---- */
  "\u{1F408}\u200D\u{2B1B}"  { return token(sym.FECHA_BLOCO, "Fecha bloco (🐈‍⬛)"); }
  "\u{1F408}"                { return token(sym.ABRE_BLOCO, "Abre bloco (🐈)"); }
  "!"                        { return token(sym.FIM, "Fim de instrução"); }
  "....."                    { return token(sym.PAREN, "Parêntese (......)"); }

  ">=w="             { return token(sym.GE, "Operador maior ou igual"); }
  "<=w="             { return token(sym.LE, "Operador menor ou igual"); }
  ">wo"              { return token(sym.GT, "Operador maior que"); }
  "ow<"              { return token(sym.LT, "Operador menor que"); }
  "!w!"              { return token(sym.NE, "Operador diferente"); }
  "=w="              { return token(sym.EQ, "Operador igual"); }
  "&w&"              { return token(sym.AND, "Operador lógico E (and)"); }
  "~w~"              { return token(sym.OR, "Operador lógico OU (or)"); }

  "+w+"              { return token(sym.PLUS, "Operador adição"); }
  "-w-"              { return token(sym.MINUS, "Operador subtração"); }
  "*w*"              { return token(sym.TIMES, "Operador multiplicação"); }
  "/w/"              { return token(sym.DIV, "Operador divisão"); }
  "%w%"              { return token(sym.MOD, "Operador resto"); }
  "^w^"              { return token(sym.POW, "Operador elevação"); }
  "vwv"              { return token(sym.SQRT, "Operador raiz quadrada"); }

  {BOOLEANO}         { return token(sym.BOOLEANO, "Booleano"); }
  {FLUTUANTE}        { return token(sym.FLUTUANTE, "Número flutuante"); }
  {INTEIRO}          { return token(sym.INTEIRO, "Número inteiro"); }
  {LITERAL_TEXTO}    { return token(sym.TEXTO, "Literal de texto"); }

  {ID}               { erro("ERRO - Palavra não reconhecida (fora de contexto)"); }

  . { erro("ERRO - Caractere inválido"); }
}

<VAR> {
  "_Mao"             { yybegin(YYINITIAL); return token(sym.MAO_FECHA, "Fecha variável"); }
  "\u{1F48A}"        { return token(sym.PILULA, "Marcador de tipo (💊)"); }
  "Garfield"         { return token(sym.T_BOOLEAN, "Tipo boolean"); }
  "Tom"              { return token(sym.T_STRING, "Tipo String"); }
  "Gumball"          { return token(sym.T_INT, "Tipo inteiro"); }
  "Frajola"          { return token(sym.T_FLOAT, "Tipo float"); }

  {ID}               { return token(sym.ID, "Identificador"); }
  .                  { erro("ERRO - Caractere inválido dentro de variável"); }
}

<CLASS> {
  {ID}               { yybegin(YYINITIAL); return token(sym.ID_CLASSE, "Identificador de Classe"); }
  .                  { yybegin(YYINITIAL); yypushback(1); }
}
