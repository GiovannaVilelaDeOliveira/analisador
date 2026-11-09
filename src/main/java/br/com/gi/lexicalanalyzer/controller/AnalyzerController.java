package br.com.gi.lexicalanalyzer.controller;

import br.com.gi.lexicalanalyzer.dto.AnalyzerResponse;
import br.com.gi.lexicalanalyzer.lexer.LexicalAnalyzer;
import br.com.gi.lexicalanalyzer.model.TokenMaoMao;
import br.com.gi.lexicalanalyzer.parser.Parser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzerController {

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzerResponse> analyze(@RequestBody Map<String, String> request) {
        AnalyzerResponse response = new AnalyzerResponse();

        String codigo = request.get("codigo");
        if (codigo == null || codigo.isBlank()) {
            response.setError("Código não pode estar vazio.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            LexicalAnalyzer lexical = new LexicalAnalyzer(new StringReader(codigo));
            Parser parser = new Parser(lexical);
            parser.parse();

            response.setAnalize(lexical.analize);

            response.setErrosLexicos(lexical.errosLexicos);
            response.setErrosSintaticos(parser.erros);
            response.setErrosSemanticos(parser.errosSemanticos);
            response.setSintaticoOk(parser.erros.isEmpty());
            response.setSemanticoOk(parser.errosSemanticos.isEmpty());

            List<AnalyzerResponse.TokenInfo> tokens = new ArrayList<>();
            for (TokenMaoMao t : lexical.getTokenMaoMao()) {
                tokens.add(new AnalyzerResponse.TokenInfo(t.lexema, t.descricao, t.quantidade));
            }
            response.setTokens(tokens);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setError("Erro na análise: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
