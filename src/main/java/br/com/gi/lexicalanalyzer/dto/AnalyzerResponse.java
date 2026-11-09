package br.com.gi.lexicalanalyzer.dto;

import java.util.List;

public class AnalyzerResponse {
    private List<String> analize;
    private List<TokenInfo> tokens;
    private String error;
    private boolean sintaticoOk;
    private List<String> errosSintaticos;
    private List<String> errosLexicos;
    private boolean semanticoOk;
    private List<String> errosSemanticos;

    public static class TokenInfo {
        private String lexema;
        private String descricao;
        private int quantidade;

        public TokenInfo() {}

        public TokenInfo(String lexema, String descricao, int quantidade) {
            this.lexema = lexema;
            this.descricao = descricao;
            this.quantidade = quantidade;
        }

        public String getLexema() { return lexema; }
        public void setLexema(String lexema) { this.lexema = lexema; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    }

    public AnalyzerResponse() {}

    public List<String> getAnalize() { return analize; }
    public void setAnalize(List<String> analize) { this.analize = analize; }
    public List<TokenInfo> getTokens() { return tokens; }
    public void setTokens(List<TokenInfo> tokens) { this.tokens = tokens; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public boolean isSintaticoOk() { return sintaticoOk; }
    public void setSintaticoOk(boolean sintaticoOk) { this.sintaticoOk = sintaticoOk; }
    public List<String> getErrosSintaticos() { return errosSintaticos; }
    public void setErrosSintaticos(List<String> errosSintaticos) { this.errosSintaticos = errosSintaticos; }
    public List<String> getErrosLexicos() { return errosLexicos; }
    public void setErrosLexicos(List<String> errosLexicos) { this.errosLexicos = errosLexicos; }
    public boolean isSemanticoOk() { return semanticoOk; }
    public void setSemanticoOk(boolean semanticoOk) { this.semanticoOk = semanticoOk; }
    public List<String> getErrosSemanticos() { return errosSemanticos; }
    public void setErrosSemanticos(List<String> errosSemanticos) { this.errosSemanticos = errosSemanticos; }
}
