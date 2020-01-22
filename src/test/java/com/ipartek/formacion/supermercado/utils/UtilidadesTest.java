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

}
