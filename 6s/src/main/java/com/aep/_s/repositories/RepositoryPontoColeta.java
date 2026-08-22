package com.aep._s.repositories;

import com.aep._s.models.PontoColeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RepositoryPontoColeta extends MongoRepository<PontoColeta, String> {

    //Função para buscar o residuoe o ponto de coleta por nome.
    //$regex procura um texto.
    //$options função que ignora letra maiuscula e miniscula.

    @Query("{ 'residuos': { $regex: ?0, $options: 'i' } }")
    List<PontoColeta> findByResiduos(String residuos);

    @Query("{ 'nome': { $regex: ?0, $options: 'i' } }")
    List<PontoColeta> buscarPorNome(String nome);

}
