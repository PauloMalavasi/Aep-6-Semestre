package com.aep._s.controllers;

import com.aep._s.models.Organizacao;
import com.aep._s.services.ServiceOrganizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/organizacoes")
@Tag(name = "Organizações", description = "Responsáveis pelos pontos de coleta.")
public class ControllerOrganizacao {
    private final ServiceOrganizacao service;

    public ControllerOrganizacao(ServiceOrganizacao service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar organizações responsáveis")
    public List<Organizacao> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar organização pelo ID")
    public Organizacao buscar(@PathVariable String id) { return service.buscar(id); }

    @PostMapping
    @Operation(summary = "Cadastrar organização responsável")
    public ResponseEntity<Organizacao> cadastrar(@Valid @RequestBody Organizacao dados) {
        Organizacao criada = service.cadastrar(dados);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri()).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nome da organização")
    public Organizacao atualizar(@PathVariable String id, @Valid @RequestBody Organizacao dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir organização sem pontos vinculados; retorna 409 se estiver em uso")
    public void deletar(@PathVariable String id) { service.deletar(id); }
}
