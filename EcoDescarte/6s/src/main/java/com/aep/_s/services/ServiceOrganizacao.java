package com.aep._s.services;

import com.aep._s.models.Organizacao;
import com.aep._s.repositories.RepositoryOrganizacao;
import com.aep._s.repositories.RepositoryPontoColeta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceOrganizacao {
    private final RepositoryOrganizacao repository;
    private final RepositoryPontoColeta pontos;

    public ServiceOrganizacao(RepositoryOrganizacao repository, RepositoryPontoColeta pontos) {
        this.repository = repository;
        this.pontos = pontos;
    }

    public List<Organizacao> listar() { return repository.findAll(); }

    public Organizacao buscar(String id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Organização não encontrada."));
    }

    public Organizacao cadastrar(Organizacao organizacao) {
        organizacao.setId(null);
        return repository.insert(organizacao);
    }

    public Organizacao atualizar(String id, Organizacao dados) {
        Organizacao existente = buscar(id);
        existente.setNome(dados.getNome());
        return repository.save(existente);
    }

    public void deletar(String id) {
        buscar(id);
        if (pontos.existsByOrganizacaoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta organização possui pontos de coleta. Transfira ou exclua os pontos antes de removê-la.");
        }
        repository.deleteById(id);
    }
}
