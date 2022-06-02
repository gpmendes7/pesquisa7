package br.com.covid.app;

import static br.com.covid.util.StringUtil.normalizarString;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class BuscaPorSexoRedome {
	
	public static void main(String[] args) throws IOException {
		List<String[]> casos = carregarRegistros("src/main/resources/csv/casos.csv");
		
		String[] cabecalhoCasos = casos.get(0);
		int indiceSexoRedomeCaso = buscarPosicao("sexoRedome", cabecalhoCasos);
		System.out.println("Posição do campo sexoRedome dos casos: " + indiceSexoRedomeCaso);
		
		String[] caso = casos.get(1);
		System.out.println("Valor do campo sexoRedome do caso 1: " + caso[indiceSexoRedomeCaso]);
		
		List<String[]> controles = carregarRegistros("src/main/resources/csv/controles.csv");
        String[] cabecalhoControles = controles.get(0);	
		int indiceSexoRedomeControle = buscarPosicao("sexoRedome", cabecalhoControles);
		System.out.println("Posição do campo sexoRedome dos controles: " + indiceSexoRedomeControle);
		
		int qtd = controles.stream()
				           .filter(controle -> normalizarString(controle[indiceSexoRedomeControle]).equals(normalizarString(caso[indiceSexoRedomeCaso])))
				           .collect(Collectors.toList())
				           .size();
			
		System.out.println("Número de controles com sexoRedome = " + caso[indiceSexoRedomeCaso] + ": " + qtd);
	}
	
	private static List<String[]> carregarRegistros(String nomeArquivo) throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get(nomeArquivo));
		CSVReader csvReader = new CSVReaderBuilder(reader).build();
		
		return csvReader.readAll();
	}
	
	private static int buscarPosicao(String campo, String[] cabecalho) {
		int i = 0;
		boolean campoExiste = false;
		
		for(String campoCabecalho: cabecalho) {  
			if(campoCabecalho.equals(campo)) {  
				 campoExiste = true;
				 break;
			}
			
		    i++;
		}
		
		if(!campoExiste) {
			i = -1;
		}
		
		return i;
	}

}
