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
import com.ipartek.formacion.supermercado.modelo.dao.CategoriaDAO;
import com.ipartek.formacion.supermercado.modelo.dao.ProductoDAO;
import com.ipartek.formacion.supermercado.modelo.dao.UsuarioDAO;
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
	private UsuarioDAO usuarioDao;
	private CategoriaDAO categoriaDao;

	int idProducto = 0;

	int statusCode = 0;

	String pathInfo = "";

	String jsonResponseBody = "";
	ArrayList<String> errores = new ArrayList<>();

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		productoDao = ProductoDAO.getInstance();
		usuarioDao = UsuarioDAO.getInstance();
		categoriaDao = CategoriaDAO.getInstance();
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		productoDao = null;
		usuarioDao = null;
		categoriaDao = null;
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

		jsonResponseBody = null;
		pathInfo = request.getPathInfo();
		
		//TODO Ponerlo en el filtro
		response.addHeader("Access-Control-Allow-Origin:", "192.168.0.1");
		response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        
		try {
			idProducto = Utilidades.obtenerId(pathInfo);

			super.service(request, response); // llama a doGet, doPost, doPut, doDelete

		} catch (Exception e) {
			statusCode = HttpServletResponse.SC_BAD_REQUEST;
			errores.add("400");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje(e.getMessage(), errores));
		} finally {
			response.setStatus(statusCode);
			if (jsonResponseBody != null) {
				PrintWriter out = response.getWriter();
				out.print(jsonResponseBody.toString());
				out.flush();
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOG.trace("peticion GET");

		try {
			if (idProducto == -1) {
				String orden = "ASC";
				String columna = "nombre";
				
				if (request.getParameter("orden") != null) {
					orden = request.getParameter("orden");
				}
				if (request.getParameter("columna") != null) {
					columna = request.getParameter("columna");
				}
				
				// obtenr productos de la BD
				ArrayList<Producto> lista = null;
				if (request.getParameter("orden") != null || request.getParameter("columna") != null){
					lista = (ArrayList<Producto>) productoDao.getAllOrdenado(columna, orden);
				}else {
					lista = (ArrayList<Producto>) productoDao.getAll();
				}
				jsonResponseBody = new Gson().toJson(lista);
				// response status code
				statusCode = HttpServletResponse.SC_OK;
			} else {
				// obtenr producto de la BD by Id
				Producto listaId = (Producto) productoDao.getById(idProducto);
				if (listaId == null) {
					statusCode = HttpServletResponse.SC_NOT_FOUND;
					errores.add("404");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("El recurso no existe", errores));
				} else {
					LOG.debug(" Json convertido a Objeto: " + listaId.toString());
					statusCode = HttpServletResponse.SC_CREATED;
					errores.add("200");
					jsonResponseBody = listaId.toString();
				}
			}
		} catch (Exception e) {
			statusCode = HttpServletResponse.SC_NO_CONTENT;
			errores.add("204");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Recurso no encontrado", errores));
		}
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
			statusCode = HttpServletResponse.SC_CREATED;
			errores.add("201");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Creado con éxito", errores));
		} catch (SQLException e) {
			e.printStackTrace();
			String mensajeError = e.getMessage();
			if (mensajeError.contains("Duplicate") && mensajeError.contains("'id'")) {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Codigo Id Duplicado", errores));
			} else if (mensajeError.contains("Duplicate") && mensajeError.contains("'nombre'")) {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Nombre Duplicado", errores));
			} else {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Datos incorrectos", errores));
			}
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOG.debug("PUT actualizar recurso");

		// convertir json del request body a Objeto
		BufferedReader reader = request.getReader();
		Gson gson = new Gson();
		Producto producto = gson.fromJson(reader, Producto.class);

		LOG.debug(" Json convertido a Objeto: " + producto.toString());

		try {
			Producto productoOriginal = (Producto) productoDao.getById(producto.getId());
			if (productoOriginal == null) {
				statusCode = HttpServletResponse.SC_NOT_FOUND;
				errores.add("404");
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("El recurso no existe", errores));
			} else {
				if (producto.getNombre() == "") {
					producto.setNombre(productoOriginal.getNombre());
				}
				if (producto.getPrecio() == 0) {
					producto.setPrecio(productoOriginal.getPrecio());
				}
				if (producto.getImagen() == "https://image.flaticon.com/icons/png/512/372/372627.png") {
					producto.setImagen(productoOriginal.getImagen());
				}
				if (producto.getDescripcion() == "") {
					producto.setDescripcion(productoOriginal.getDescripcion());
				}
				if (producto.getDescuento() == 0) {
					producto.setDescuento(productoOriginal.getDescuento());
				}
				if (producto.getFechaCreacion() == null) {
					producto.setFechaCreacion(productoOriginal.getFechaCreacion());
				}
				if (producto.getFechaModificacion() == null) {
					producto.setFechaModificacion(productoOriginal.getFechaModificacion());
				}
				if (producto.getFechaEliminacion() == null) {
					producto.setFechaEliminacion(productoOriginal.getFechaEliminacion());
				}
				if (producto.getUsuario().getId() == 0) {
					producto.setUsuario(productoOriginal.getUsuario());
				} else {
					producto.setUsuario(usuarioDao.getById(producto.getUsuario().getId()));
				}
				if (producto.getCategoria().getId() == 0) {
					producto.setCategoria(productoOriginal.getCategoria());
				} else {
					producto.setCategoria(categoriaDao.getById(producto.getCategoria().getId()));
				}
				if (producto.getValidado() == 0) {
					producto.setValidado(productoOriginal.getValidado());
				}
				Producto productoInsertado = (Producto) productoDao.update(producto.getId(), producto);
				LOG.debug(" Json convertido a Objeto: " + productoInsertado.toString());
				statusCode = HttpServletResponse.SC_OK;
				errores.add("200");
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Creado con éxito", errores));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String mensajeError = e.getMessage();
			if (mensajeError.contains("Duplicate") && mensajeError.contains("'id'")) {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Codigo Id Duplicado", errores));
			} else if (mensajeError.contains("Duplicate") && mensajeError.contains("'nombre'")) {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Nombre Duplicado", errores));
			} else {
				errores.add("409");
				statusCode = HttpServletResponse.SC_CONFLICT;
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Datos incorrectos", errores));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.debug("DELETE borrar recurso");

		try {
			if (idProducto != -1) {
				Producto productoEliminado = (Producto) productoDao.deleteLogico(idProducto);
				if (productoEliminado == null) {
					statusCode = HttpServletResponse.SC_NOT_FOUND;
					errores.add("404");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("El recurso no existe", errores));
				} else {
					LOG.debug(" Json convertido a Objeto: " + productoEliminado.toString());
					statusCode = HttpServletResponse.SC_CREATED;
					errores.add("200");
					jsonResponseBody = new Gson().toJson(new ResponseMensaje("Eliminado con éxito", errores));
				}
			} else {
				statusCode = HttpServletResponse.SC_CONFLICT;
				errores.add("409");
				jsonResponseBody = new Gson().toJson(new ResponseMensaje("Datos Incorrectos", errores));
			}
		} catch (Exception e) {
			statusCode = HttpServletResponse.SC_NO_CONTENT;
			errores.add("204");
			jsonResponseBody = new Gson().toJson(new ResponseMensaje("Recurso no encontrado", errores));
			LOG.trace(jsonResponseBody);
		}
	}

}
