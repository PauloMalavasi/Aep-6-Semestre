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

@ExtendWith(MockitoExtension.class)
class ControllerPontoColetaTest {

    @Mock
    private ServicePontoColeta service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ControllerPontoColeta(service))
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
                                  "telefone": "(44) 99999-9999",
                                  "residuos": ["pilhas"]
                                }
                                """))
                .andExpect(status().isOk())
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
                .andExpect(status().isOk());

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
                                  "telefone": "(44) 98888-8888",
                                  "residuos": ["vidro"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nome").value("Eco Ponto Atualizado"));
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
