package com.ipartek.formacion.supermercado.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

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
import com.ipartek.formacion.supermercado.pojo.ResponseMensaje;
import com.ipartek.formacion.supermercado.utils.Utilidades;

/**
 * Servlet implementation class ProductoRestController
 */
@WebServlet({ "/producto/*", "/producto" })
public class ProductoRestController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LogManager.getLogger(ProductoRestController.class);
	private ProductoDAO productoDao;
	
	String pathInfo = "";

	String jsonResponseBody = "";
	ArrayList<String> errores = new ArrayList<>();

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		productoDao = ProductoDAO.getInstance();
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		productoDao = null;
	}

	/**
	 * @throws IOException
	 * @throws ServletException
	 * @see Servlet#destroy()
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// prepara la response
		response.setContentType("application/json"); // por defecto => text/html;charset=UTF-8
		response.setCharacterEncoding("utf-8");
		
		pathInfo = request.getPathInfo();

		super.service(request, response); // llama a doGet, doPost, doPut, doDelete
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOG.trace("peticion GET");

		int numId = 0;
		try {
			numId = Utilidades.obtenerId(pathInfo);
			if (numId == -1) {
				// obtenr productos de la BD
				ArrayList<Producto> lista = (ArrayList<Producto>) productoDao.getAll();
				jsonResponseBody = new Gson().toJson(lista);
			} else {
				// obtenr producto de la BD by Id
				Producto listaId = (Producto) productoDao.getById(numId);
				if (listaId == null) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					errores.add("404");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("El recurso no existe", errores));
				} else {
					LOG.debug(" Json convertido a Objeto: " + listaId.toString());
					response.setStatus(HttpServletResponse.SC_CREATED);
					errores.add("200");
					jsonResponseBody = listaId.toString();
				}
			}
			// response status code
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			errores.add("204");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Recurso no encontrado", errores));
		}

		PrintWriter out = response.getWriter();
		out.print(jsonResponseBody.toString());
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOG.debug("POST crear recurso");

		// convertir json del request body a Objeto
		BufferedReader reader = request.getReader();
		Gson gson = new Gson();
		Producto producto = gson.fromJson(reader, Producto.class);

		LOG.debug(" Json convertido a Objeto: " + producto.toString());

		try {
			Producto productoInsertado = (Producto) productoDao.create(producto);
			LOG.debug(" Json convertido a Objeto: " + productoInsertado.toString());
			response.setStatus(HttpServletResponse.SC_CREATED);
			errores.add("201");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Creado con éxito", errores));
		} catch (SQLException e) {
			e.printStackTrace();
			String mensajeError = e.getMessage();
			if (mensajeError.contains("Duplicate") && mensajeError.contains("'id'")) {
				errores.add("409");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Codigo Id Duplicado", errores));
			} else if (mensajeError.contains("Duplicate") && mensajeError.contains("'nombre'")) {
				errores.add("409");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Nombre Duplicado", errores));
			} else {
				errores.add("409");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Datos incorrectos", errores));
			}
		}

		PrintWriter out = response.getWriter();
		out.print(jsonResponseBody.toString());
		out.flush();
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPut(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.debug("DELETE borrar recurso");

		int numId = 0;
		try {
			numId = Utilidades.obtenerId(pathInfo);
			if (numId != -1) {
				Producto productoEliminado = (Producto) productoDao.delete(numId);
				if (productoEliminado == null) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					errores.add("404");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("El recurso no existe", errores));
				} else {
					LOG.debug(" Json convertido a Objeto: " + productoEliminado.toString());
					response.setStatus(HttpServletResponse.SC_CREATED);
					errores.add("200");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("Eliminado con éxito", errores));
				}
			} else {
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				errores.add("409");
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Datos Incorrectos", errores));
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			errores.add("204");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Recurso no encontrado", errores));
			LOG.trace(jsonResponseBody);
		}

		PrintWriter out = response.getWriter();
		out.print(jsonResponseBody.toString());
		out.flush();
	}

}
