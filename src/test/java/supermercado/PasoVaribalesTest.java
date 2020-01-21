package supermercado;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ipartek.formacion.supermercado.modelo.pojo.Categoria;

public class PasoVaribalesTest {



	@Test	
	public void testVariablesObjetos() throws CloneNotSupportedException {
		
		Categoria c1 = new Categoria();
		c1.setNombre("fruteria");
		
		Categoria c2 = c1;              // dos objetos, misma posicion de memoria
		c2.setNombre("Otra");
		
		assertEquals("Otra", c1.getNombre());
		assertEquals("Otra", c2.getNombre());
		
		Categoria c3 = c2.clone();     // dos objetos, 2 posiciones de memoria
		c3.setNombre("clonado");
		
		assertEquals("Otra", c1.getNombre());
		assertEquals("Otra", c2.getNombre());
		assertEquals("clonado", c3.getNombre());
		
		
	}
	

	
	@Test
	public void testVariablesPrimitivas() {

		int i = 0 ;
		int j = i;  // dos variables pero no comparten la posicon de memoria		
		j = 5;
		
		assertEquals(0, i);
		assertEquals(5, j);
		
		
		
	}

	
	
}
