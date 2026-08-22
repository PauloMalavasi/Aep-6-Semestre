package com.aep._s.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "pontos_coleta")
public class PontoColeta {

    @Id
    private String id;
    private String nome;
    private String telefone;
    private Enderco enderco;
    private List<String> tipoResiduos;
}
