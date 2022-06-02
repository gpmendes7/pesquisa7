package br.com.covid.util;

import java.text.Normalizer;

public class StringUtil {
	
	public static String removeAcentos(String string) {
		return Normalizer.normalize(string, Normalizer.Form.NFD)
						 .replaceAll("[^\\p{ASCII}]", "");
	}
	
	public static String normalizarString(String string) {
		if(string != null)
		   return removeAcentos(string).toUpperCase().trim();
		else
			return "";
	}


}
