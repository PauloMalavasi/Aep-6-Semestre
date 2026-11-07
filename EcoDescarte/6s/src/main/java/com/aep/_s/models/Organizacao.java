package com.aep._s.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "organizacoes")
public class Organizacao {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    @NotBlank(message = "Informe o nome da organização.")
    @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
    private String nome;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
