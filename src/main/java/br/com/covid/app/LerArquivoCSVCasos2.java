package br.com.covid.app;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class LerArquivoCSVCasos2 {
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Digite o nome do arquivo de casos: ");
		String nomeArquivo = scanner.nextLine();
		
		Reader reader = Files.newBufferedReader(Paths.get(nomeArquivo));
		CSVReader csvReader = new CSVReaderBuilder(reader).build();
		
		List<String[]> casos = csvReader.readAll();
		for(String[] caso: casos) {
			System.out.println(caso);
		}
		
		scanner.close();
	}
}
