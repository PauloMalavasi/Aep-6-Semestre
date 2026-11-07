package com.aep._s.configuration;

import com.aep._s.models.PontoColeta;
import com.aep._s.models.Organizacao;
import com.aep._s.repositories.RepositoryOrganizacao;
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
    private final RepositoryOrganizacao organizacoes = mock(RepositoryOrganizacao.class);

    @Test
    void deveCadastrarTresPontosQuandoAindaNaoExistem() throws Exception {
        ApplicationRunner runner = configuration.cadastrarPontosIniciais(repository, organizacoes);

        runner.run(null);

        verify(repository, times(3)).save(any(PontoColeta.class));
        verify(organizacoes).save(any(Organizacao.class));
    }

    @Test
    void naoDeveDuplicarPontosExistentes() throws Exception {
        when(repository.existsById(any())).thenReturn(true);
        when(organizacoes.existsById(any())).thenReturn(true);

        configuration.cadastrarPontosIniciais(repository, organizacoes).run(null);

        verify(repository, never()).save(any());
        verify(organizacoes, never()).save(any());
    }

    @Test
    void deveManterAplicacaoAtivaQuandoMongoEstiverIndisponivel() throws Exception {
        when(repository.existsById(any())).thenThrow(new DataAccessResourceFailureException("offline"));

        configuration.cadastrarPontosIniciais(repository, organizacoes).run(null);

        verify(repository, never()).save(any());
    }
}
