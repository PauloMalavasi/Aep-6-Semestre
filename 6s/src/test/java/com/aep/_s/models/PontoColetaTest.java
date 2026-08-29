package com.aep._s.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PontoColetaTest {

    @Test
    void deveArmazenarOsDadosDoPontoDeColeta() {
        PontoColeta ponto = new PontoColeta();
        Endereco endereco = new Endereco();
        List<String> residuos = List.of("pilhas", "eletronicos");

        ponto.setId("1");
        ponto.setNome("Eco Ponto Centro");
        ponto.setTelefone("(44) 99999-9999");
        ponto.setEndereco(endereco);
        ponto.setResiduos(residuos);

        assertEquals("1", ponto.getId());
        assertEquals("Eco Ponto Centro", ponto.getNome());
        assertEquals("(44) 99999-9999", ponto.getTelefone());
        assertSame(endereco, ponto.getEndereco());
        assertEquals(residuos, ponto.getResiduos());
    }
}
