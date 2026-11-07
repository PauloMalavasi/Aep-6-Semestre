package com.aep._s.controllers;

import com.aep._s.models.PontoColeta;
import com.aep._s.services.ServicePontoColeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ponto_coleta")
@Tag(name = "Pontos de coleta", description = "Cadastro, consulta, atualização e exclusão de pontos.")
public class ControllerPontoColeta {

    private final ServicePontoColeta service;

    public ControllerPontoColeta(ServicePontoColeta service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um ponto de coleta")
    public ResponseEntity<PontoColeta> createPontoColeta(@Valid @RequestBody PontoColeta pontoColeta) {
        PontoColeta criado = service.cadastrar(pontoColeta);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar todos os pontos")
    public List<PontoColeta> listarPontos(){
        return service.listarPontos();
    }

    @GetMapping("/nome-ponto")
    @Operation(summary = "Buscar por parte do nome, ignorando maiúsculas e minúsculas")
    public List<PontoColeta> buscarPorNome(@RequestParam(defaultValue = "") String nome){
        return service.buscarPorNome(nome);
    }

    @GetMapping("/nome-residuo")
    @Operation(summary = "Buscar por resíduo, aceitando acentos e letras minúsculas")
    public List<PontoColeta> buscarPorResiduo(@RequestParam(defaultValue = "") String nome){
        return service.buscarPorResiduo(nome);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar um ponto pelo ID")
    public PontoColeta buscaPontoId(@PathVariable String id){
        return service.buscaPontoId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um ponto existente")
    public void deletar(@PathVariable String id){
        service.deletar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Substituir os dados de um ponto existente")
    public PontoColeta atualizar(@PathVariable String id, @Valid @RequestBody PontoColeta pontoColeta){
        return service.atualizar(id, pontoColeta);
    }
}
