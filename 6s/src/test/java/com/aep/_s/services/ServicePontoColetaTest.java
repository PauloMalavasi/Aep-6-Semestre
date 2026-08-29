package com.aep._s.services;

import com.aep._s.models.Endereco;
import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePontoColetaTest {

    @Mock
    private RepositoryPontoColeta repository;

    private ServicePontoColeta service;

    @BeforeEach
    void configurar() {
        service = new ServicePontoColeta(repository);
    }

    @Test
    void deveCadastrarPontoDeColeta() {
        PontoColeta ponto = criarPonto("1", "Eco Ponto");
        when(repository.save(ponto)).thenReturn(ponto);

        PontoColeta resultado = service.cadastrar(ponto);

        assertSame(ponto, resultado);
        verify(repository).save(ponto);
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
        when(repository.buscarPorNome("Centro")).thenReturn(pontos);

        assertEquals(pontos, service.buscarPorNome("Centro"));
        verify(repository).buscarPorNome("Centro");
    }

    @Test
    void deveBuscarPontosPorResiduo() {
        List<PontoColeta> pontos = List.of(criarPonto("1", "Eco Ponto Centro"));
        when(repository.findByResiduos("pilhas")).thenReturn(pontos);

        assertEquals(pontos, service.buscarPorResiduo("pilhas"));
        verify(repository).findByResiduos("pilhas");
    }

    @Test
    void deveBuscarPontoPorId() {
        PontoColeta ponto = criarPonto("1", "Eco Ponto Centro");
        when(repository.findById("1")).thenReturn(Optional.of(ponto));

        assertSame(ponto, service.buscaPontoId("1"));
        verify(repository).findById("1");
    }

    @Test
    void deveRetornarNuloQuandoIdNaoExistir() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertNull(service.buscaPontoId("inexistente"));
        verify(repository).findById("inexistente");
    }

    @Test
    void deveExcluirPontoPorId() {
        service.deletar("1");

        verify(repository).deleteById("1");
    }

    @Test
    void deveAtualizarTodosOsDadosDoPonto() {
        PontoColeta existente = criarPonto("1", "Nome antigo");
        PontoColeta novosDados = criarPonto(null, "Nome atualizado");
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
        assertEquals("(44) 98888-8888", resultado.getTelefone());
        assertSame(novoEndereco, resultado.getEndereco());
        assertEquals(List.of("vidro", "papel"), resultado.getResiduos());
        verify(repository).save(existente);
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
