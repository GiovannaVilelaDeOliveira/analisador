package br.com.gi.lexicalanalyzer.controller;

import br.com.gi.lexicalanalyzer.parser.BottomUpParser;
import br.com.gi.lexicalanalyzer.parser.BottomUpParser.Resultado;
import br.com.gi.lexicalanalyzer.parser.BottomUpParser.TabelaSLRData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BottomUpController {

    @PostMapping("/bottomup")
    public ResponseEntity<Resultado> analyzeBottomUp(@RequestBody Map<String, String> request) {
        String entrada = request.get("entrada");
        if (entrada == null || entrada.isBlank()) {
            Resultado r = new Resultado();
            r.aceito = false;
            r.erros.add("Entrada não pode estar vazia.");
            r.tabelaData = BottomUpParser.getTabelaData();
            return ResponseEntity.badRequest().body(r);
        }

        BottomUpParser parser = new BottomUpParser();
        Resultado resultado = parser.analisar(entrada);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/bottomup/tabela")
    public ResponseEntity<TabelaSLRData> getTabelaSLR() {
        return ResponseEntity.ok(BottomUpParser.getTabelaData());
    }
}

