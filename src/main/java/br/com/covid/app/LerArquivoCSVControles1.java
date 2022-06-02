package br.com.covid.app;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class LerArquivoCSVControles1 {
	
	public static void main(String[] args) throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get("src/main/resources/csv/controles.csv"));
		CSVReader csvReader = new CSVReaderBuilder(reader).build();
		
		List<String[]> controles = csvReader.readAll();
		for(String[] controle: controles) {
			System.out.println(controle);
		}
	}

}
