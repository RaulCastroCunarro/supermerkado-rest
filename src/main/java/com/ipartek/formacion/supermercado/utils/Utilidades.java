package com.ipartek.formacion.supermercado.utils;

public class Utilidades {
	
	private static final int INCORRECTO = -1;
	private static final String URLError = "URL mal formada ";
	
	public static int obtenerId(String pathInfo) throws Exception{
		int resultado = INCORRECTO;
		if(pathInfo != null) {
			if (pathInfo.equals("/")){
				resultado = INCORRECTO;
			}else {
				String[] partes = pathInfo.split("/");
				if (partes.length != 2) {
					throw new Exception(URLError + pathInfo);
				}else {
					try {
						resultado = Integer.parseInt(partes[1]);
				    } catch (NumberFormatException nfe) {
				    	throw new Exception(URLError + "el id no es numérico " + pathInfo);
				    }
				}
			}
		}
		
		return resultado;
	}

}
