package com.art.inventario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.art.inventario.aplicacion.ParametroLegalAplicacion;
import com.art.inventario.dominio.ParametroLegal;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;
import com.art.inventario.excepcion.NoEncontradoExcepcion;
import com.art.inventario.puerto.salida.CambiosNotificador;
import com.art.inventario.puerto.salida.ParametroLegalPersistencia;

@ExtendWith(MockitoExtension.class)
class ParametroLegalAplicacionTest {

	@Mock
	private ParametroLegalPersistencia persistencia;
	@Mock
	private CambiosNotificador notificador;

	private ParametroLegalAplicacion aplicacion;

	@BeforeEach
	void setUp() {
		aplicacion = new ParametroLegalAplicacion(persistencia, notificador);
	}

	@Test
	void eliminarParametroAnioAnteriorOk() {
		ParametroLegal param = new ParametroLegal();
		param.setId(10L);
		param.setAnio(LocalDate.now().getYear() - 1);
		when(persistencia.obtener(10L)).thenReturn(param);

		assertDoesNotThrow(() -> aplicacion.eliminar(10L));

		verify(persistencia).eliminar(10L);
		verify(notificador).publicar(CambiosNotificador.RECURSO_PARAMETROS_LEGALES);
	}

	@Test
	void eliminarParametroAnioEnCursoLanzaExcepcion() {
		ParametroLegal param = new ParametroLegal();
		param.setId(20L);
		param.setAnio(LocalDate.now().getYear());
		when(persistencia.obtener(20L)).thenReturn(param);

		DatosInvalidosExcepcion ex = assertThrows(DatosInvalidosExcepcion.class,
				() -> aplicacion.eliminar(20L));

		assert ex.getMessage().contains("No es posible eliminar el parámetro legal del año en curso");
		verify(persistencia, never()).eliminar(anyLong());
	}

	@Test
	void eliminarParametroNoExistenteLanzaNoEncontrado() {
		when(persistencia.obtener(999L)).thenThrow(new NoEncontradoExcepcion("Parámetro legal no encontrado"));

		assertThrows(NoEncontradoExcepcion.class, () -> aplicacion.eliminar(999L));

		verify(persistencia, never()).eliminar(anyLong());
	}
}
