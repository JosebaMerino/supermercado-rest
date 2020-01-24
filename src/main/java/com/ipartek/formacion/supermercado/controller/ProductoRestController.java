package com.ipartek.formacion.supermercado.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ipartek.formacion.supermercado.modelo.dao.ProductoDAO;
import com.ipartek.formacion.supermercado.modelo.pojo.Producto;
import com.ipartek.formacion.supermercado.pojo.ResponseMensaje;
import com.ipartek.formacion.supermercado.utils.Utilidades;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.config.BeanConfig;



/*
 * https://refactoring.guru/refactoring/catalog
 *
 * */

/**
 * Servlet implementation class ProductoRestController
 */
@Path("/")
@Api(tags={"todo"})
@WebServlet({ "/producto/*" })
public class ProductoRestController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LogManager.getLogger(ProductoRestController.class);

	PrintWriter out = null;
	private int responseStatus;
	private Object responseBody;

	private ProductoDAO productoDAO;

	BeanConfig beanConfig;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		productoDAO = ProductoDAO.getInstance();

		  beanConfig = new BeanConfig();

		  beanConfig.setVersion("1.0.0");
		  beanConfig.setTitle("Todo API");
		  beanConfig.setBasePath("/todo/api");
		  beanConfig.setResourcePackage("com.synaptik.javaee");
		  beanConfig.setScan(true);
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//TODO: esto deberia estar en un filtro
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");


		responseBody = null;

		response.setContentType("application/json"); // por defecto => text/html; charset=UTF-8
		response.setCharacterEncoding("UTF-8");
		out = response.getWriter();

		super.service(request, response); // Llama a doGet, o doPost o doPut o doDelete

		response.setStatus(responseStatus);
		String jsonResponseBody = new Gson().toJson(responseBody); // conversion de java a json
		out.print(jsonResponseBody); // "imprimimos" un JSON
		out.flush(); // termina de escribir dato en response body

	}

	//TODO: hacer una clase para las validaciones

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  @ApiOperation(value = "Fetch all to dos")
	  @ApiResponses({
	  @ApiResponse(code=200, message="Success")
	  })
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.trace("Peticion GET");

		int id = -1;
		try {
			id = Utilidades.obtenerId(request.getPathInfo());
		} catch (Exception e) {
			LOG.trace("La url esta mal formada");
			enviarMensaje(HttpServletResponse.SC_BAD_REQUEST, "La url esta mal formada");
			return;
		}

		if (id == -1) {
			String orden = request.getParameter("_orden");
			String columna = request.getParameter("columna");
			listar(orden, columna);
		} else {
			obtenerPorId(id);
		}
	}


	private void obtenerPorId(int id) {
		// Obtener un producto en concreto
		Producto producto = productoDAO.getById(id);
		if (producto != null) {
			setRespose(HttpServletResponse.SC_OK, producto);
		} else {
			enviarMensaje(HttpServletResponse.SC_NOT_FOUND, "No se ha encontrado el producto con id: " + id);
		}
	}

	private void listar(String orden, String columna) {
		// obtener todos los productos que no esten dados de baja de la base de datos

		List<Producto> lista = null;
		if(orden == null || columna == null) {
			 lista = productoDAO.getAll();
		} else {
			lista = productoDAO.getAllOrdered(columna, orden);
		}
		if (!lista.isEmpty()) {
			// response body
			setRespose(HttpServletResponse.SC_OK, lista);

		} else {
			responseStatus = HttpServletResponse.SC_NO_CONTENT;
			LOG.trace("La lista de productos esta vacia");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.debug("POST crear recurso");

		boolean error = false;
		Producto producto = null;
		try {
			producto = requestJSONtoProducto(request, response);
		} catch (Exception e) {
			return;
		}
		try {
			producto = productoDAO.create(producto);
		} catch (MySQLIntegrityConstraintViolationException e) {
			String mensaje = e.getMessage();
			error = true;
			if (mensaje.contains("Duplicate entry")) {
				LOG.error("Nombre duplicado en la BD");

				enviarMensaje("El nombre esta duplicado en la BD");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
			} else {
				LOG.error("Violacion de las restricciones de integridad");

				enviarMensaje("Violacion de las restricciones de integridad");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			error = true;
			LOG.error("No se ha podido crear el Producto en la BD");
			LOG.error(e);

			response.setStatus(HttpServletResponse.SC_CONFLICT);
			enviarMensaje("No se ha podido crear el producto en la BD");
		}

		if (!error) {
			// TODO: validar producto
			response.setStatus(HttpServletResponse.SC_CREATED);
			String jsonResponseBody = new Gson().toJson(producto);

			PrintWriter out = response.getWriter(); // se encarga de poder escribir datos en el body

			// Convertir de Java a JSON

			out.print(jsonResponseBody); // retornamos un array vacio en Json dentro del body
			out.flush(); // termina de escribir dato en response body
		}

	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean enviado = false;

		int id = -1;
		try {
			id = Utilidades.obtenerId(request.getPathInfo());
		} catch (Exception e) {
			LOG.error("La URL esta mal formada");
			LOG.error(e);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("La URL esta mal formada");
			return;
		}

		if (id == -1) {
			LOG.error("Id incorrecto, no se puede realizar la actualizacion");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("Id incorrecto, no se puede realizar la actualizacion");
			return;
		}

		Producto producto = null;
		try {
			producto = requestJSONtoProducto(request, response);
		} catch (Exception e) {
			return;
		}

		try {
			producto = productoDAO.update(id, producto);
		} catch (MySQLIntegrityConstraintViolationException e) {
			enviado = true;
			String mensaje = e.getMessage();
			if (mensaje.contains("Duplicate entry")) {
				LOG.error("Nombre duplicado en la BD");

				enviarMensaje("El nombre esta duplicado en la BD");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
			} else {
				LOG.error("Violacion de las restricciones de integridad");

				enviarMensaje("Violacion de las restricciones de integridad");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.error("No lo puede actualizar");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("No lo puede actualizar");
			return;
		}

		if (!enviado) {
			response.setStatus(HttpServletResponse.SC_OK);
			String jsonResponseBody = new Gson().toJson(producto);
			out.print(jsonResponseBody);
			out.flush();
		}

	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int id = -1;
		boolean enviado = false;
		Producto producto = null;

		try {
			id = Utilidades.obtenerId(request.getPathInfo());
		} catch (Exception e) {
			LOG.error("URL mal formada");
			enviarMensaje("URL mal formada");
			enviado = true;
		}

		if (!enviado && id == -1) {
			LOG.error("URL incorrecta");
			enviarMensaje("URL incorrecta");
			enviado = true;
		}

		if (!enviado) {
			try {
				producto = productoDAO.delete(id);
				response.setStatus(HttpServletResponse.SC_OK);
				String jsonResponseBody = new Gson().toJson(producto);
				out.print(jsonResponseBody);
				out.flush();

			} catch (Exception e) {
				LOG.error("URL incorrecta");
				enviarMensaje("URL incorrecta");
				enviado = true;
			}
		}

	}

	@Override
	public void destroy() {
		productoDAO = null;
		super.destroy();
	}

	/**
	 * Mete en el body un mensaje para dar una explicacion mas extensa
	 *
	 * @param mensaje: mensaje que se enviará en el body
	 */
	private void enviarMensaje(String mensaje) {

		String jsonResponseBody = new Gson().toJson(new ResponseMensaje(mensaje));
		out.print(jsonResponseBody);
		out.flush();
	}

	/**
	 * Facilita el envio de mensajes creando y estableciendo valores.
	 *
	 * @param statusCode
	 * @param mensaje
	 */
	private void enviarMensaje(int statusCode, String mensaje) {
		responseStatus = statusCode;
		responseBody = new ResponseMensaje(mensaje);
	}

	private void setRespose(int statusCode, Object objeto) {
		responseStatus = statusCode;
		responseBody = objeto;
	}

	/**
	 * Intenta obtener un producto de la request body
	 *
	 * @param request
	 * @param response
	 * @return un producto si puede parsearlo
	 * @throws Exception: si no puede parsear el producto
	 */
	private Producto requestJSONtoProducto(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// convertir json del request body a Objeto
		BufferedReader reader = request.getReader();
		Gson gson = new Gson();
		Producto producto = null;
		try {
			producto = gson.fromJson(reader, Producto.class);
		} catch (JsonSyntaxException e) {
			LOG.error("La sintaxis del objeto JSON recibido es incorrecta");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("La sintaxis del JSON enviado es incorrecta");
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("El formato de un numero pasado es incorrecto");
			LOG.error(e);
		} catch (Exception e) {
			LOG.error(e);
		}
		LOG.debug(" Json convertido a Objeto: " + producto);

		if (producto == null) {
			throw new Exception();
		}
		return producto;
	}

}
