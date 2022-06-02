package br.com.covid.app;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class LerArquivoCSVCasos1 {
	
	public static void main(String[] args) throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get("src/main/resources/csv/casos.csv"));
		CSVReader csvReader = new CSVReaderBuilder(reader).build();
		
		List<String[]> casos = csvReader.readAll();
		for(String[] caso: casos) {
			System.out.println(caso);
		}
	}

}
