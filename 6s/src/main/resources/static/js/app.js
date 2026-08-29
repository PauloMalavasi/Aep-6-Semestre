const API_URL = "/ponto_coleta";

const residueLabels = {
    ELETRONICO: "Eletrônico",
    PILHA: "Pilha",
    BATERIA: "Bateria",
    PAPEL: "Papel",
    PLASTICO: "Plástico",
    VIDRO: "Vidro",
    METAL: "Metal",
    ORGANICO: "Orgânico"
};

const state = {
    pontos: [],
    editandoId: null,
    excluindoId: null,
    buscaAtiva: false
};

const elements = {
    grid: document.querySelector("#points-grid"),
    empty: document.querySelector("#empty-state"),
    resultLabel: document.querySelector("#result-label"),
    totalPoints: document.querySelector("#total-points"),
    totalResidues: document.querySelector("#total-residues"),
    searchForm: document.querySelector("#search-form"),
    searchInput: document.querySelector("#search-input"),
    searchType: document.querySelector("#search-type"),
    clearSearch: document.querySelector("#clear-search"),
    refresh: document.querySelector("#refresh-list"),
    pointDialog: document.querySelector("#point-dialog"),
    pointForm: document.querySelector("#point-form"),
    dialogTitle: document.querySelector("#dialog-title"),
    submitForm: document.querySelector("#submit-form"),
    residueError: document.querySelector("#residue-error"),
    deleteDialog: document.querySelector("#delete-dialog"),
    deleteName: document.querySelector("#delete-name"),
    confirmDelete: document.querySelector("#confirm-delete"),
    toast: document.querySelector("#toast")
};

document.querySelectorAll("#open-create, #hero-create, #empty-create").forEach(button => {
    button.addEventListener("click", abrirCadastro);
});

document.querySelector("#close-dialog").addEventListener("click", fecharFormulario);
document.querySelector("#cancel-form").addEventListener("click", fecharFormulario);
document.querySelector("#cancel-delete").addEventListener("click", () => elements.deleteDialog.close());
elements.confirmDelete.addEventListener("click", confirmarExclusao);
elements.pointForm.addEventListener("submit", salvarPonto);
elements.searchForm.addEventListener("submit", buscarPontos);
elements.clearSearch.addEventListener("click", limparBusca);
elements.refresh.addEventListener("click", () => carregarPontos());
elements.grid.addEventListener("click", tratarAcaoDoCard);

document.querySelector("#cep").addEventListener("input", event => {
    event.target.value = event.target.value.replace(/\D/g, "").replace(/(\d{5})(\d)/, "$1-$2").slice(0, 9);
});

document.querySelector("#telefone").addEventListener("input", event => {
    const value = event.target.value.replace(/\D/g, "").slice(0, 11);
    if (value.length <= 10) {
        event.target.value = value.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3").replace(/-$/, "");
    } else {
        event.target.value = value.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3").replace(/-$/, "");
    }
});

document.querySelector("#uf").addEventListener("input", event => {
    event.target.value = event.target.value.replace(/[^a-zA-Z]/g, "").toUpperCase().slice(0, 2);
});

elements.pointDialog.addEventListener("click", event => {
    if (event.target === elements.pointDialog) fecharFormulario();
});

elements.deleteDialog.addEventListener("click", event => {
    if (event.target === elements.deleteDialog) elements.deleteDialog.close();
});

carregarPontos();

async function carregarPontos(url = API_URL, mensagem = null) {
    mostrarCarregamento();

    try {
        const pontos = await requisicao(url);
        state.pontos = Array.isArray(pontos) ? pontos : [];
        renderizarPontos(mensagem);
    } catch (error) {
        state.pontos = [];
        renderizarPontos();
        elements.resultLabel.textContent = "Não foi possível acessar a API.";
        mostrarToast("Não foi possível carregar os pontos. Confira se o servidor e o MongoDB estão ativos.", true);
    }
}

function mostrarCarregamento() {
    elements.grid.setAttribute("aria-busy", "true");
    elements.empty.hidden = true;
    elements.grid.innerHTML = '<div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>';
    elements.resultLabel.textContent = "Carregando pontos de coleta...";
}

function renderizarPontos(mensagem = null) {
    elements.grid.setAttribute("aria-busy", "false");
    elements.grid.innerHTML = "";

    const residuosUnicos = new Set(
        state.pontos.flatMap(ponto => ponto.residuos || []).map(item => String(item).toUpperCase())
    );

    elements.totalPoints.textContent = state.pontos.length;
    elements.totalResidues.textContent = residuosUnicos.size;
    elements.resultLabel.textContent = mensagem || `${state.pontos.length} ${state.pontos.length === 1 ? "local encontrado" : "locais encontrados"}`;

    if (!state.pontos.length) {
        elements.empty.hidden = false;
        return;
    }

    elements.empty.hidden = true;
    elements.grid.innerHTML = state.pontos.map(criarCard).join("");
}

function criarCard(ponto) {
    const endereco = ponto.endereco || {};
    const enderecoLinha = [endereco.logradouro, endereco.numero].filter(Boolean).join(", ");
    const localidade = [endereco.bairro, endereco.cidade, endereco.uf].filter(Boolean).join(" · ");
    const residuos = (ponto.residuos || []).map(residuo => {
        const codigo = String(residuo).toUpperCase();
        return `<span class="residue-tag">${escapeHtml(residueLabels[codigo] || codigo)}</span>`;
    }).join("");

    return `
        <article class="point-card">
            <div class="card-top">
                <div class="location-mark" aria-hidden="true"><span>•</span></div>
                <div class="card-menu">
                    <button class="icon-button edit" type="button" data-action="edit" data-id="${escapeHtml(ponto.id)}" aria-label="Editar ${escapeHtml(ponto.nome)}">✎</button>
                    <button class="icon-button delete" type="button" data-action="delete" data-id="${escapeHtml(ponto.id)}" aria-label="Excluir ${escapeHtml(ponto.nome)}">×</button>
                </div>
            </div>
            <h3>${escapeHtml(ponto.nome || "Ponto sem nome")}</h3>
            <p class="address"><span aria-hidden="true">⌖</span><span>${escapeHtml(enderecoLinha || "Endereço não informado")}${localidade ? `<br>${escapeHtml(localidade)}` : ""}</span></p>
            <p class="phone"><span aria-hidden="true">◌</span><span>${escapeHtml(ponto.telefone || "Telefone não informado")}</span></p>
            <div class="residue-tags">${residuos || '<span class="residue-tag">Sem resíduos informados</span>'}</div>
        </article>`;
}

async function buscarPontos(event) {
    event.preventDefault();
    const termo = elements.searchInput.value.trim();

    if (!termo) {
        elements.searchInput.focus();
        mostrarToast("Digite um nome ou resíduo para buscar.", true);
        return;
    }

    state.buscaAtiva = true;
    elements.clearSearch.hidden = false;
    const tipo = elements.searchType.value;
    const endpoint = tipo === "residuo" ? "/nome-residuo" : "/nome-ponto";
    const parametro = encodeURIComponent(termo);
    await carregarPontos(`${API_URL}${endpoint}?nome=${parametro}`, `Resultados para “${termo}”`);
}

async function limparBusca() {
    elements.searchForm.reset();
    elements.clearSearch.hidden = true;
    state.buscaAtiva = false;
    await carregarPontos();
}

function abrirCadastro() {
    state.editandoId = null;
    elements.pointForm.reset();
    elements.residueError.hidden = true;
    elements.dialogTitle.textContent = "Cadastrar novo ponto";
    elements.submitForm.textContent = "Salvar ponto";
    elements.pointDialog.showModal();
    document.querySelector("#nome").focus();
}

function abrirEdicao(ponto) {
    state.editandoId = ponto.id;
    elements.pointForm.reset();
    elements.residueError.hidden = true;
    elements.dialogTitle.textContent = "Editar ponto de coleta";
    elements.submitForm.textContent = "Salvar alterações";

    const endereco = ponto.endereco || {};
    preencher("nome", ponto.nome);
    preencher("telefone", ponto.telefone);
    preencher("logradouro", endereco.logradouro);
    preencher("numero", endereco.numero);
    preencher("bairro", endereco.bairro);
    preencher("cep", endereco.cep);
    preencher("cidade", endereco.cidade);
    preencher("uf", endereco.uf);

    const selecionados = new Set((ponto.residuos || []).map(item => String(item).toUpperCase()));
    elements.pointForm.querySelectorAll('input[name="residuos"]').forEach(input => {
        input.checked = selecionados.has(input.value);
    });

    elements.pointDialog.showModal();
    document.querySelector("#nome").focus();
}

function fecharFormulario() {
    elements.pointDialog.close();
    elements.pointForm.reset();
    state.editandoId = null;
}

async function salvarPonto(event) {
    event.preventDefault();
    const residuos = [...elements.pointForm.querySelectorAll('input[name="residuos"]:checked')]
        .map(input => input.value);

    if (!residuos.length) {
        elements.residueError.hidden = false;
        elements.pointForm.querySelector('input[name="residuos"]').focus();
        return;
    }

    elements.residueError.hidden = true;
    const payload = {
        nome: valor("nome"),
        telefone: valor("telefone"),
        endereco: {
            logradouro: valor("logradouro"),
            numero: valor("numero"),
            bairro: valor("bairro"),
            cep: valor("cep"),
            cidade: valor("cidade"),
            uf: valor("uf").toUpperCase()
        },
        residuos
    };

    const editando = Boolean(state.editandoId);
    const url = editando ? `${API_URL}/${encodeURIComponent(state.editandoId)}` : API_URL;
    elements.submitForm.disabled = true;
    elements.submitForm.textContent = editando ? "Salvando..." : "Cadastrando...";

    try {
        await requisicao(url, {
            method: editando ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        fecharFormulario();
        mostrarToast(editando ? "Ponto atualizado com sucesso." : "Ponto cadastrado com sucesso.");
        await carregarPontos();
    } catch (error) {
        mostrarToast(error.message || "Não foi possível salvar o ponto.", true);
    } finally {
        elements.submitForm.disabled = false;
        elements.submitForm.textContent = state.editandoId ? "Salvar alterações" : "Salvar ponto";
    }
}

function tratarAcaoDoCard(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;

    const ponto = state.pontos.find(item => item.id === button.dataset.id);
    if (!ponto) return;

    if (button.dataset.action === "edit") {
        abrirEdicao(ponto);
        return;
    }

    state.excluindoId = ponto.id;
    elements.deleteName.textContent = ponto.nome;
    elements.deleteDialog.showModal();
}

async function confirmarExclusao() {
    if (!state.excluindoId) return;
    elements.confirmDelete.disabled = true;
    elements.confirmDelete.textContent = "Excluindo...";

    try {
        await requisicao(`${API_URL}/${encodeURIComponent(state.excluindoId)}`, { method: "DELETE" });
        elements.deleteDialog.close();
        mostrarToast("Ponto excluído com sucesso.");
        state.excluindoId = null;
        await carregarPontos();
    } catch (error) {
        mostrarToast(error.message || "Não foi possível excluir o ponto.", true);
    } finally {
        elements.confirmDelete.disabled = false;
        elements.confirmDelete.textContent = "Excluir ponto";
    }
}

async function requisicao(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        let detail = "A API retornou um erro.";
        try {
            const body = await response.json();
            detail = body.message || body.error || detail;
        } catch (_) {
            // A resposta pode não possuir um corpo JSON.
        }
        throw new Error(detail);
    }

    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function preencher(id, value) {
    document.querySelector(`#${id}`).value = value || "";
}

function valor(id) {
    return document.querySelector(`#${id}`).value.trim();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

let toastTimer;
function mostrarToast(message, error = false) {
    clearTimeout(toastTimer);
    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", error);
    elements.toast.classList.add("show");
    toastTimer = setTimeout(() => elements.toast.classList.remove("show"), 4200);
}
