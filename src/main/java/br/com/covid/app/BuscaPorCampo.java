package br.com.covid.app;

import static br.com.covid.util.StringUtil.normalizarString;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class BuscaPorCampo {
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Digite o nome do arquivo de casos: ");
		String nomeArquivoCasos = scanner.nextLine();
		
		List<String[]> casos = carregarRegistros(nomeArquivoCasos);
		System.out.println("Número de casos: " + casos.size());
		
		String[] cabecalhoCasos = casos.get(0);
		
		System.out.print("Digite o nome do campo dos casos: ");
		String campo = scanner.nextLine();
		
		Integer indiceCampoCasos = buscarPosicao(campo, cabecalhoCasos);
		System.out.println("Posição do campo " + campo + " dos casos: " + indiceCampoCasos);
		
		System.out.print("Digite o número do registro do arquivo de casos: ");
		int indiceCaso = Integer.parseInt(scanner.nextLine());
		
		String[] caso = casos.get(indiceCaso);
		System.out.println("Valor do campo " + campo + " do caso " + indiceCaso + ": " + caso[indiceCampoCasos]);
		
		System.out.print("Digite o nome do arquivo de controles: ");
		String nomeArquivoControles = scanner.nextLine();
		
		List<String[]> controles = carregarRegistros(nomeArquivoControles);
		
        String[] cabecalhoControles = controles.get(0);	
        
		int indiceCampoControles = buscarPosicao(campo, cabecalhoControles);
		System.out.println("Posição do campo " + campo + " dos controles: " + indiceCampoControles);
		
		int qtd = controles.stream()
		                   .filter(controle -> normalizarString(controle[indiceCampoControles]).equals(normalizarString(caso[indiceCampoCasos])))
		                   .collect(Collectors.toList())
		                   .size();
	 
        System.out.println("Número de controles com " + campo + " = " + caso[indiceCampoCasos] + ": " + qtd);
		
		scanner.close();
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
