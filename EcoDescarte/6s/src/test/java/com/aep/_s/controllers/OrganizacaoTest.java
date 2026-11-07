package com.aep._s.controllers;

import com.aep._s.models.Organizacao;
import com.aep._s.repositories.RepositoryOrganizacao;
import com.aep._s.repositories.RepositoryPontoColeta;
import com.aep._s.services.ServiceOrganizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrganizacaoTest {
    private final RepositoryOrganizacao repository = mock(RepositoryOrganizacao.class);
    private final RepositoryPontoColeta pontos = mock(RepositoryPontoColeta.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerOrganizacao(new ServiceOrganizacao(repository, pontos)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private Organizacao organizacao() {
        Organizacao org = new Organizacao();
        org.setId("org-1");
        org.setNome("Associação exemplo");
        return org;
    }

    @Test
    void cadastrarGeraIdSemSobrescrever() throws Exception {
        when(repository.insert(any(Organizacao.class))).thenAnswer(invocation -> {
            Organizacao org = invocation.getArgument(0);
            assertNull(org.getId());
            org.setId("novo-id");
            return org;
        });
        mvc.perform(post("/organizacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"existente\",\"nome\":\"Associação\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Associação"))
                .andExpect(header().string("Location", "http://localhost/organizacoes/novo-id"));
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"nome\":\" \"}", "{\"nome\":null}"})
    void recusarNomeInvalido(String corpo) throws Exception {
        mvc.perform(post("/organizacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nome").exists());
        mvc.perform(put("/organizacoes/org-1").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void listarEBuscar() throws Exception {
        when(repository.findAll()).thenReturn(List.of(organizacao()));
        when(repository.findById("org-1")).thenReturn(Optional.of(organizacao()));
        mvc.perform(get("/organizacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("org-1"));
        mvc.perform(get("/organizacoes/org-1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Associação exemplo"));
    }

    @Test
    void atualizarPreservaIdentificador() throws Exception {
        when(repository.findById("org-1")).thenReturn(Optional.of(organizacao()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        mvc.perform(put("/organizacoes/org-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"outro\",\"nome\":\"Novo nome\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("org-1"))
                .andExpect(jsonPath("$.nome").value("Novo nome"));
    }

    @Test
    void recusarExclusaoComPontosVinculados() throws Exception {
        when(repository.findById("org-1")).thenReturn(Optional.of(organizacao()));
        when(pontos.existsByOrganizacaoId("org-1")).thenReturn(true);
        mvc.perform(delete("/organizacoes/org-1")).andExpect(status().isConflict());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void excluirOrganizacaoSemPontos() throws Exception {
        when(repository.findById("org-1")).thenReturn(Optional.of(organizacao()));
        mvc.perform(delete("/organizacoes/org-1")).andExpect(status().isNoContent());
        verify(repository).deleteById("org-1");
    }

    @Test
    void retornar404SemAlterarDados() throws Exception {
        mvc.perform(get("/organizacoes/ausente")).andExpect(status().isNotFound());
        mvc.perform(delete("/organizacoes/ausente")).andExpect(status().isNotFound());
        mvc.perform(put("/organizacoes/ausente").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo nome\"}"))
                .andExpect(status().isNotFound());
        verify(repository, never()).deleteById(any());
        verify(repository, never()).save(any());
    }
}
