package br.com.covid.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class DataUtil {
	
	public static Date alterarDiasEmData(final Date data, int qtdDias) {
	    Date newDate = new Date(data.getTime());

	    GregorianCalendar calendar = new GregorianCalendar();
	    calendar.setTime(newDate);
	    calendar.add(Calendar.DATE, qtdDias);
	    newDate.setTime(calendar.getTime().getTime());

	    return newDate;
	}
	
	public static long obterDiferencaDias(final Date data1, final Date data2) {
		long diffInMillies = Math.abs(data2.getTime() - data1.getTime());
	    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    return diff;
	}
	
	public static boolean dataEstaEmIntervalo(Date data1, final Date data2, final Date data){
	    return !(data.before(data1) || data.after(data2));
	}

}
