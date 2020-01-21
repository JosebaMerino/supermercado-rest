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
import com.ipartek.formacion.supermercado.modelo.dao.ProductoDAO;
import com.ipartek.formacion.supermercado.modelo.pojo.Producto;

/**
 * Servlet implementation class ProductoRestController
 */
@WebServlet({ "/producto/*" })
public class ProductoRestController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LogManager.getLogger(ProductoRestController.class);

	private ProductoDAO productoDAO;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		productoDAO = ProductoDAO.getInstance();
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		super.service(request, response); // Llama a doGet, o doPost o doPut o doDelete
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.trace("Peticion GET");
		String[] pathSplitted = getPathSplitted(request);

		if(pathSplitted.length == 0) {
			// obtener todos los productos que no esten dados de baja de la base de datos

			List<Producto> lista = productoDAO.getAll();
			if(!lista.isEmpty()) {
				// prepara la response
				response.setContentType("application/json"); // por defecto => text/html; charset=UTF-8
				response.setCharacterEncoding("UTF-8");

				// response body
				PrintWriter out = response.getWriter(); // se encarga de poder escribir datos en el body

				// Convertir de Java a JSON

				String jsonResponseBody = new Gson().toJson(lista); // conversion de java a json
				out.print(jsonResponseBody); // "imprimimos" un JSON
				out.flush();				// termina de escribir dato en response body

				// response status code
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				LOG.trace("La lista de productos esta vacia");
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		} else if(pathSplitted.length == 1) {
			// Obtener un producto en concreto

			response.setContentType("application/json"); // por defecto => text/html; charset=UTF-8
			response.setCharacterEncoding("UTF-8");
			String idStr = pathSplitted[0];
			int id = 0;
			if(idStr.matches("^\\d+$")) {
				id = Integer.parseInt(idStr);
			}
			Producto producto = productoDAO.getById(id);
			String jsonResponseBody = "";
			if(producto != null) {
				jsonResponseBody = new Gson().toJson(producto);
			}

			PrintWriter out = response.getWriter(); // se encarga de poder escribir datos en el body

			// Convertir de Java a JSON

			out.print(jsonResponseBody); // retornamos un array vacio en Json dentro del body
			out.flush();				// termina de escribir dato en response body

			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private String[] getPathSplitted(HttpServletRequest request) {
		String[] resul = null;
		String pathInfo = request.getPathInfo();
		String[] emptyArray = new String[0];
		if(pathInfo == null) {
			resul = emptyArray;
		} else if(pathInfo.length() == 1) {
			resul = emptyArray;
		} else {
			pathInfo = request.getPathInfo().substring(1);
			String[] splitted = pathInfo.split("/");
			resul = splitted;
		}

		return resul;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("POST crear recurso");

		// convertir json del request body a Objeto
		BufferedReader reader = request.getReader();
		Gson gson = new Gson();
		Producto producto = gson.fromJson(reader, Producto.class);

		LOG.debug(" Json convertido a Objeto: " + producto);

		response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
	}

	@Override
	public void destroy() {
		productoDAO = null;
		super.destroy();
	}

}
