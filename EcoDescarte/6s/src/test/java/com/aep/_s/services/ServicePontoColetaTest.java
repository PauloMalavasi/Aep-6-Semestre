package com.aep._s.services;

import com.aep._s.models.Endereco;
import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import com.aep._s.repositories.RepositoryOrganizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePontoColetaTest {

    @Mock
    private RepositoryPontoColeta repository;
    @Mock
    private RepositoryOrganizacao organizacoes;

    private ServicePontoColeta service;

    @BeforeEach
    void configurar() {
        service = new ServicePontoColeta(repository, organizacoes);
    }

    @Test
    void deveCadastrarPontoDeColeta() {
        PontoColeta ponto = criarPonto("1", "Eco Ponto");
        when(organizacoes.existsById("org-1")).thenReturn(true);
        when(repository.insert(ponto)).thenReturn(ponto);

        PontoColeta resultado = service.cadastrar(ponto);

        assertSame(ponto, resultado);
        assertNull(ponto.getId());
        verify(repository).insert(ponto);
        verify(repository, never()).save(any());
    }

    @Test
    void deveListarTodosOsPontos() {
        List<PontoColeta> pontos = List.of(
                criarPonto("1", "Eco Ponto Centro"),
                criarPonto("2", "Eco Ponto Bairro")
        );
        when(repository.findAll()).thenReturn(pontos);

        assertEquals(pontos, service.listarPontos());
        verify(repository).findAll();
    }

    @Test
    void deveBuscarPontosPorNome() {
        List<PontoColeta> pontos = List.of(criarPonto("1", "Eco Ponto Centro"));
        when(repository.buscarPorNome(Pattern.quote("Centro"))).thenReturn(pontos);

        assertEquals(pontos, service.buscarPorNome("Centro"));
        verify(repository).buscarPorNome(Pattern.quote("Centro"));
    }

    @Test
    void deveBuscarPontosPorResiduo() {
        List<PontoColeta> pontos = List.of(criarPonto("1", "Eco Ponto Centro"));
        when(repository.findByResiduos(Pattern.quote("PLASTICO"))).thenReturn(pontos);

        assertEquals(pontos, service.buscarPorResiduo(" plástico "));
        verify(repository).findByResiduos(Pattern.quote("PLASTICO"));
    }

    @Test
    void deveBuscarPontoPorId() {
        PontoColeta ponto = criarPonto("1", "Eco Ponto Centro");
        when(repository.findById("1")).thenReturn(Optional.of(ponto));

        assertSame(ponto, service.buscaPontoId("1"));
        verify(repository).findById("1");
    }

    @Test
    void deveRetornar404QuandoIdNaoExistir() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.buscaPontoId("inexistente")).getStatusCode());
        verify(repository).findById("inexistente");
    }

    @Test
    void deveExcluirPontoPorId() {
        when(repository.findById("1")).thenReturn(Optional.of(criarPonto("1", "Eco Ponto")));
        service.deletar("1");

        verify(repository).deleteById("1");
    }

    @Test
    void deveAtualizarTodosOsDadosDoPonto() {
        PontoColeta existente = criarPonto("1", "Nome antigo");
        PontoColeta novosDados = criarPonto(null, "Nome atualizado");
        novosDados.setOrganizacaoId("org-2");
        when(organizacoes.existsById("org-2")).thenReturn(true);
        Endereco novoEndereco = new Endereco();
        novosDados.setEndereco(novoEndereco);
        novosDados.setTelefone("(44) 98888-8888");
        novosDados.setResiduos(List.of("vidro", "papel"));

        when(repository.findById("1")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        PontoColeta resultado = service.atualizar("1", novosDados);

        assertSame(existente, resultado);
        assertEquals("1", resultado.getId());
        assertEquals("Nome atualizado", resultado.getNome());
        assertEquals("org-2", resultado.getOrganizacaoId());
        assertEquals("(44) 98888-8888", resultado.getTelefone());
        assertSame(novoEndereco, resultado.getEndereco());
        assertEquals(List.of("vidro", "papel"), resultado.getResiduos());
        verify(repository).save(existente);
    }

    @Test
    void naoDeveSalvarAtualizacaoDeIdInexistente() {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.atualizar("ausente", criarPonto(null, "Novo"))).getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveExcluirIdInexistente() {
        assertThrows(ResponseStatusException.class, () -> service.deletar("ausente"));
        verify(repository, never()).deleteById(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarBuscaVazia(String termo) {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorNome(termo)).getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.buscarPorResiduo(termo));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarBuscaMuitoLonga() {
        assertThrows(ResponseStatusException.class, () -> service.buscarPorNome("a".repeat(101)));
        verifyNoInteractions(repository);
    }

    @Test
    void deveBuscarCaracteresEspeciaisComoTextoLiteral() {
        service.buscarPorNome(" .*[");
        verify(repository).buscarPorNome(Pattern.quote(".*["));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "inexistente"})
    void deveRejeitarVinculoInvalidoSemGravar(String organizacaoId) {
        PontoColeta ponto = criarPonto("1", "Original");
        ponto.setOrganizacaoId(organizacaoId);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(ponto)).getStatusCode());
        verify(repository, never()).insert(any(PontoColeta.class));
    }

    @Test
    void devePreservarPontoQuandoNovaOrganizacaoNaoExiste() {
        PontoColeta existente = criarPonto("1", "Original");
        when(repository.findById("1")).thenReturn(Optional.of(existente));
        PontoColeta novos = criarPonto(null, "Alterado");
        novos.setOrganizacaoId("ausente");
        assertThrows(ResponseStatusException.class, () -> service.atualizar("1", novos));
        assertEquals("Original", existente.getNome());
        assertEquals("org-1", existente.getOrganizacaoId());
        verify(repository, never()).save(any());
    }

    private PontoColeta criarPonto(String id, String nome) {
        PontoColeta ponto = new PontoColeta();
        ponto.setId(id);
        ponto.setOrganizacaoId("org-1");
        ponto.setNome(nome);
        ponto.setTelefone("(44) 99999-9999");
        ponto.setResiduos(List.of("pilhas"));
        return ponto;
    }
}
