package com.aep._s.configuration;

import com.aep._s.models.Endereco;
import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

import java.util.List;

@Configuration
public class DadosIniciaisConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosIniciaisConfiguration.class);

    @Bean
    ApplicationRunner cadastrarPontosIniciais(RepositoryPontoColeta repository) {
        return args -> {
            try {
                salvarSeAusente(repository, ponto(
                        "ponto-inicial-maringa-centro", "Eco Ponto Centro", "(44) 3221-1234",
                        "Avenida Brasil", "1000", "Zona 1", "87013-000", "Maringá",
                        List.of("ELETRONICO", "PILHA", "BATERIA")));
                salvarSeAusente(repository, ponto(
                        "ponto-inicial-maringa-zona-7", "Eco Ponto Zona 7", "(44) 3221-5678",
                        "Avenida Mandacaru", "2500", "Zona 7", "87080-000", "Maringá",
                        List.of("PAPEL", "PLASTICO", "VIDRO", "METAL")));
                salvarSeAusente(repository, ponto(
                        "ponto-inicial-jandaia-centro", "Eco Ponto Jandaia do Sul", "(43) 3432-1234",
                        "Avenida Getúlio Vargas", "500", "Centro", "86900-000", "Jandaia do Sul",
                        List.of("ELETRONICO", "PILHA", "PAPEL", "PLASTICO")));
            } catch (DataAccessException exception) {
                LOGGER.warn("Não foi possível cadastrar os pontos iniciais: MongoDB indisponível.");
            }
        };
    }

    private void salvarSeAusente(RepositoryPontoColeta repository, PontoColeta ponto) {
        if (!repository.existsById(ponto.getId())) {
            repository.save(ponto);
        }
    }

    private PontoColeta ponto(String id, String nome, String telefone, String logradouro,
                              String numero, String bairro, String cep, String cidade,
                              List<String> residuos) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCep(cep);
        endereco.setCidade(cidade);
        endereco.setUf("PR");

        PontoColeta ponto = new PontoColeta();
        ponto.setId(id);
        ponto.setNome(nome);
        ponto.setTelefone(telefone);
        ponto.setEndereco(endereco);
        ponto.setResiduos(residuos);
        return ponto;
    }
}
