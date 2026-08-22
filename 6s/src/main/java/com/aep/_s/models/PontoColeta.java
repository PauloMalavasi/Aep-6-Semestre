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
    private Endereco endereco;
    private List<String> residuos;


    public PontoColeta(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco enderco) {
        this.endereco = enderco;
    }

    public List<String> getResiduos() {
        return residuos;
    }

    public void setResiduos(List<String> residuos) {
        this.residuos = residuos;
    }
}
