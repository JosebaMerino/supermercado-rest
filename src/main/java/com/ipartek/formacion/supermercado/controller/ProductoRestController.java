package com.ipartek.formacion.supermercado.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ipartek.formacion.supermercado.modelo.dao.ProductoDAO;
import com.ipartek.formacion.supermercado.modelo.pojo.Producto;
import com.ipartek.formacion.supermercado.pojo.ResponseMensaje;
import com.ipartek.formacion.supermercado.utils.Utilidades;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * Servlet implementation class ProductoRestController
 */
@WebServlet({ "/producto/*" })
public class ProductoRestController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LogManager.getLogger(ProductoRestController.class);

	PrintWriter out = null;

	private ProductoDAO productoDAO;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		productoDAO = ProductoDAO.getInstance();
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json"); // por defecto => text/html; charset=UTF-8
		response.setCharacterEncoding("UTF-8");
		out = response.getWriter();
		super.service(request, response); // Llama a doGet, o doPost o doPut o doDelete
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.trace("Peticion GET");

		String jsonResponseBody = "";

		String[] pathSplitted = Utilidades.getPathSplitted(request.getPathInfo());

		if (pathSplitted.length == 0) {
			// obtener todos los productos que no esten dados de baja de la base de datos

			List<Producto> lista = productoDAO.getAll();
			if (!lista.isEmpty()) {

				// response body

				// Convertir de Java a JSON

				jsonResponseBody = new Gson().toJson(lista); // conversion de java a json
				out.print(jsonResponseBody); // "imprimimos" un JSON
				out.flush(); // termina de escribir dato en response body

				// response status code
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				LOG.trace("La lista de productos esta vacia");
			}
		} else if (pathSplitted.length == 1) {
			// Obtener un producto en concreto
			int id = -1;
			try {
				id = Utilidades.obtenerId(request.getPathInfo());
			} catch (Exception e) {
				LOG.trace("La url esta mal formada");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				enviarMensaje("La url esta mal formada");
				return;
			}
			if (id == -1) {
				LOG.trace("El dato pasado tiene mal formato");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				enviarMensaje("El dato pasado tiene mal formato");
				return;
			}
			Producto producto = productoDAO.getById(id);
			if (producto != null) {
				jsonResponseBody = "";
				if (producto != null) {
					jsonResponseBody = new Gson().toJson(producto);
				}

				// Convertir de Java a JSON

				out.print(jsonResponseBody); // retornamos un array vacio en Json dentro del body
				out.flush(); // termina de escribir dato en response body

				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				enviarMensaje("No se ha encontrado el producto con id: " + id);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("La solicitud realizada tiene parametros de más");

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
			LOG.error("Id negativo, no se puede realizar la actualizacion");

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			enviarMensaje("Id negativo, no se puede realizar la actualizacion");
			return;
		}

		Producto producto = null;
		try {
			producto = requestJSONtoProducto(request, response);
		} catch (Exception e) {
			return;
		}

		try {
			productoDAO.update(id, producto);
		} catch (MySQLIntegrityConstraintViolationException e) {
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
			enviarMensaje("Id negativo, no se puede realizar la actualizacion");
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);


	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
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
