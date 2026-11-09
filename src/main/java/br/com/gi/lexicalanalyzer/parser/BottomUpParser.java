package br.com.gi.lexicalanalyzer.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analisador Sintático Bottom-Up (SLR) para a gramática do laço (rrr-rrr)
 * da Linguagem MaoMao.
 *
 * Gramática (aumentada):
 * (0) S' → S
 * (1) S → laco
 * (2) laco → rrr-rrr ..... cond ..... bloco
 * (3) cond → expr RELOP expr
 * (4) bloco → 🐈 lista 🐈‍⬛
 * (5) lista → lista comando
 * (6) lista → comando
 * (7) comando → VAR Pspsps expr !
 * (8) expr → expr +w+ termo
 * (9) expr → termo
 * (10) termo → termo *w* fator
 * (11) termo → fator
 * (12) fator → NUM
 * (13) fator → VAR
 *
 * Terminais MaoMao:
 * WHILE : rrr-rrr
 * PAREN : .....
 * RELOP : >wo | ow< | =w= | >=w= | <=w= | !w!
 * ABRE_BLOCO : 🐈
 * FECHA_BLOCO : 🐈‍⬛
 * VAR : Mao_ id _Mao
 * ASSIGN : Pspsps
 * FIM : !
 * PLUS : +w+
 * TIMES : *w*
 * NUM : inteiros (ex: 10, 0, 1)
 * $ : Fim de arquivo
 */
public class BottomUpParser {

    // ========================== Terminais ==========================
    public static final int T_WHILE = 0; // rrr-rrr
    public static final int T_PAREN = 1; // .....
    public static final int T_RELOP = 2; // >wo, ow<, =w=, etc.
    public static final int T_ABRE_BLOCO = 3; // 🐈
    public static final int T_FECHA_BLOCO = 4; // 🐈‍⬛
    public static final int T_VAR = 5; // Mao_ id _Mao
    public static final int T_ASSIGN = 6; // Pspsps
    public static final int T_FIM = 7; // !
    public static final int T_PLUS = 8; // +w+
    public static final int T_TIMES = 9; // *w*
    public static final int T_NUM = 10; // 0..9
    public static final int T_DOLLAR = 11; // $

    private static final int NUM_TERMINAIS = 12;

    // ========================== Não-terminais ==========================
    public static final int NT_S = 0;
    public static final int NT_LACO = 1;
    public static final int NT_COND = 2;
    public static final int NT_BLOCO = 3;
    public static final int NT_LISTA = 4;
    public static final int NT_COMANDO = 5;
    public static final int NT_EXPR = 6;
    public static final int NT_TERMO = 7;
    public static final int NT_FATOR = 8;

    private static final int NUM_NAO_TERMINAIS = 9;
    private static final int NUM_ESTADOS = 28;

    private static final int ACEITAR = Integer.MAX_VALUE;
    private static final int ERRO = 0;

    // ACTION[estado][terminal]
    private static final int[][] ACTION = new int[NUM_ESTADOS][NUM_TERMINAIS];

    // GOTO[estado][nao_terminal]
    private static final int[][] GOTO = new int[NUM_ESTADOS][NUM_NAO_TERMINAIS];

    // ========================== Produções ==========================
    // [lado_esquerdo, tamanho_lado_direito]
    private static final int[][] PRODUCOES = {
            { NT_S, 1 }, // (0) S' → S
            { NT_S, 1 }, // (1) S → laco
            { NT_LACO, 5 }, // (2) laco → rrr-rrr ..... cond ..... bloco
            { NT_COND, 3 }, // (3) cond → expr RELOP expr
            { NT_BLOCO, 3 }, // (4) bloco → 🐈 lista 🐈‍⬛
            { NT_LISTA, 2 }, // (5) lista → lista comando
            { NT_LISTA, 1 }, // (6) lista → comando
            { NT_COMANDO, 4 }, // (7) comando → VAR Pspsps expr !
            { NT_EXPR, 3 }, // (8) expr → expr +w+ termo
            { NT_EXPR, 1 }, // (9) expr → termo
            { NT_TERMO, 3 }, // (10) termo → termo *w* fator
            { NT_TERMO, 1 }, // (11) termo → fator
            { NT_FATOR, 1 }, // (12) fator → NUM
            { NT_FATOR, 1 }, // (13) fator → VAR
    };

    private static final String[] NOMES_PRODUCOES = {
            "S' → S",
            "S → laco",
            "laco → rrr-rrr ..... cond ..... bloco",
            "cond → expr RELOP expr",
            "bloco → 🐈 lista 🐈‍⬛",
            "lista → lista comando",
            "lista → comando",
            "comando → VAR Pspsps expr !",
            "expr → expr +w+ termo",
            "expr → termo",
            "termo → termo *w* fator",
            "termo → fator",
            "fator → NUM",
            "fator → VAR",
    };

    private static final String[] NOMES_TERMINAIS = {
            "rrr-rrr", ".....", "RELOP", "🐈", "🐈‍⬛", "VAR", "Pspsps", "!", "+w+", "*w*", "NUM", "$"
    };

    private static final String[] NOMES_NAO_TERMINAIS = {
            "S", "laco", "cond", "bloco", "lista", "comando", "expr", "termo", "fator"
    };

    // ========================== Construção da Tabela SLR
    // ==========================
    static {
        for (int[] row : ACTION)
            Arrays.fill(row, ERRO);
        for (int[] row : GOTO)
            Arrays.fill(row, -1);

        // I0
        ACTION[0][T_WHILE] = 3; // s3
        GOTO[0][NT_S] = 1;
        GOTO[0][NT_LACO] = 2;

        // I1
        ACTION[1][T_DOLLAR] = ACEITAR;

        // I2: S → laco. FOLLOW(S) = { $ }
        ACTION[2][T_DOLLAR] = -1; // r1: S → laco

        // I3: laco → rrr-rrr . ..... cond ..... bloco
        ACTION[3][T_PAREN] = 4; // s4

        // I4: laco → rrr-rrr ..... . cond ..... bloco
        ACTION[4][T_NUM] = 9; // s9
        ACTION[4][T_VAR] = 10; // s10
        GOTO[4][NT_COND] = 5;
        GOTO[4][NT_EXPR] = 6;
        GOTO[4][NT_TERMO] = 7;
        GOTO[4][NT_FATOR] = 8;

        // I5: laco → rrr-rrr ..... cond . ..... bloco
        ACTION[5][T_PAREN] = 11; // s11

        // I6: cond → expr . RELOP expr | expr → expr . +w+ termo
        ACTION[6][T_RELOP] = 12; // s12
        ACTION[6][T_PLUS] = 13; // s13

        // I7: expr → termo. FOLLOW(expr) = { RELOP, ....., !, +w+ }
        ACTION[7][T_RELOP] = -9; // r9: expr → termo
        ACTION[7][T_PAREN] = -9;
        ACTION[7][T_FIM] = -9;
        ACTION[7][T_PLUS] = -9;
        ACTION[7][T_TIMES] = 14; // s14

        // I8: termo → fator. FOLLOW(termo) = { RELOP, ....., !, +w+, *w* }
        ACTION[8][T_RELOP] = -11; // r11: termo → fator
        ACTION[8][T_PAREN] = -11;
        ACTION[8][T_FIM] = -11;
        ACTION[8][T_PLUS] = -11;
        ACTION[8][T_TIMES] = -11;

        // I9: fator → NUM. FOLLOW(fator) = { RELOP, ....., !, +w+, *w* }
        ACTION[9][T_RELOP] = -12; // r12: fator → NUM
        ACTION[9][T_PAREN] = -12;
        ACTION[9][T_FIM] = -12;
        ACTION[9][T_PLUS] = -12;
        ACTION[9][T_TIMES] = -12;

        // I10: fator → VAR. FOLLOW(fator) = { RELOP, ....., !, +w+, *w* }
        ACTION[10][T_RELOP] = -13; // r13: fator → VAR
        ACTION[10][T_PAREN] = -13;
        ACTION[10][T_FIM] = -13;
        ACTION[10][T_PLUS] = -13;
        ACTION[10][T_TIMES] = -13;

        // I11: laco → rrr-rrr ..... cond ..... . bloco
        ACTION[11][T_ABRE_BLOCO] = 16; // s16
        GOTO[11][NT_BLOCO] = 15;

        // I12: cond → expr RELOP . expr
        ACTION[12][T_NUM] = 9; // s9
        ACTION[12][T_VAR] = 10; // s10
        GOTO[12][NT_EXPR] = 17;
        GOTO[12][NT_TERMO] = 7;
        GOTO[12][NT_FATOR] = 8;

        // I13: expr → expr +w+ . termo
        ACTION[13][T_NUM] = 9; // s9
        ACTION[13][T_VAR] = 10; // s10
        GOTO[13][NT_TERMO] = 18;
        GOTO[13][NT_FATOR] = 8;

        // I14: termo → termo *w* . fator
        ACTION[14][T_NUM] = 9; // s9
        ACTION[14][T_VAR] = 10; // s10
        GOTO[14][NT_FATOR] = 19;

        // I15: laco → rrr-rrr ..... cond ..... bloco . FOLLOW(laco) = { $ }
        ACTION[15][T_DOLLAR] = -2; // r2: laco → ...

        // I16: bloco → 🐈 . lista 🐈‍⬛
        ACTION[16][T_VAR] = 22; // s22
        GOTO[16][NT_LISTA] = 20;
        GOTO[16][NT_COMANDO] = 21;

        // I17: cond → expr RELOP expr . | expr → expr . +w+ termo
        // FOLLOW(cond) = { ..... }
        ACTION[17][T_PAREN] = -3; // r3: cond → expr RELOP expr
        ACTION[17][T_PLUS] = 13; // s13

        // I18: expr → expr +w+ termo . | termo → termo . *w* fator
        ACTION[18][T_RELOP] = -8; // r8: expr → expr +w+ termo
        ACTION[18][T_PAREN] = -8;
        ACTION[18][T_FIM] = -8;
        ACTION[18][T_PLUS] = -8;
        ACTION[18][T_TIMES] = 14; // s14

        // I19: termo → termo *w* fator .
        ACTION[19][T_RELOP] = -10; // r10: termo → termo *w* fator
        ACTION[19][T_PAREN] = -10;
        ACTION[19][T_FIM] = -10;
        ACTION[19][T_PLUS] = -10;
        ACTION[19][T_TIMES] = -10;

        // I20: bloco → 🐈 lista . 🐈‍⬛ | lista → lista . comando
        ACTION[20][T_FECHA_BLOCO] = 23; // s23
        ACTION[20][T_VAR] = 22; // s22
        GOTO[20][NT_COMANDO] = 24;

        // I21: lista → comando . FOLLOW(lista) = { 🐈‍⬛, VAR }
        ACTION[21][T_FECHA_BLOCO] = -6; // r6: lista → comando
        ACTION[21][T_VAR] = -6;

        // I22: comando → VAR . Pspsps expr !
        ACTION[22][T_ASSIGN] = 25; // s25

        // I23: bloco → 🐈 lista 🐈‍⬛ . FOLLOW(bloco) = { $ }
        ACTION[23][T_DOLLAR] = -4; // r4: bloco → 🐈 lista 🐈‍⬛

        // I24: lista → lista comando . FOLLOW(lista) = { 🐈‍⬛, VAR }
        ACTION[24][T_FECHA_BLOCO] = -5; // r5: lista → lista comando
        ACTION[24][T_VAR] = -5;

        // I25: comando → VAR Pspsps . expr !
        ACTION[25][T_NUM] = 9; // s9
        ACTION[25][T_VAR] = 10; // s10
        GOTO[25][NT_EXPR] = 26;
        GOTO[25][NT_TERMO] = 7;
        GOTO[25][NT_FATOR] = 8;

        // I26: comando → VAR Pspsps expr . ! | expr → expr . +w+ termo
        ACTION[26][T_FIM] = 27; // s27
        ACTION[26][T_PLUS] = 13; // s13

        // I27: comando → VAR Pspsps expr ! . FOLLOW(comando) = { 🐈‍⬛, VAR }
        ACTION[27][T_FECHA_BLOCO] = -7; // r7: comando → VAR Pspsps expr !
        ACTION[27][T_VAR] = -7;
    }

    // ========================== Token ==========================
    public static class Token {
        public final int tipo;
        public final String valor;

        public Token(int tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }

        @Override
        public String toString() {
            return valor;
        }
    }

    // ========================== Passo Estruturado ==========================
    public static class PassoInfo {
        public int passo;
        public String pilha;
        public String entrada;
        public String acao;
        public String tipoAcao; // "shift", "reduce", "accept", "error"

        public PassoInfo() {
        }

        public PassoInfo(int passo, String pilha, String entrada, String acao, String tipoAcao) {
            this.passo = passo;
            this.pilha = pilha;
            this.entrada = entrada;
            this.acao = acao;
            this.tipoAcao = tipoAcao;
        }

        public int getPasso() {
            return passo;
        }

        public String getPilha() {
            return pilha;
        }

        public String getEntrada() {
            return entrada;
        }

        public String getAcao() {
            return acao;
        }

        public String getTipoAcao() {
            return tipoAcao;
        }
    }

    // ========================== Dados da Tabela SLR ==========================
    public static class TabelaSLRData {
        public List<String> terminais = Arrays.asList(NOMES_TERMINAIS);
        public List<String> naoTerminais = Arrays.asList(NOMES_NAO_TERMINAIS);
        public List<LinhaSLR> linhas = new ArrayList<>();

        public static class LinhaSLR {
            public int estado;
            public List<String> actions = new ArrayList<>();
            public List<String> gotos = new ArrayList<>();

            public LinhaSLR() {
            }

            public LinhaSLR(int estado) {
                this.estado = estado;
            }

            public int getEstado() {
                return estado;
            }

            public List<String> getActions() {
                return actions;
            }

            public List<String> getGotos() {
                return gotos;
            }
        }

        public List<String> getTerminais() {
            return terminais;
        }

        public List<String> getNaoTerminais() {
            return naoTerminais;
        }

        public List<LinhaSLR> getLinhas() {
            return linhas;
        }
    }

    public static TabelaSLRData getTabelaData() {
        TabelaSLRData data = new TabelaSLRData();
        for (int i = 0; i < NUM_ESTADOS; i++) {
            TabelaSLRData.LinhaSLR linha = new TabelaSLRData.LinhaSLR(i);
            for (int j = 0; j < NUM_TERMINAIS; j++) {
                int val = ACTION[i][j];
                String cell;
                if (val == ACEITAR) {
                    cell = "acc";
                } else if (val > 0) {
                    cell = "s" + val;
                } else if (val < 0) {
                    cell = "r" + (-val);
                } else {
                    cell = "";
                }
                linha.actions.add(cell);
            }
            for (int j = 0; j < NUM_NAO_TERMINAIS; j++) {
                int val = GOTO[i][j];
                linha.gotos.add(val >= 0 ? String.valueOf(val) : "");
            }
            data.linhas.add(linha);
        }
        return data;
    }

    // ========================== Resultado ==========================
    public static class Resultado {
        public boolean aceito;
        public List<PassoInfo> passosDetalhados = new ArrayList<>();
        public List<String> passos = new ArrayList<>();
        public List<String> erros = new ArrayList<>();
        public String tabelaSLR;
        public TabelaSLRData tabelaData;

        public boolean isAceito() {
            return aceito;
        }

        public List<PassoInfo> getPassosDetalhados() {
            return passosDetalhados;
        }

        public List<String> getPassos() {
            return passos;
        }

        public List<String> getErros() {
            return erros;
        }

        public String getTabelaSLR() {
            return tabelaSLR;
        }

        public TabelaSLRData getTabelaData() {
            return tabelaData;
        }
    }

    // ========================== Tokenizador MaoMao ==========================
    public static List<Token> tokenizar(String entrada) {
        List<Token> tokens = new ArrayList<>();

        // Ordem de captura:
        // 1) 🐈‍⬛ (\uD83D\uDC08\u200D\u2B1B) antes de 🐈 (\uD83D\uDC08)
        // 2) Operadores relacionais compostos (>=w=, <=w=, !w!) antes dos outros
        // 3) Variáveis MaoMao: Mao_ id _Mao
        Pattern pattern = Pattern.compile(
                "\\s+|" + // Espaços
                        "(rrr-rrr)|" + // 1: WHILE
                        "(\\.\\.\\.\\.\\.)|" + // 2: PAREN (.....)
                        "(\\uD83D\\uDC08\\u200D\\u2B1B|🐈‍⬛)|" + // 3: FECHA_BLOCO
                        "(\\uD83D\\uDC08|🐈)|" + // 4: ABRE_BLOCO
                        "(Mao_\\s*[a-zA-Z][a-zA-Z0-9_\\-?!]*\\s*_Mao)|" + // 5: VAR
                        "(Pspsps)|" + // 6: ASSIGN
                        "(!)|" + // 7: FIM (!)
                        "(\\+w\\+)|" + // 8: PLUS (+w+)
                        "(\\*w\\*)|" + // 9: TIMES (*w*)
                        "(>=w=|<=w=|!w!|>wo|ow<|=w=)|" + // 10: RELOP
                        "(\\d+)|" + // 11: NUM
                        "([a-zA-Z0-9_]+)|" + // 12: Palavras soltas
                        "(.)" // 13: Qualquer outro símbolo
        );

        Matcher matcher = pattern.matcher(entrada);
        while (matcher.find()) {
            if (matcher.group().trim().isEmpty())
                continue;

            if (matcher.group(1) != null) {
                tokens.add(new Token(T_WHILE, matcher.group(1)));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token(T_PAREN, matcher.group(2)));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token(T_FECHA_BLOCO, "🐈‍⬛"));
            } else if (matcher.group(4) != null) {
                tokens.add(new Token(T_ABRE_BLOCO, "🐈"));
            } else if (matcher.group(5) != null) {
                tokens.add(new Token(T_VAR, matcher.group(5).replaceAll("\\s+", " ")));
            } else if (matcher.group(6) != null) {
                tokens.add(new Token(T_ASSIGN, matcher.group(6)));
            } else if (matcher.group(7) != null) {
                tokens.add(new Token(T_FIM, "!"));
            } else if (matcher.group(8) != null) {
                tokens.add(new Token(T_PLUS, "+w+"));
            } else if (matcher.group(9) != null) {
                tokens.add(new Token(T_TIMES, "*w*"));
            } else if (matcher.group(10) != null) {
                tokens.add(new Token(T_RELOP, matcher.group(10)));
            } else if (matcher.group(11) != null) {
                tokens.add(new Token(T_NUM, matcher.group(11)));
            } else {
                tokens.add(new Token(-1, matcher.group()));
            }
        }
        return tokens;
    }

    // ========================== Análise Bottom-Up (SLR) ==========================
    public Resultado analisar(String entrada) {
        Resultado resultado = new Resultado();

        List<Token> tokens = tokenizar(entrada);

        // Checagem de erros léxicos
        for (Token t : tokens) {
            if (t.tipo == -1) {
                resultado.erros.add("Caractere ou token não reconhecido na Linguagem MaoMao: '" + t.valor + "'");
            }
        }
        if (!resultado.erros.isEmpty()) {
            resultado.aceito = false;
            resultado.passos.add("❌ Análise abortada: erros léxicos na entrada.");
            resultado.tabelaSLR = gerarTabelaSLR();
            return resultado;
        }

        tokens.add(new Token(T_DOLLAR, "$"));

        Deque<Integer> pilhaEstados = new ArrayDeque<>();
        Deque<String> pilhaSimbolos = new ArrayDeque<>();

        pilhaEstados.push(0);
        pilhaSimbolos.push("$");

        int pos = 0;
        int passo = 1;

        resultado.passos.add(String.format(
                "%-6s | %-40s | %-40s | %s",
                "Passo", "Pilha", "Entrada", "Ação"));
        resultado.passos.add("-".repeat(120));

        while (true) {
            int estadoAtual = pilhaEstados.peek();
            Token tokenAtual = tokens.get(pos);
            int tipoToken = tokenAtual.tipo;

            String pilhaStr = montarPilha(pilhaEstados, pilhaSimbolos);
            String entradaStr = montarEntrada(tokens, pos);

            if (tipoToken < 0 || tipoToken >= NUM_TERMINAIS) {
                String acaoStr = "ERRO: token inválido '" + tokenAtual.valor + "'";
                resultado.passos.add(String.format(
                        "%-6d | %-40s | %-40s | %s",
                        passo, pilhaStr, entradaStr, acaoStr));
                resultado.passosDetalhados.add(new PassoInfo(passo, pilhaStr, entradaStr, acaoStr, "error"));
                resultado.aceito = false;
                resultado.erros.add("Token inválido: '" + tokenAtual.valor + "'");
                break;
            }

            int acao = ACTION[estadoAtual][tipoToken];

            if (acao == ACEITAR) {
                String acaoStr = "Aceitar (Sucesso)";
                resultado.passos.add(String.format(
                        "%-6d | %-40s | %-40s | ✅ ACEITAR",
                        passo, pilhaStr, entradaStr));
                resultado.passosDetalhados.add(new PassoInfo(passo, pilhaStr, entradaStr, acaoStr, "accept"));
                resultado.aceito = true;
                break;

            } else if (acao > 0) {
                // SHIFT
                String acaoStr = "Carrega " + acao;
                resultado.passos.add(String.format(
                        "%-6d | %-40s | %-40s | %s",
                        passo, pilhaStr, entradaStr, acaoStr));
                resultado.passosDetalhados.add(new PassoInfo(passo, pilhaStr, entradaStr, acaoStr, "shift"));
                pilhaEstados.push(acao);
                pilhaSimbolos.push(tokenAtual.valor);
                pos++;

            } else if (acao < 0) {
                // REDUCE
                int numProducao = -acao;
                int[] producao = PRODUCOES[numProducao];
                int ladoEsquerdo = producao[0];
                int tamanho = producao[1];

                String acaoStr = "Reduz " + numProducao + ": " + NOMES_PRODUCOES[numProducao];
                resultado.passos.add(String.format(
                        "%-6d | %-40s | %-40s | %s",
                        passo, pilhaStr, entradaStr, acaoStr));
                resultado.passosDetalhados.add(new PassoInfo(passo, pilhaStr, entradaStr, acaoStr, "reduce"));

                for (int i = 0; i < tamanho; i++) {
                    pilhaEstados.pop();
                    pilhaSimbolos.pop();
                }

                int estadoTopo = pilhaEstados.peek();
                String nomeNT = NOMES_NAO_TERMINAIS[ladoEsquerdo];
                pilhaSimbolos.push(nomeNT);

                int novoEstado = GOTO[estadoTopo][ladoEsquerdo];
                if (novoEstado < 0) {
                    resultado.erros.add("Erro na tabela GOTO: estado " + estadoTopo +
                            ", não-terminal " + nomeNT);
                    resultado.aceito = false;
                    break;
                }
                pilhaEstados.push(novoEstado);

            } else {
                // ERRO
                String acaoStr = "Erro sintático";
                resultado.passos.add(String.format(
                        "%-6d | %-40s | %-40s | ❌ ERRO",
                        passo, pilhaStr, entradaStr));
                resultado.passosDetalhados.add(new PassoInfo(passo, pilhaStr, entradaStr, acaoStr, "error"));

                List<String> esperados = new ArrayList<>();
                for (int t = 0; t < NUM_TERMINAIS; t++) {
                    if (ACTION[estadoAtual][t] != ERRO) {
                        esperados.add(NOMES_TERMINAIS[t]);
                    }
                }
                String msg = "Erro sintático no token '" + tokenAtual.valor + "'. ";
                if (!esperados.isEmpty()) {
                    msg += "Esperado: " + String.join(", ", esperados);
                }
                resultado.erros.add(msg);
                resultado.aceito = false;
                break;
            }

            passo++;
        }

        resultado.tabelaSLR = gerarTabelaSLR();
        resultado.tabelaData = getTabelaData();
        return resultado;
    }

    private String montarPilha(Deque<Integer> pilhaEstados, Deque<String> pilhaSimbolos) {
        List<Integer> estados = new ArrayList<>(pilhaEstados);
        List<String> simbolos = new ArrayList<>(pilhaSimbolos);
        Collections.reverse(estados);
        Collections.reverse(simbolos);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < estados.size(); i++) {
            if (i < simbolos.size())
                sb.append(simbolos.get(i)).append(" ");
            sb.append(estados.get(i)).append(" ");
        }
        return sb.toString().trim();
    }

    private String montarEntrada(List<Token> tokens, int pos) {
        StringBuilder sb = new StringBuilder();
        for (int i = pos; i < tokens.size(); i++) {
            if (i > pos)
                sb.append(" ");
            sb.append(tokens.get(i).valor);
        }
        return sb.toString();
    }

    // ========================== Geração da Tabela SLR em Texto
    // ==========================
    public String gerarTabelaSLR() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-7s", "Estado"));
        sb.append("| ");
        for (String nome : NOMES_TERMINAIS) {
            sb.append(String.format("%-10s", nome));
        }
        sb.append("| ");
        for (String nome : NOMES_NAO_TERMINAIS) {
            sb.append(String.format("%-9s", nome));
        }
        sb.append("\n");
        sb.append("-".repeat(sb.length())).append("\n");

        for (int i = 0; i < NUM_ESTADOS; i++) {
            sb.append(String.format("%-7d", i));
            sb.append("| ");
            for (int j = 0; j < NUM_TERMINAIS; j++) {
                int val = ACTION[i][j];
                String cell;
                if (val == ACEITAR) {
                    cell = "acc";
                } else if (val > 0) {
                    cell = "s" + val;
                } else if (val < 0) {
                    cell = "r" + (-val);
                } else {
                    cell = "";
                }
                sb.append(String.format("%-10s", cell));
            }
            sb.append("| ");
            for (int j = 0; j < NUM_NAO_TERMINAIS; j++) {
                int val = GOTO[i][j];
                String cell = val >= 0 ? String.valueOf(val) : "";
                sb.append(String.format("%-9s", cell));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ========================== Testes de Linha de Comando
    // ==========================
    public static void main(String[] args) {
        BottomUpParser parser = new BottomUpParser();

        System.out.println("=== TABELA SLR (LAÇO MAOMAO) ===");
        System.out.println(parser.gerarTabelaSLR());

        String valida1 = "rrr-rrr ..... Mao_ i _Mao ow< 10 ..... 🐈 Mao_ i _Mao Pspsps Mao_ i _Mao +w+ 1! 🐈‍⬛";
        String valida2 = "rrr-rrr ..... Mao_ x _Mao >wo 0 ..... 🐈 Mao_ soma _Mao Pspsps Mao_ soma _Mao +w+ Mao_ x _Mao *w* 2! Mao_ x _Mao Pspsps Mao_ x _Mao +w+ 1! 🐈‍⬛";

        String invalida1 = "rrr-rrr ..... Mao_ i _Mao ow< 10 ..... 🐈 Mao_ i _Mao Pspsps +w+ 1! 🐈‍⬛";
        String invalida2 = "rrr-rrr ..... Mao_ i _Mao ow< 10 🐈 Mao_ i _Mao Pspsps 1! 🐈‍⬛";
        String invalida3 = "rrr-rrr ..... Mao_ i _Mao ow< 10 ..... 🐈 Mao_ i _Mao Pspsps 1 🐈‍⬛";

        System.out.println("\n>>> TESTANDO ENTRADA VÁLIDA 1:");
        System.out.println("Entrada: " + valida1);
        Resultado r1 = parser.analisar(valida1);
        r1.passos.forEach(System.out::println);
        System.out.println("Resultado: " + (r1.aceito ? "ACEITO" : "❌ REJEITADO"));

        System.out.println("\n>>> TESTANDO ENTRADA VÁLIDA 2 (2 comandos):");
        System.out.println("Entrada: " + valida2);
        Resultado r2 = parser.analisar(valida2);
        r2.passos.forEach(System.out::println);
        System.out.println("Resultado: " + (r2.aceito ? "ACEITO" : "❌ REJEITADO"));

        System.out.println("\n>>> TESTANDO ENTRADA INVÁLIDA 1 (expressão incompleta):");
        System.out.println("Entrada: " + invalida1);
        Resultado r3 = parser.analisar(invalida1);
        r3.passos.forEach(System.out::println);
        System.out.println("Resultado: " + (r3.aceito ? "ACEITO" : "❌ REJEITADO"));
        r3.erros.forEach(e -> System.out.println("  ⚠️ " + e));

        System.out.println("\n>>> TESTANDO ENTRADA INVÁLIDA 2 (sem fechar parentese .....):");
        System.out.println("Entrada: " + invalida2);
        Resultado r4 = parser.analisar(invalida2);
        r4.passos.forEach(System.out::println);
        System.out.println("Resultado: " + (r4.aceito ? "ACEITO" : "❌ REJEITADO"));
        r4.erros.forEach(e -> System.out.println("  ⚠️ " + e));

        System.out.println("\n>>> TESTANDO ENTRADA INVÁLIDA 3 (sem ! de fim):");
        System.out.println("Entrada: " + invalida3);
        Resultado r5 = parser.analisar(invalida3);
        r5.passos.forEach(System.out::println);
        System.out.println("Resultado: " + (r5.aceito ? "ACEITO" : "❌ REJEITADO"));
        r5.erros.forEach(e -> System.out.println("  ⚠️ " + e));
    }
}
