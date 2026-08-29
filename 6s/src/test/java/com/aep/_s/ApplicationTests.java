package com.aep._s;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTests {

	@Test
	void deveEstarConfiguradaComoAplicacaoSpringBoot() {
		assertTrue(Application.class.isAnnotationPresent(SpringBootApplication.class));
	}

}
