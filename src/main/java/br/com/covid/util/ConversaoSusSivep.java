package br.com.covid.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConversaoSusSivep {
	
	public static String converterDataSusParaSivep(String dataSus) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		
	    Date dataNotificacao = sdf1.parse(dataSus);
			
	    return sdf2.format(dataNotificacao);
	}

}
