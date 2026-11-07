package com.aep._s.repositories;

import com.aep._s.models.Organizacao;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositoryOrganizacao extends MongoRepository<Organizacao, String> {
}
