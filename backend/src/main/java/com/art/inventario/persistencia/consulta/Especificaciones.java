package com.art.inventario.persistencia.consulta;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.art.inventario.aplicacion.dto.ConsultaPaginada;
import com.art.inventario.excepcion.DatosInvalidosExcepcion;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Construye Specification y Sort para los listados paginados y filtrables de
 * los cat&aacute;logos. Centraliza la validaci&oacute;n de campos ordenables y
 * buscables (listas blancas) y el orden con desempate por id.
 *
 * @param <T> tipo de entidad de persistencia sobre la que se filtra
 */
public final class Especificaciones<T> {

	public enum TipoFiltro {
		TEXTO_EXACTO, TEXTO_CONTIENE, ID, NUMERO, FECHA, BOOLEANO, NULO
	}

	/**
	 * Predicado de filtro de resoluci&oacute;n a medida. Se usa para campos
	 * derivados que no existen como columna simple (p. ej. estado de pago de una
	 * factura, contratado de un empleado), donde el adaptador conoce la
	 * subconsulta o el c&aacute;lculo exacto.
	 */
	@FunctionalInterface
	public interface PredicadoFiltro {
		Predicate aplicar(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb, String valor);
	}

	/** Campo de filtro: propiedad de la entidad y c&oacute;mo interpretar el valor. */
	public static class CampoFiltro {
		private final String propiedad;
		private final TipoFiltro tipo;
		private final PredicadoFiltro predicado;

		public CampoFiltro(String propiedad, TipoFiltro tipo) {
			this.propiedad = propiedad;
			this.tipo = tipo;
			this.predicado = null;
		}

		/** Campo con predicado a medida: la validaci&oacute;n e interpretaci&oacute;n del valor las resuelve el adaptador. */
		public CampoFiltro(PredicadoFiltro predicado) {
			this.propiedad = null;
			this.tipo = null;
			this.predicado = predicado;
		}

		/**
		 * Campo con predicado a medida que adem&aacute;s declara un
		 * {@link TipoFiltro} para que {@link Especificaciones#validar} valide el
		 * valor de forma temprana (p. ej. booleano de {@code contratados}) y
		 * devuelva 400 ante un valor inv&aacute;lido.
		 */
		public CampoFiltro(PredicadoFiltro predicado, TipoFiltro tipo) {
			this.propiedad = null;
			this.tipo = tipo;
			this.predicado = predicado;
		}

		public String getPropiedad() {
			return propiedad;
		}

		public TipoFiltro getTipo() {
			return tipo;
		}

		public PredicadoFiltro getPredicado() {
			return predicado;
		}
	}

	private Especificaciones() {
	}

	/**
	 * Valida los filtros y la b&uacute;squeda libre. Lanza
	 * {@link DatosInvalidosExcepcion} ante un campo desconocido o un valor que
	 * no se pueda interpretar seg&uacute;n el tipo del campo.
	 */
	public static void validar(ConsultaPaginada c, Map<String, CampoFiltro> campos, List<String> buscables) {
		for (Map.Entry<String, String> f : c.getFiltros().entrySet()) {
			String clave = f.getKey();
			CampoFiltro campo = campos.get(clave);
			if (campo == null) {
				throw new DatosInvalidosExcepcion("Campo de filtro desconocido: " + clave);
			}
			if (campo.getPredicado() == null || campo.getTipo() != null) {
				validarValor(clave, f.getValue(), campo);
			}
		}
		if (c.getQ() != null && !c.getQ().isBlank() && buscables.isEmpty()) {
			throw new DatosInvalidosExcepcion("La búsqueda libre no está configurada para este recurso");
		}
	}

	private static void validarValor(String clave, String valor, CampoFiltro campo) {
		if (valor == null || valor.isBlank()) {
			return;
		}
		switch (campo.getTipo()) {
			case ID, NUMERO -> {
				try {
					Long.parseLong(valor.trim());
				} catch (NumberFormatException e) {
					throw new DatosInvalidosExcepcion("Valor no numérico en " + clave + ": " + valor);
				}
			}
			case BOOLEANO -> {
				String v = valor.trim().toLowerCase(Locale.ROOT);
				if (!"true".equals(v) && !"false".equals(v)) {
					throw new DatosInvalidosExcepcion("Valor booleano inválido en " + clave + ": " + valor);
				}
			}
			case NULO -> {
				String v = valor.trim().toLowerCase(Locale.ROOT);
				if (!"true".equals(v) && !"false".equals(v)) {
					throw new DatosInvalidosExcepcion("Valor booleano inválido en " + clave + ": " + valor);
				}
			}
			default -> {
				// texto y fechas: se aceptan tal cual
			}
		}
	}

	/**
	 * Construye el orden por el campo indicado (o el defecto) respetando la
	 * direcci&oacute;n y a&ntilde;adiendo siempre el desempate por id. Valida
	 * que el campo y la direcci&oacute;n sean v&aacute;lidos.
	 */
	public static Sort ordenar(ConsultaPaginada c, Set<String> ordenables, String campoPorDefecto) {
		String campo = c.getOrden();
		if (campo == null || campo.isBlank()) {
			campo = campoPorDefecto;
		}
		if (!ordenables.contains(campo)) {
			throw new DatosInvalidosExcepcion("Campo de orden no permitido: " + campo);
		}
		String dir = c.getDir();
		Sort.Direction direccion = Sort.Direction.ASC;
		if (dir != null && !dir.isBlank()) {
			try {
				direccion = Sort.Direction.fromString(dir);
			} catch (IllegalArgumentException e) {
				throw new DatosInvalidosExcepcion("Dirección de orden no válida: " + dir);
			}
		}
		return Sort.by(direccion, campo).and(Sort.by(direccion, "id"));
	}

	private static final java.util.regex.Pattern FECHA_ISO = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

	/**
	 * Valida el rango de fechas de los filtros {@code fechaDesde}/{@code fechaHasta}
	 * usados por los historiales y devoluciones (H1). Si alguno est&aacute; presente
	 * con formato no ISO (yyyy-MM-dd) o {@code fechaDesde > fechaHasta} lanza
	 * {@link DatosInvalidosExcepcion} (400). Sin filtros de fecha no hace nada.
	 */
	public static void validarRangoFechas(ConsultaPaginada c) {
		String desde = c.getFiltros().get("fechaDesde");
		String hasta = c.getFiltros().get("fechaHasta");
		if (desde != null && !desde.isBlank()) {
			validarFormatoFecha("fechaDesde", desde);
		}
		if (hasta != null && !hasta.isBlank()) {
			validarFormatoFecha("fechaHasta", hasta);
		}
		if (desde != null && !desde.isBlank() && hasta != null && !hasta.isBlank()
				&& desde.compareTo(hasta) > 0) {
			throw new DatosInvalidosExcepcion("La fecha desde no puede ser posterior a la fecha hasta");
		}
	}

	private static void validarFormatoFecha(String clave, String valor) {
		String v = valor.trim();
		if (!FECHA_ISO.matcher(v).matches()) {
			throw new DatosInvalidosExcepcion("Formato de fecha inválido en " + clave + ": " + valor);
		}
		try {
			java.time.LocalDate.parse(v);
		} catch (java.time.format.DateTimeParseException e) {
			throw new DatosInvalidosExcepcion("Formato de fecha inválido en " + clave + ": " + valor);
		}
	}

	/**
	 * Construye el orden de los historiales y devoluciones (movimientos). A
	 * diferencia de {@link #ordenar}, el orden por defecto es {@code fecha desc,
	 * id desc} (H2/D7). Si se indica {@code orden}/{@code dir} se validan contra la
	 * lista blanca y se a&ntilde;ade siempre el desempate por id.
	 */
	public static Sort ordenarMovimientos(ConsultaPaginada c, Set<String> ordenables) {
		String campo = c.getOrden();
		if (campo == null || campo.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"));
		}
		if (!ordenables.contains(campo)) {
			throw new DatosInvalidosExcepcion("Campo de orden no permitido: " + campo);
		}
		String dir = c.getDir();
		Sort.Direction direccion = Sort.Direction.ASC;
		if (dir != null && !dir.isBlank()) {
			try {
				direccion = Sort.Direction.fromString(dir);
			} catch (IllegalArgumentException e) {
				throw new DatosInvalidosExcepcion("Dirección de orden no válida: " + dir);
			}
		}
		return Sort.by(direccion, campo).and(Sort.by(direccion, "id"));
	}

	/**
	 * Construye la Specification con los filtros y la b&uacute;squeda libre.
	 * Valida primero; la b&uacute;squeda libre aplica TEXTO_CONTIONE sobre los
	 * campos buscables, con escape de % y _ y comparaci&oacute;n insensible a
	 * may&uacute;sculas.
	 */
	public static <E> Specification<E> filtrar(ConsultaPaginada c, Map<String, CampoFiltro> campos, List<String> buscables) {
		validar(c, campos, buscables);
		return (root, query, cb) -> {
			Predicate[] predicados = predicados(c, campos, buscables, root, query, cb);
			return predicados.length == 0 ? cb.conjunction() : cb.and(predicados);
		};
	}

	private static <E> Predicate[] predicados(ConsultaPaginada c, Map<String, CampoFiltro> campos,
			List<String> buscables, Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		java.util.ArrayList<Predicate> lista = new java.util.ArrayList<>();
		for (Map.Entry<String, String> f : c.getFiltros().entrySet()) {
			String valor = f.getValue();
			if (valor == null || valor.isBlank()) {
				continue;
			}
			CampoFiltro campo = campos.get(f.getKey());
			PredicadoFiltro predicado = campo.getPredicado();
			if (predicado != null) {
				lista.add(predicado.aplicar(root, query, cb, valor));
				continue;
			}
			Path<Object> path = ruta(root, campo.getPropiedad());
			switch (campo.getTipo()) {
				case TEXTO_EXACTO -> lista.add(cb.equal(path, valor));
				case TEXTO_CONTIENE -> lista.add(cb.like(cb.lower(path.as(String.class)),
						"%" + escapar(valor.toLowerCase(Locale.ROOT)) + "%"));
				case ID, NUMERO -> lista.add(cb.equal(path, Long.parseLong(valor.trim())));
				case FECHA -> lista.add(cb.like(path.as(String.class), valor.trim() + "%"));
				case BOOLEANO -> lista.add(cb.equal(path, Boolean.parseBoolean(valor.trim())));
				case NULO -> lista.add(Boolean.parseBoolean(valor.trim()) ? cb.isNotNull(path) : cb.isNull(path));
				default -> {
					// sin predicado
				}
			}
		}
		if (c.getQ() != null && !c.getQ().isBlank()) {
			Predicate[] qPredicados = buscables.stream()
					.map(b -> {
						Path<Object> p = ruta(root, b);
						return cb.like(cb.lower(p.as(String.class)),
								"%" + escapar(c.getQ().toLowerCase(Locale.ROOT)) + "%");
					})
					.toArray(Predicate[]::new);
			lista.add(cb.or(qPredicados));
		}
		return lista.toArray(new Predicate[0]);
	}

	private static String escapar(String valor) {
		return valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	private static <E> Path<Object> ruta(Root<E> root, String propiedad) {
		String[] partes = propiedad.split("\\.");
		Path<Object> p = root.get(partes[0]);
		for (int i = 1; i < partes.length; i++) {
			p = p.get(partes[i]);
		}
		return p;
	}
}
