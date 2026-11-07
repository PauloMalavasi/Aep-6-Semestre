package com.aep._s.services;

import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import com.aep._s.repositories.RepositoryOrganizacao;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ServicePontoColeta {

    private final RepositoryPontoColeta repository;
    private final RepositoryOrganizacao organizacoes;

    public ServicePontoColeta(RepositoryPontoColeta repository, RepositoryOrganizacao organizacoes) {
        this.repository = repository;
        this.organizacoes = organizacoes;
    }

    public PontoColeta cadastrar(PontoColeta pontoColeta){
        validarOrganizacao(pontoColeta.getOrganizacaoId());
        // Um POST sempre cria um documento, mesmo que o cliente envie um ID.
        pontoColeta.setId(null);
        return repository.insert(pontoColeta);
    }

    public List<PontoColeta> listarPontos() {
        return repository.findAll();
    }

    public List<PontoColeta> buscarPorNome(String nome) {
        return repository.buscarPorNome(Pattern.quote(validarBusca(nome)));
    }

    public List<PontoColeta> buscarPorResiduo(String residuo) {
        String termo = Normalizer.normalize(validarBusca(residuo), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        return repository.findByResiduos(Pattern.quote(termo));
    }

    public PontoColeta buscaPontoId(String id){
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ponto de coleta não encontrado."));
    }

    public void deletar(String id){
        buscaPontoId(id);
        repository.deleteById(id);
    }
    public PontoColeta atualizar(
            String id,
            PontoColeta novosDados
    ) {
        PontoColeta pontoExistente = buscaPontoId(id);
        validarOrganizacao(novosDados.getOrganizacaoId());

        pontoExistente.setOrganizacaoId(novosDados.getOrganizacaoId());
        pontoExistente.setNome(novosDados.getNome());
        pontoExistente.setEndereco(novosDados.getEndereco());
        pontoExistente.setTelefone(novosDados.getTelefone());
        pontoExistente.setResiduos(
                novosDados.getResiduos()
        );
        return repository.save(pontoExistente);
    }

    private void validarOrganizacao(String id) {
        if (id == null || id.isBlank() || !organizacoes.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecione uma organização cadastrada.");
        }
    }

    private String validarBusca(String termo) {
        if (termo == null || termo.isBlank() || termo.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um termo de busca com 1 a 100 caracteres.");
        }
        return termo.trim();
    }
}
