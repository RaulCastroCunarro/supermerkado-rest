package com.ipartek.formacion.supermercado.utils;

import java.util.ArrayList;

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
	
	public static int contarPalabras (String frase) {
		int resultado = 0;
		String[] partes = null;
		ArrayList<String> palabras = new ArrayList<String>();
		
		if (frase != null && frase.trim().length() != 0) {
			partes = frase.split("[\\W\\_]");
			for (int i = 0; i < partes.length; i++) {
				if (partes[i].length() > 0) {
					palabras.add(partes[i]);
				}
			}
			resultado = palabras.size();
		}
		
		return resultado;
	}

}
