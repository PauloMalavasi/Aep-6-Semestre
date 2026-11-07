package com.aep._s.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Endereco {

    @NotBlank(message = "Informe o logradouro.")
    @Size(max = 150, message = "O logradouro deve ter até 150 caracteres.")
    private String logradouro;
    @NotBlank(message = "Informe o número ou S/N.")
    @Size(max = 10, message = "O número deve ter até 10 caracteres.")
    private String numero;
    @NotBlank(message = "Informe o bairro.")
    @Size(max = 100, message = "O bairro deve ter até 100 caracteres.")
    private String bairro;
    @NotBlank(message = "Informe o CEP.")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Informe um CEP válido, como 87000-000.")
    private String cep;
    @NotBlank(message = "Informe a cidade.")
    @Size(max = 100, message = "A cidade deve ter até 100 caracteres.")
    private String cidade;
    @NotBlank(message = "Informe a UF.")
    @Pattern(regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
            message = "Informe uma UF brasileira válida, como PR.")
    private String uf;

    public Endereco() {
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
