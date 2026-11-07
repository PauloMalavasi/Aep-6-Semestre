package com.aep._s.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "pontos_coleta")
public class PontoColeta {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    @NotBlank(message = "Informe o nome do ponto.")
    @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
    private String nome;
    @NotBlank(message = "Informe o telefone.")
    @Pattern(regexp = "(?:\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}|\\d{10,11})",
            message = "Informe um telefone com DDD, como (44) 99999-9999.")
    private String telefone;
    @NotBlank(message = "Selecione a organização responsável.")
    @Size(max = 100, message = "O identificador da organização deve ter até 100 caracteres.")
    private String organizacaoId;

    public String getOrganizacaoId() { return organizacaoId; }
    public void setOrganizacaoId(String organizacaoId) { this.organizacaoId = organizacaoId; }

    @Valid
    @NotNull(message = "Informe o endereço.")
    private Endereco endereco;
    @NotEmpty(message = "Selecione pelo menos um resíduo.")
    @Size(max = 8, message = "Informe no máximo oito resíduos.")
    private List<@NotNull(message = "O resíduo não pode ser nulo.")
            @Pattern(regexp = "ELETRONICO|PILHA|BATERIA|PAPEL|PLASTICO|VIDRO|METAL|ORGANICO",
                    message = "Selecione um resíduo válido: ELETRONICO, PILHA, BATERIA, PAPEL, PLASTICO, VIDRO, METAL ou ORGANICO.") String> residuos;


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

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<String> getResiduos() {
        return residuos;
    }

    public void setResiduos(List<String> residuos) {
        this.residuos = residuos;
    }
}
