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

    @Test
    void deveArmazenarOsDadosDoEndereco() {
        Endereco endereco = new Endereco();

        endereco.setLogradouro("Avenida Brasil");
        endereco.setNumero("100");
        endereco.setBairro("Centro");
        endereco.setCep("87000-000");
        endereco.setCidade("Maringá");
        endereco.setUf("PR");

        assertEquals("Avenida Brasil", endereco.getLogradouro());
        assertEquals("100", endereco.getNumero());
        assertEquals("Centro", endereco.getBairro());
        assertEquals("87000-000", endereco.getCep());
        assertEquals("Maringá", endereco.getCidade());
        assertEquals("PR", endereco.getUf());
    }
}
