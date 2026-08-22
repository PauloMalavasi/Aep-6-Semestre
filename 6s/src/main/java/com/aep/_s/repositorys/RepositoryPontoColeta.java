package com.aep._s.repositorys;

import com.aep._s.models.PontoColeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RepositoryPontoColeta extends MongoRepository<PontoColeta, String> {

    @Query("{ 'residuos': { $regex: ?0, $options: 'i' } }")
    List<PontoColeta> findByResiduos(String residuos);

}
