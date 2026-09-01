package com.aep._s.configuration;

import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DadosIniciaisConfigurationTest {

    private final DadosIniciaisConfiguration configuration = new DadosIniciaisConfiguration();
    private final RepositoryPontoColeta repository = mock(RepositoryPontoColeta.class);

    @Test
    void deveCadastrarTresPontosQuandoAindaNaoExistem() throws Exception {
        ApplicationRunner runner = configuration.cadastrarPontosIniciais(repository);

        runner.run(null);

        verify(repository, times(3)).save(any(PontoColeta.class));
    }

    @Test
    void naoDeveDuplicarPontosExistentes() throws Exception {
        when(repository.existsById(any())).thenReturn(true);

        configuration.cadastrarPontosIniciais(repository).run(null);

        verify(repository, never()).save(any());
    }

    @Test
    void deveManterAplicacaoAtivaQuandoMongoEstiverIndisponivel() throws Exception {
        when(repository.existsById(any())).thenThrow(new DataAccessResourceFailureException("offline"));

        configuration.cadastrarPontosIniciais(repository).run(null);

        verify(repository, never()).save(any());
    }
}
