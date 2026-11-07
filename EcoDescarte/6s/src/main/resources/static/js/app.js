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
    organizacoes: [],
    carregamento: 0,
    editandoId: null,
    excluindoId: null,
    buscaAtiva: false,
    tipoBusca: null,
    termoBusca: "",
    erroCarregamento: false
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
    listAll: document.querySelector("#list-all"),
    clearSearch: document.querySelector("#clear-search"),
    emptyTitle: document.querySelector("#empty-title"),
    emptyMessage: document.querySelector("#empty-message"),
    refresh: document.querySelector("#refresh-list"),
    pointDialog: document.querySelector("#point-dialog"),
    pointForm: document.querySelector("#point-form"),
    dialogTitle: document.querySelector("#dialog-title"),
    submitForm: document.querySelector("#submit-form"),
    residueError: document.querySelector("#residue-error"),
    deleteDialog: document.querySelector("#delete-dialog"),
    deleteName: document.querySelector("#delete-name"),
    confirmDelete: document.querySelector("#confirm-delete"),
    toast: document.querySelector("#toast"),
    organizationSelect: document.querySelector("#organizacaoId"),
    organizationDialog: document.querySelector("#organization-dialog"),
    organizationForm: document.querySelector("#organization-form"),
    organizationSave: document.querySelector("#save-organization"),
    organizationError: document.querySelector("#organization-error")
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
elements.listAll.addEventListener("click", listarTodosOsPontos);
elements.refresh.addEventListener("click", listarTodosOsPontos);
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

elements.pointDialog.addEventListener("cancel", event => {
    if (elements.submitForm.disabled) event.preventDefault();
});

elements.deleteDialog.addEventListener("click", event => {
    if (event.target === elements.deleteDialog) elements.deleteDialog.close();
});

document.querySelector("#open-organization").addEventListener("click", () => {
    elements.organizationForm.reset();
    elements.organizationError.hidden = true;
    elements.organizationDialog.showModal();
    document.querySelector("#organization-name").focus();
});
document.querySelector("#cancel-organization").addEventListener("click", () => {
    if (!elements.organizationSave.disabled) elements.organizationDialog.close();
});
elements.organizationDialog.addEventListener("cancel", event => {
    if (elements.organizationSave.disabled) event.preventDefault();
});
elements.organizationForm.addEventListener("submit", salvarOrganizacao);

carregarPontos();

async function carregarPontos(url = API_URL, mensagem = null) {
    const carregamento = ++state.carregamento;
    mostrarCarregamento();
    state.erroCarregamento = false;

    try {
        const [pontos, organizacoes] = await Promise.all([requisicao(url), requisicao("/organizacoes")]);
        if (carregamento !== state.carregamento) return;
        state.organizacoes = Array.isArray(organizacoes) ? organizacoes : [];
        renderizarOrganizacoes();
        state.pontos = Array.isArray(pontos) ? pontos : [];
        renderizarPontos(mensagem);
    } catch (error) {
        if (carregamento !== state.carregamento) return;
        state.pontos = [];
        state.erroCarregamento = true;
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
        atualizarEstadoVazio();
        elements.empty.hidden = false;
        return;
    }

    elements.empty.hidden = true;
    elements.grid.innerHTML = state.pontos.map(criarCard).join("");
}

function atualizarEstadoVazio() {
    if (state.erroCarregamento) {
        elements.emptyTitle.textContent = "Não foi possível carregar os pontos";
        elements.emptyMessage.textContent = "Verifique se o servidor e o MongoDB estão ativos e tente novamente.";
        return;
    }

    if (state.buscaAtiva && state.tipoBusca === "residuo") {
        elements.resultLabel.textContent = `Resíduo “${state.termoBusca}” não disponível`;
        elements.emptyTitle.textContent = "Resíduo não disponível";
        elements.emptyMessage.textContent = `Nenhum ponto cadastrado recebe “${state.termoBusca}”. Tente outro resíduo ou veja todos os pontos.`;
        return;
    }

    if (state.buscaAtiva) {
        elements.emptyTitle.textContent = "Nenhum ponto encontrado";
        elements.emptyMessage.textContent = `Não encontramos um ponto com o nome “${state.termoBusca}”. Tente outro nome ou veja todos os pontos.`;
        return;
    }

    elements.emptyTitle.textContent = "Nenhum ponto cadastrado";
    elements.emptyMessage.textContent = "Cadastre o primeiro ponto de coleta para começar.";
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
            <p class="organization">Responsável: ${escapeHtml(state.organizacoes.find(org => org.id === ponto.organizacaoId)?.nome || "Selecione na edição do ponto")}</p>
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
    state.tipoBusca = elements.searchType.value;
    state.termoBusca = termo;
    elements.clearSearch.hidden = false;
    const tipo = state.tipoBusca;
    const endpoint = tipo === "residuo" ? "/nome-residuo" : "/nome-ponto";
    const parametro = encodeURIComponent(termo);
    await carregarPontos(`${API_URL}${endpoint}?nome=${parametro}`, `Resultados para “${termo}”`);
}

async function limparBusca() {
    await listarTodosOsPontos();
}

async function listarTodosOsPontos() {
    elements.searchForm.reset();
    elements.clearSearch.hidden = true;
    state.buscaAtiva = false;
    state.tipoBusca = null;
    state.termoBusca = "";
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
    preencher("organizacaoId", ponto.organizacaoId);
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
    if (elements.submitForm.disabled) return;
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
        organizacaoId: valor("organizacaoId"),
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
        elements.pointDialog.close();
        elements.pointForm.reset();
        state.editandoId = null;
        mostrarToast(editando ? "Ponto atualizado com sucesso." : "Ponto cadastrado com sucesso.");
        await listarTodosOsPontos();
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
        await listarTodosOsPontos();
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
            const campos = body.campos ? Object.values(body.campos).join(" ") : "";
            detail = campos || body.detail || body.message || body.error || detail;
        } catch (_) {
            // A resposta pode não possuir um corpo JSON.
        }
        throw new Error(detail);
    }

    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function renderizarOrganizacoes() {
    const selecionada = elements.organizationSelect.value;
    elements.organizationSelect.innerHTML = '<option value="">Selecione uma organização</option>' +
        state.organizacoes.map(org => `<option value="${escapeHtml(org.id)}">${escapeHtml(org.nome)}</option>`).join("");
    elements.organizationSelect.value = selecionada;
}

async function salvarOrganizacao(event) {
    event.preventDefault();
    elements.organizationSave.disabled = true;
    elements.organizationError.hidden = true;
    try {
        const criada = await requisicao("/organizacoes", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome: valor("organization-name") })
        });
        state.organizacoes.push(criada);
        renderizarOrganizacoes();
        elements.organizationSelect.value = criada.id;
        elements.organizationDialog.close();
        elements.organizationSelect.focus();
    } catch (error) {
        elements.organizationError.textContent = error.message || "Não foi possível salvar a organização.";
        elements.organizationError.hidden = false;
    } finally {
        elements.organizationSave.disabled = false;
    }
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
