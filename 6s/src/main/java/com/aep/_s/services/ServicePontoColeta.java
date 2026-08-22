package com.aep._s.services;

import com.aep._s.models.PontoColeta;
import com.aep._s.repositories.RepositoryPontoColeta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePontoColeta {

    private RepositoryPontoColeta repository;

    public ServicePontoColeta(RepositoryPontoColeta repository) {
        this.repository = repository;
    }

    public PontoColeta cadastrar(PontoColeta pontoColeta){
        return repository.save(pontoColeta);
    }

    public List<PontoColeta> listarPontos() {
        return repository.findAll();
    }

    public List<PontoColeta> buscarPorNome(String nome) {
        return repository.buscarPorNome(nome);
    }

    public List<PontoColeta> buscarPorResiduo(String residuo) {
        return repository.findByResiduos(residuo);
    }

    public PontoColeta buscaPontoId(String id){
        return repository.findById(id).orElse(null);
    }

    public void deletar(String id){
        repository.deleteById(id);
    }
    public PontoColeta atualizar(
            String id,
            PontoColeta novosDados
    ) {
        PontoColeta pontoExistente = buscaPontoId(id);

        pontoExistente.setNome(novosDados.getNome());
        pontoExistente.setEndereco(novosDados.getEndereco());
        pontoExistente.setTelefone(novosDados.getTelefone());
        pontoExistente.setResiduos(
                novosDados.getResiduos()
        );
        return repository.save(pontoExistente);
    }

}
