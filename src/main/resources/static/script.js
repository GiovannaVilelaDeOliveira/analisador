// ===== DOM Elements =====
const codeEditor = document.getElementById("codeEditor");
const analyzeBtn = document.getElementById("analyzeBtn");
const clearBtn = document.getElementById("clearBtn");
const uploadZone = document.getElementById("uploadZone");
const fileInput = document.getElementById("fileInput");
const fileName = document.getElementById("fileName");
const resultsPlaceholder = document.getElementById("resultsPlaceholder");
const resultsContent = document.getElementById("resultsContent");
const errorMessage = document.getElementById("errorMessage");
const errorText = document.getElementById("errorText");
const analizeTableBody = document.getElementById("analizeTableBody");
const tokensTableBody = document.getElementById("tokensTableBody");
const totalTokens = document.getElementById("totalTokens");
const uniqueTokens = document.getElementById("uniqueTokens");
const lineNumbers = document.getElementById("lineNumbers");

// ===== Numeração de linhas =====
function updateLineNumbers() {
  const total = codeEditor.value.split("\n").length;
  const current = codeEditor.value
    .slice(0, codeEditor.selectionStart)
    .split("\n").length;
  let html = "";
  for (let i = 1; i <= total; i++) {
    html +=
      i === current ? `<div class="active-line">${i}</div>` : `<div>${i}</div>`;
  }
  lineNumbers.innerHTML = html;
  lineNumbers.scrollTop = codeEditor.scrollTop;
}

codeEditor.addEventListener("input", updateLineNumbers);
codeEditor.addEventListener("keyup", updateLineNumbers);
codeEditor.addEventListener("click", updateLineNumbers);
codeEditor.addEventListener("scroll", () => {
  lineNumbers.scrollTop = codeEditor.scrollTop;
});
new ResizeObserver(() => {
  lineNumbers.scrollTop = codeEditor.scrollTop;
}).observe(codeEditor);

const DEFAULT_CODE = `ronron [w] Principal 🐈
    ronron raaaaah 0w0 miau ..... ..... 🐈

    Mao_ a 💊Frajola _Mao Pspsps 5!
    Mao_ b 💊Frajola _Mao Pspsps 7!

    Mao_ soma 💊Frajola _Mao Pspsps Mao_ a _Mao +w+ Mao_ b _Mao!

    Ptooey "A soma e:"!
    Ptooey Mao_ soma _Mao!

    meow ..... Mao_ soma _Mao >wo 10 ..... 🐈
        Ptooey "Maior que 10"!
    🐈‍⬛ grrrrr 🐈
        Ptooey "Menor ou igual a 10"!
    🐈‍⬛
🐈‍⬛
🐈‍⬛`;

document.addEventListener("DOMContentLoaded", () => {
  codeEditor.value = DEFAULT_CODE;
  updateLineNumbers();
  errorMessage.hide();
});

analyzeBtn.addEventListener("click", async () => {
  const codigo = codeEditor.value.trim();
  if (!codigo) {
    showError("Digite ou carregue um código para analisar! 🐱");
    return;
  }

  hideError();
  analyzeBtn.loading = true;

  try {
    const response = await fetch("/api/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ codigo }),
    });

    const data = await response.json();

    if (!response.ok || data.error) {
      showError(data.error || "Erro desconhecido na análise.");
      return;
    }

    renderResults(data);
  } catch (err) {
    showError(
      "Erro de conexão com o servidor. Verifique se a aplicação está rodando.",
    );
  } finally {
    analyzeBtn.loading = false;
  }
});

clearBtn.addEventListener("click", () => {
  codeEditor.value = "";
  updateLineNumbers();
  hideResults();
  hideError();
  fileName.classList.remove("visible");
  fileName.textContent = "";
  codeEditor.focus();
});

uploadZone.addEventListener("click", () => fileInput.click());

uploadZone.addEventListener("dragover", (e) => {
  e.preventDefault();
  uploadZone.classList.add("drag-over");
});

uploadZone.addEventListener("dragleave", () => {
  uploadZone.classList.remove("drag-over");
});

uploadZone.addEventListener("drop", (e) => {
  e.preventDefault();
  uploadZone.classList.remove("drag-over");
  const file = e.dataTransfer.files[0];
  if (file) handleFile(file);
});

fileInput.addEventListener("change", (e) => {
  const file = e.target.files[0];
  if (file) handleFile(file);
  fileInput.value = "";
});

function handleFile(file) {
  if (!file.name.endsWith(".maomao")) {
    showError("Apenas arquivos com extensão .maomao são aceitos! 🐱");
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    codeEditor.value = e.target.result;
    updateLineNumbers();
    fileName.textContent = `📄 ${file.name}`;
    fileName.classList.add("visible");
    hideError();
  };
  reader.onerror = () => {
    showError("Erro ao ler o arquivo.");
  };
  reader.readAsText(file);
}

function renderResults(data) {
  let errorCount = 0;
  if (data.analize) {
    errorCount = data.analize.filter((item) => item.includes("ERRO")).length;
  }

  totalTokens.textContent = data.analize ? data.analize.length : 0;
  uniqueTokens.textContent = data.tokens ? data.tokens.length : 0;

  const errorsChip = document.getElementById("errorCount");
  const errorsValue = document.getElementById("errorCountValue");
  if (errorCount > 0) {
    errorsValue.textContent = errorCount;
    errorsChip.classList.remove("hidden");
  } else {
    errorsChip.classList.add("hidden");
  }

  analizeTableBody.innerHTML = "";
  if (data.analize) {
    data.analize.forEach((item, idx) => {
      const parts = item.split(" - ");
      const lexema = parts[0] || "";
      const desc = parts.slice(1).join(" - ") || "";
      const isError = desc.startsWith("ERRO");

      const tr = document.createElement("tr");
      if (isError) tr.classList.add("row-error");
      tr.innerHTML = `
                <td class="token-index">${idx + 1}</td>
                <td class="token-lexema${isError ? " error-lexema" : ""}">${isError ? "⚠️ " : ""}${escapeHtml(lexema)}</td>
                <td class="token-desc${isError ? " error-desc" : ""}">${escapeHtml(desc)}</td>
            `;
      analizeTableBody.appendChild(tr);
    });
  }

  tokensTableBody.innerHTML = "";
  if (data.tokens) {
    data.tokens.sort((a, b) => b.quantidade - a.quantidade);

    data.tokens.forEach((token, idx) => {
      const isError = token.descricao && token.descricao.startsWith("ERRO");
      const tr = document.createElement("tr");
      if (isError) tr.classList.add("row-error");
      tr.innerHTML = `
                <td class="token-lexema${isError ? " error-lexema" : ""}">${isError ? "⚠️ " : ""}${escapeHtml(token.lexema)}</td>
                <td class="token-desc${isError ? " error-desc" : ""}">${escapeHtml(token.descricao)}</td>
                <td class="token-qty"><span class="qty-badge${isError ? " qty-badge-error" : ""}">${token.quantidade}</span></td>
            `;
      tokensTableBody.appendChild(tr);
    });
  }

  renderErrorPanel({
    prefix: "lexical",
    erros: data.errosLexicos || [],
    okText: "Léxico válido 😺",
    failText: (n) => `${n} erro(s) léxico(s) 🙀`,
  });
  renderErrorPanel({
    prefix: "syntax",
    erros: data.errosSintaticos || [],
    okText: "Sintaxe válida 😺",
    failText: (n) => `${n} erro(s) de sintaxe 🙀`,
  });
  renderErrorPanel({
    prefix: "semantic",
    erros: data.errosSemanticos || [],
    okText: "Semântica válida 😺",
    failText: (n) => `${n} erro(s) de semântica 🙀`,
  });

  resultsPlaceholder.style.display = "none";
  resultsContent.classList.add("visible");
}

function renderErrorPanel({ prefix, erros, okText, failText }) {
  const ok = erros.length === 0;
  const count = document.getElementById(`${prefix}Count`);
  const body = document.getElementById(`${prefix}TableBody`);

  count.textContent = erros.length;
  count.classList.toggle("has-errors", !ok);

  body.innerHTML = "";
  if (ok) {
    body.innerHTML =
      '<tr><td colspan="2" class="token-desc">✅ Nenhum erro encontrado.</td></tr>';
    return;
  }
  erros.forEach((erro, idx) => {
    const tr = document.createElement("tr");
    tr.classList.add("row-error");
    tr.innerHTML = `
            <td class="token-index">${idx + 1}</td>
            <td class="token-desc error-desc">⚠️ ${escapeHtml(erro)}</td>
        `;
    body.appendChild(tr);
  });
}

// ===== Helpers =====
function showError(message) {
  errorText.textContent = message;
  errorMessage.show();
}

function hideError() {
  errorMessage.hide();
}

function hideResults() {
  resultsContent.classList.remove("visible");
  resultsPlaceholder.style.display = "flex";
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}


// ===== Bottom-Up Parser =====
const buCodeEditor = document.getElementById("buCodeEditor");
const buAnalyzeBtn = document.getElementById("buAnalyzeBtn");
const buClearBtn = document.getElementById("buClearBtn");
const buResultsPlaceholder = document.getElementById("buResultsPlaceholder");
const buResultsContent = document.getElementById("buResultsContent");
const buErrorMessage = document.getElementById("buErrorMessage");
const buErrorText = document.getElementById("buErrorText");
const buPassosTableBody = document.getElementById("buPassosTableBody");
const buTabelaSubheader = document.getElementById("buTabelaSubheader");
const buTabelaTableBody = document.getElementById("buTabelaTableBody");
const buErrosTableBody = document.getElementById("buErrosTableBody");
const buResultBadge = document.getElementById("buResultBadge");
const buResultText = document.getElementById("buResultText");
const buErrorCount = document.getElementById("buErrorCount");

function safeShowAlert(el, textEl, msg) {
  if (textEl) textEl.textContent = msg;
  if (!el) return;
  try {
    if (typeof el.show === "function") {
      el.show();
    } else {
      el.setAttribute("open", "");
    }
  } catch (e) {
    el.setAttribute("open", "");
  }
}

function safeHideAlert(el) {
  if (!el) return;
  try {
    if (typeof el.hide === "function") {
      el.hide();
    } else {
      el.removeAttribute("open");
    }
  } catch (e) {
    el.removeAttribute("open");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  safeHideAlert(buErrorMessage);
  carregarTabelaSLR();
});

async function carregarTabelaSLR() {
  try {
    const res = await fetch("/api/bottomup/tabela");
    if (res.ok) {
      const data = await res.json();
      renderBUTabela(data);
    }
  } catch (e) {
    console.warn("Não foi possível pré-carregar a tabela SLR:", e);
  }
}

buAnalyzeBtn.addEventListener("click", async () => {
  const entrada = buCodeEditor ? buCodeEditor.value.trim() : "";
  if (!entrada) {
    safeShowAlert(buErrorMessage, buErrorText, "Digite o código do laço para analisar! ⬆️");
    return;
  }

  safeHideAlert(buErrorMessage);
  buAnalyzeBtn.loading = true;
  console.log("[BottomUp] Iniciando análise:", entrada);

  try {
    const response = await fetch("/api/bottomup", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ entrada }),
    });

    const data = await response.json();
    console.log("[BottomUp] Resposta recebida:", data);

    if (!response.ok && !data.passosDetalhados && !data.passos) {
      const msg = data.erros && data.erros.length > 0 ? data.erros.join("; ") : (data.error || "Erro desconhecido na análise.");
      safeShowAlert(buErrorMessage, buErrorText, msg);
      return;
    }

    renderBUResults(data);
  } catch (err) {
    console.error("[BottomUp] Erro na requisição:", err);
    safeShowAlert(buErrorMessage, buErrorText, "Erro ao conectar com o servidor. Verifique se o backend Spring Boot está ativo na porta 8080.");
  } finally {
    buAnalyzeBtn.loading = false;
  }
});

buClearBtn.addEventListener("click", () => {
  if (buCodeEditor) buCodeEditor.value = "";
  if (buResultsContent) {
    buResultsContent.classList.remove("visible");
    buResultsContent.style.display = "none";
  }
  if (buResultsPlaceholder) buResultsPlaceholder.style.display = "flex";
  safeHideAlert(buErrorMessage);
  if (buCodeEditor) buCodeEditor.focus();
});

function renderBUResults(data) {
  // 1. Badge de Resultado (Aceito ou Rejeitado)
  if (buResultBadge && buResultText) {
    if (data.aceito) {
      buResultBadge.variant = "success";
      buResultText.textContent = "ACEITO ✅";
    } else {
      buResultBadge.variant = "danger";
      buResultText.textContent = "REJEITADO ❌";
    }
  }

  // 2. Tabela de Passos da Análise (HTML Real)
  renderBUPassos(data.passosDetalhados, data.passos);

  // 3. Tabela SLR (HTML Real)
  if (data.tabelaData) {
    renderBUTabela(data.tabelaData);
  }

  // 4. Tabela de Erros Sintáticos
  const erros = data.erros || [];
  if (buErrorCount) {
    buErrorCount.textContent = erros.length;
    buErrorCount.classList.toggle("has-errors", erros.length > 0);
  }

  if (buErrosTableBody) {
    buErrosTableBody.innerHTML = "";
    if (erros.length === 0) {
      buErrosTableBody.innerHTML =
        '<tr><td colspan="2" class="token-desc" style="padding: 1rem; color: var(--sl-color-success-600);">✅ Nenhum erro sintático encontrado. A estrutura do laço está válida!</td></tr>';
    } else {
      erros.forEach((erro, idx) => {
        const tr = document.createElement("tr");
        tr.classList.add("row-error");
        tr.innerHTML = `
          <td class="token-index" style="text-align: center;">${idx + 1}</td>
          <td class="token-desc error-desc">⚠️ ${escapeHtml(erro)}</td>
        `;
        buErrosTableBody.appendChild(tr);
      });
    }
  }

  // 5. Exibir o painel de resultados e ocultar o placeholder
  if (buResultsPlaceholder) buResultsPlaceholder.style.display = "none";
  if (buResultsContent) {
    buResultsContent.style.display = "block";
    buResultsContent.classList.add("visible");
  }

  // 6. Ativar aba de passos no Shoelace Tab Group
  const tabs = document.getElementById("buResultTabs");
  if (tabs && typeof tabs.show === "function") {
    setTimeout(() => {
      try { tabs.show("bu-passos"); } catch(e) {}
    }, 50);
  }
}

function renderBUPassos(passosDetalhados, passosTexto) {
  if (!buPassosTableBody) return;
  buPassosTableBody.innerHTML = "";

  // Se vier no formato de objetos detalhados:
  if (passosDetalhados && passosDetalhados.length > 0) {
    passosDetalhados.forEach((p) => {
      const tr = document.createElement("tr");
      if (p.tipoAcao === "error") {
        tr.classList.add("row-error");
      }

      let badgeClass = "badge-shift";
      if (p.tipoAcao === "reduce") badgeClass = "badge-reduce";
      else if (p.tipoAcao === "accept") badgeClass = "badge-accept";
      else if (p.tipoAcao === "error") badgeClass = "badge-error";

      tr.innerHTML = `
        <td class="token-index" style="text-align: center;">${p.passo}</td>
        <td class="step-stack">${escapeHtml(p.pilha)}</td>
        <td class="step-input">${escapeHtml(p.entrada)}</td>
        <td><span class="step-badge ${badgeClass}">${escapeHtml(p.acao)}</span></td>
      `;
      buPassosTableBody.appendChild(tr);
    });
    return;
  }

  // Fallback caso venha apenas como lista de strings:
  if (passosTexto && passosTexto.length > 2) {
    // Pula os headers (linhas 0 e 1)
    let idx = 1;
    for (let i = 2; i < passosTexto.length; i++) {
      const line = passosTexto[i];
      const parts = line.split("|").map(s => s.trim());
      if (parts.length >= 4) {
        const tr = document.createElement("tr");
        const acao = parts[3];
        let badgeClass = "badge-shift";
        if (acao.includes("Reduce")) badgeClass = "badge-reduce";
        else if (acao.includes("ACEITAR")) badgeClass = "badge-accept";
        else if (acao.includes("ERRO")) badgeClass = "badge-error";

        tr.innerHTML = `
          <td class="token-index" style="text-align: center;">${idx++}</td>
          <td class="step-stack">${escapeHtml(parts[1])}</td>
          <td class="step-input">${escapeHtml(parts[2])}</td>
          <td><span class="step-badge ${badgeClass}">${escapeHtml(acao)}</span></td>
        `;
        buPassosTableBody.appendChild(tr);
      }
    }
    return;
  }

  buPassosTableBody.innerHTML =
    '<tr><td colspan="4" style="text-align:center; padding: var(--sl-spacing-medium); color: var(--sl-color-neutral-400);">Nenhum passo registrado.</td></tr>';
}

function renderBUTabela(tabelaData) {
  if (!tabelaData || !tabelaData.linhas || !buTabelaSubheader || !buTabelaTableBody) return;

  // Montar subheader de colunas
  buTabelaSubheader.innerHTML = "";
  (tabelaData.terminais || []).forEach((t) => {
    const th = document.createElement("th");
    th.classList.add("th-terminal");
    th.textContent = t;
    buTabelaSubheader.appendChild(th);
  });
  (tabelaData.naoTerminais || []).forEach((nt) => {
    const th = document.createElement("th");
    th.classList.add("th-nao-terminal");
    th.textContent = nt;
    buTabelaSubheader.appendChild(th);
  });

  // Montar linhas dos estados
  buTabelaTableBody.innerHTML = "";
  tabelaData.linhas.forEach((linha) => {
    const tr = document.createElement("tr");

    // Estado
    const tdEstado = document.createElement("td");
    tdEstado.classList.add("cell-state");
    tdEstado.textContent = linha.estado;
    tr.appendChild(tdEstado);

    // ACTION
    (linha.actions || []).forEach((act) => {
      const td = document.createElement("td");
      if (act.startsWith("s")) {
        td.classList.add("cell-shift");
      } else if (act.startsWith("r")) {
        td.classList.add("cell-reduce");
      } else if (act === "acc") {
        td.classList.add("cell-acc");
      }
      td.textContent = act;
      tr.appendChild(td);
    });

    // GOTO
    (linha.gotos || []).forEach((gt) => {
      const td = document.createElement("td");
      if (gt) {
        td.classList.add("cell-goto");
      }
      td.textContent = gt;
      tr.appendChild(td);
    });

    buTabelaTableBody.appendChild(tr);
  });
}
