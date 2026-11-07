package com.aep._s.controllers;

import com.aep._s.models.PontoColeta;
import com.aep._s.services.ServicePontoColeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
class ControllerPontoColetaTest {

    @Mock
    private ServicePontoColeta service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControllerPontoColeta(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void deveCadastrarPontoDeColeta() throws Exception {
        PontoColeta ponto = criarPonto("1", "Eco Ponto Centro");
        when(service.cadastrar(any(PontoColeta.class))).thenReturn(ponto);

        mockMvc.perform(post("/ponto_coleta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Eco Ponto Centro",
                                  "organizacaoId": "org-1",
                                  "telefone": "(44) 99999-9999",
                                  "endereco": {"logradouro":"Avenida Brasil","numero":"100","bairro":"Centro","cep":"87000-000","cidade":"Maringá","uf":"PR"},
                                  "residuos": ["PILHA"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/ponto_coleta/1"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Eco Ponto Centro"));
    }

    @Test
    void deveListarPontosDeColeta() throws Exception {
        when(service.listarPontos()).thenReturn(List.of(
                criarPonto("1", "Eco Ponto Centro"),
                criarPonto("2", "Eco Ponto Bairro")
        ));

        mockMvc.perform(get("/ponto_coleta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    void deveBuscarPontoPorNome() throws Exception {
        when(service.buscarPorNome("Centro"))
                .thenReturn(List.of(criarPonto("1", "Eco Ponto Centro")));

        mockMvc.perform(get("/ponto_coleta/nome-ponto").param("nome", "Centro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Eco Ponto Centro"));
    }

    @Test
    void deveBuscarPontoPorResiduo() throws Exception {
        when(service.buscarPorResiduo("pilhas"))
                .thenReturn(List.of(criarPonto("1", "Eco Ponto Centro")));

        mockMvc.perform(get("/ponto_coleta/nome-residuo").param("nome", "pilhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].residuos[0]").value("pilhas"));
    }

    @Test
    void deveBuscarPontoPorId() throws Exception {
        when(service.buscaPontoId("1")).thenReturn(criarPonto("1", "Eco Ponto Centro"));

        mockMvc.perform(get("/ponto_coleta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void deveExcluirPontoPorId() throws Exception {
        mockMvc.perform(delete("/ponto_coleta/1"))
                .andExpect(status().isNoContent());

        verify(service).deletar("1");
    }

    @Test
    void deveAtualizarPontoDeColeta() throws Exception {
        PontoColeta atualizado = criarPonto("1", "Eco Ponto Atualizado");
        when(service.atualizar(org.mockito.ArgumentMatchers.eq("1"), any(PontoColeta.class)))
                .thenReturn(atualizado);

        mockMvc.perform(put("/ponto_coleta/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Eco Ponto Atualizado",
                                  "organizacaoId": "org-1",
                                  "telefone": "(44) 98888-8888",
                                  "endereco": {"logradouro":"Avenida Brasil","numero":"100","bairro":"Centro","cep":"87000-000","cidade":"Maringá","uf":"PR"},
                                  "residuos": ["VIDRO"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Eco Ponto Atualizado"));
    }

    private static final String VALIDO = """
            {"nome":"Eco Ponto", "telefone":"(44) 99999-9999", "organizacaoId":"org-1",
             "endereco":{"logradouro":"Rua A","numero":"100","bairro":"Centro",
                         "cep":"87000-000","cidade":"Maringá","uf":"PR"},
             "residuos":["PILHA"]}
            """;

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void deveRejeitarCamposObrigatoriosAusentes(String metodo) throws Exception {
        var request = metodo.equals("POST") ? post("/ponto_coleta") : put("/ponto_coleta/1");
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.endereco").exists());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nome", "telefone", "organizacaoId", "logradouro", "numero", "bairro", "cep", "cidade", "uf", "residuos"})
    void deveRejeitarCampoInvalido(String campo) throws Exception {
        String json = switch (campo) {
            case "nome" -> VALIDO.replace("Eco Ponto", "   ");
            case "organizacaoId" -> VALIDO.replace("org-1", " ");
            case "telefone" -> VALIDO.replace("(44) 99999-9999", "123");
            case "logradouro" -> VALIDO.replace("Rua A", " ");
            case "numero" -> VALIDO.replace("100", " ");
            case "bairro" -> VALIDO.replace("Centro", " ");
            case "cep" -> VALIDO.replace("87000-000", "123");
            case "cidade" -> VALIDO.replace("Maringá", " ");
            case "uf" -> VALIDO.replace("PR", "ZZ");
            default -> VALIDO.replace("PILHA", "INVALIDO");
        };
        mockMvc.perform(post("/ponto_coleta").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").exists());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "[null]", "null"})
    void deveRejeitarListaDeResiduosInvalida(String residuos) throws Exception {
        mockMvc.perform(post("/ponto_coleta").contentType(MediaType.APPLICATION_JSON)
                        .content(VALIDO.replace("[\"PILHA\"]", residuos)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deveRejeitarJsonMalformado() throws Exception {
        mockMvc.perform(post("/ponto_coleta").contentType(MediaType.APPLICATION_JSON).content("{invalido"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").exists());
        verifyNoInteractions(service);
    }

    @Test
    void deveRetornar404NaConsultaInexistente() throws Exception {
        when(service.buscaPontoId("ausente")).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."));
        mockMvc.perform(get("/ponto_coleta/ausente"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Ponto de coleta não encontrado."));
    }

    @Test
    void deveRetornar404NaAtualizacaoInexistente() throws Exception {
        when(service.atualizar(org.mockito.ArgumentMatchers.eq("ausente"), any())).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."));
        mockMvc.perform(put("/ponto_coleta/ausente").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404NaExclusaoInexistente() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."))
                .when(service).deletar("ausente");
        mockMvc.perform(delete("/ponto_coleta/ausente")).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar503QuandoBancoIndisponivel() throws Exception {
        when(service.listarPontos()).thenThrow(new DataAccessResourceFailureException("detalhe interno"));
        mockMvc.perform(get("/ponto_coleta")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Não foi possível acessar o banco de dados. Tente novamente em instantes."));
    }

    @Test
    void deveRetornar400QuandoValidadorDoMongoRejeitar() throws Exception {
        when(service.cadastrar(any())).thenThrow(new DataIntegrityViolationException("documento inválido"));
        mockMvc.perform(post("/ponto_coleta").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isBadRequest());
    }

    private PontoColeta criarPonto(String id, String nome) {
        PontoColeta ponto = new PontoColeta();
        ponto.setId(id);
        ponto.setNome(nome);
        ponto.setTelefone("(44) 99999-9999");
        ponto.setResiduos(List.of("pilhas"));
        return ponto;
    }
}
