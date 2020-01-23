package com.ipartek.formacion.supermercado.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class UtilidadesTest {

	@Test
	public void testObtenerId() throws Exception {
		assertEquals(-1, Utilidades.obtenerId(null));

		assertEquals(-1, Utilidades.obtenerId("/"));
		assertEquals(2, Utilidades.obtenerId("/2/"));
		assertEquals(2, Utilidades.obtenerId("/2"));
		assertEquals(99, Utilidades.obtenerId("/99"));



		try {
			Utilidades.obtenerId("/pepe");
			fail("Deberia lanzar una excepcion");
		} catch (Exception e) {
			assertTrue(true);
		}
		try {
			Utilidades.obtenerId("/pepe/");
			fail("Deberia lanzar una excepcion");
		} catch (Exception e) {
			assertTrue(true);
		}
		try {
			Utilidades.obtenerId("/99/huhu/uuy/tt");
			fail("Deberia lanzar una excepcion");
		} catch (Exception e) {
			assertTrue(true);
		}



	}

	@Test
	public void contarPalabrasTest1() {
		assertEquals(0, Utilidades.contarPalabras(null));
		assertEquals(0, Utilidades.contarPalabras(""));
		assertEquals(0, Utilidades.contarPalabras("     "));
		assertEquals(2, Utilidades.contarPalabras("hola caracola"));
		assertEquals(2, Utilidades.contarPalabras("hola         mundo"));
		assertEquals(2, Utilidades.contarPalabras("     hola         mundo      "));
	}

	@Test
	public void contarPalabrasTest2() {
		assertEquals(2, Utilidades.contarPalabras("Ho44la, mu33ndo"));
		assertEquals(2, Utilidades.contarPalabras("hola44 ? 22mundo"));
		assertEquals(2, Utilidades.contarPalabras("??hola44__ __ -- ^^()/&%$·{}? 22mundo"));
	}

}
