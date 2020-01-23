package supermercado;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ipartek.formacion.supermercado.utils.Utilidades;

public class UtilidadesTest {

	@Test
	public void test() {
		assertEquals(0,Utilidades.contarPalabras(null));
		assertEquals(0,Utilidades.contarPalabras(""));
		assertEquals(0,Utilidades.contarPalabras("   "));
		assertEquals(2,Utilidades.contarPalabras("hola adios"));
		assertEquals(2,Utilidades.contarPalabras("hola;adios"));
		assertEquals(4,Utilidades.contarPalabras("hola adios hasta luego"));
		assertEquals(2,Utilidades.contarPalabras("h;F"));
		assertEquals(2,Utilidades.contarPalabras("h;:F"));
		assertEquals(3,Utilidades.contarPalabras("h;:F hola"));
	}

}
