package com.aep._s.controllers;

import com.aep._s.models.PontoColeta;
import com.aep._s.services.ServicePontoColeta;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ponto_coleta")
public class ControllerPontoColeta {

    private ServicePontoColeta sevice;

    public  ControllerPontoColeta(ServicePontoColeta sevice) {
        this.sevice = sevice;
    }

    @PostMapping
    public PontoColeta createPontoColeta(@RequestBody PontoColeta pontoColeta) {
        return sevice.cadastrar(pontoColeta);
    }

    @GetMapping
    public List<PontoColeta> listarPontos(){
        return sevice.listarPontos();
    }

    @GetMapping("/nome-ponto")
    public List<PontoColeta> buscarPorNome(String nome){
        return sevice.buscarPorNome(nome);
    }

    @GetMapping("/nome-residuo")
    public List<PontoColeta> buscarPorResiduo(String nome){
        return sevice.buscarPorResiduo(nome);
    }

    @GetMapping("/{id}")
    public PontoColeta buscaPontoId(@PathVariable String id){
        return sevice.buscaPontoId(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable String id){
        sevice.deletar(id);
    }

    @PutMapping("/{id}")
    public PontoColeta atualizar(@PathVariable String id, @RequestBody PontoColeta pontoColeta){
        return sevice.atualizar(id, pontoColeta);
    }
}
