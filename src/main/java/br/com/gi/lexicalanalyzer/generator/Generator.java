package br.com.gi.lexicalanalyzer.generator;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Generator {
    private static final String BASE = "src/main/java/br/com/gi/lexicalanalyzer";

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("").toAbsolutePath();
        Path lexerDir  = root.resolve(BASE + "/lexer");
        Path parserDir = root.resolve(BASE + "/parser");

        // Gera o analisador Lexico
        jflex.Main.main(new String[]{
                "-d", lexerDir.toString(),
                lexerDir.resolve("language.lex").toString()
        });

        // Gera o analisador Sintático
        java_cup.Main.main(new String[]{
                "-destdir", parserDir.toString(),
                "-parser", "Parser",
                "-symbols", "sym",
                parserDir.resolve("parser.cup").toString()
        });
    }
}
