package br.com.covid.pareamento;

import static br.com.covid.util.StringUtil.normalizarString;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class PareamentoPorSexoRedome {
	
	private static int TAMANHO_PAREAMENTO = 3; 
	
	public static void main(String[] args) throws IOException {
		List<String[]> casos = carregarRegistros("src/main/resources/csv/casos.csv");
	
		String[] cabecalhoCasos = casos.get(0);
		int indiceIdadeCasos = buscarPosicao("idade", cabecalhoCasos);
		int indiceSexoRedomeCasos = buscarPosicao("sexoRedome", cabecalhoCasos);
		
		casos = casos.stream()
				     .skip(1)
				     .collect(Collectors.toList());
		System.out.println("Número de casos: " + casos.size());
		
		List<String[]> casosFiltrados = filtrarPorFaixaEtaria(casos, 30, 40, indiceIdadeCasos);
		System.out.println("Número de casos filtrados por faixa etária de 30 até 40 anos: " + casosFiltrados.size());
		
		List<String[]> controles = carregarRegistros("src/main/resources/csv/controles.csv");
		
		String[] cabecalhoControles = controles.get(0);
		int indiceIdadeControles = buscarPosicao("idade", cabecalhoControles);
		int indiceSexoRedomeControles = buscarPosicao("sexoRedome", cabecalhoControles);
		int indiceTipoTesteControles = buscarPosicao("tipoTeste", cabecalhoControles);
		int indiceResultadoTesteControles = buscarPosicao("resultadoTeste", cabecalhoControles);
		int indiceObservacaoUsoControles = buscarPosicao("observacaoUso", cabecalhoControles);
		
		controles = controles.stream()
			                 .skip(1)
			                 .collect(Collectors.toList());
		System.out.println("Número de controles: " + controles.size());
					
		for (String[] casoFiltrado : casosFiltrados) {	
			List<String[]> controlesFiltrados = filtrarControlesNaoUsados(controles, indiceObservacaoUsoControles);
			System.out.println("Número de casos filtrados não usados: " + controlesFiltrados.size());	
			
			controlesFiltrados = filtrarPorFaixaEtaria(controlesFiltrados, 30, 40, indiceIdadeControles);
			System.out.println("Número de casos filtrados por faixa etária de 30 até 40 anos: " + controlesFiltrados.size());	
			
			controlesFiltrados = filtrarPorSexoRedome(controlesFiltrados, casoFiltrado[indiceSexoRedomeCasos], indiceSexoRedomeControles);
			System.out.println("Número de controles filtrados por sexoRedome: " + controlesFiltrados.size());		
						
			controlesFiltrados = filtrarPorTipoTeste(controlesFiltrados, indiceTipoTesteControles);
			System.out.println("Número de controles filtrados por tipoTeste: " + controlesFiltrados.size());	
			
			List<String[]> controlesPositivosFiltradosCasoFiltrado = marcarRegistrosPositivos(controlesFiltrados, TAMANHO_PAREAMENTO, indiceResultadoTesteControles, indiceObservacaoUsoControles);								
			List<String[]> controlesNegativosFiltradosCasoFiltrado = marcarRegistrosNegativos(controlesFiltrados, TAMANHO_PAREAMENTO, indiceResultadoTesteControles, indiceObservacaoUsoControles);
		}
		
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
	
	private static List<String[]> filtrarControlesNaoUsados(List<String[]> registros, int indiceObservacaoUsoControles) {
		return registros.stream()
				.filter(r -> r[indiceObservacaoUsoControles] == null || r[indiceObservacaoUsoControles].equals(""))
				.collect(Collectors.toList());
	}
	
	private static List<String[]> filtrarPorFaixaEtaria(List<String[]> registros, int idadeMinima, int idadeMaxima, int indiceIdade) {
		return registros.stream()
				        .filter(r -> Integer.parseInt(r[indiceIdade]) >= idadeMinima 
				                         && Integer.parseInt(r[indiceIdade]) <= idadeMaxima)
				        .collect(Collectors.toList());
	}
	
	private static List<String[]> filtrarPorSexoRedome(List<String[]> registros, String sexoRedome, int indiceSexoRedome) {
		return registros.stream()
				        .filter(r -> normalizarString(r[indiceSexoRedome]).equals(normalizarString(sexoRedome)))
				        .collect(Collectors.toList());
	}
	
	private static List<String[]> filtrarPorTipoTeste(List<String[]> registros, int indiceTipoTeste) {
		List<String[]> registrosRTPCR = registros.stream()
			                                     .filter(r -> normalizarString(r[indiceTipoTeste]).equals(normalizarString("RT-PCR")))
			                                     .collect(Collectors.toList());
		
		List<String[]> registrosAntigeno = registros.stream()
												    .filter(r -> normalizarString(r[indiceTipoTeste]).equals(normalizarString("TESTE RÁPIDO - ANTÍGENO")))
                                                    .collect(Collectors.toList());
		
		List<String[]> registrosEnzimaElisaIgm = registros.stream()
												          .filter(r -> normalizarString(r[indiceTipoTeste]).equals(normalizarString("Enzimaimunoensaio - ELISA IgM")))
								                          .collect(Collectors.toList());
		
		List<String[]> registrosOutros = new ArrayList<String[]>(registros);
		registrosOutros.removeAll(registrosRTPCR);
		registrosOutros.removeAll(registrosAntigeno);
		registrosOutros.removeAll(registrosEnzimaElisaIgm);
		
		List<String[]> registrosFiltradosPorTipoTeste = new ArrayList<String[]>();
		registrosFiltradosPorTipoTeste.addAll(registrosRTPCR);
		registrosFiltradosPorTipoTeste.addAll(registrosAntigeno);
		registrosFiltradosPorTipoTeste.addAll(registrosEnzimaElisaIgm);
		registrosFiltradosPorTipoTeste.addAll(registrosOutros);
		
		return registrosFiltradosPorTipoTeste;
	}
	
	private static List<String[]> filtrarPorResultadoTeste(List<String[]> registros, String resultadoTeste, int indiceResultadoTeste) {
		return registros.stream()
				        .filter(r -> normalizarString(r[indiceResultadoTeste]).equals(normalizarString(resultadoTeste)))
				        .collect(Collectors.toList());
	}
	
	private static List<String[]> marcarRegistrosPositivos(List<String[]> registros, int qtd, 
			                                               int indiceResultadoTeste, int indiceObservacaoUso) {
		List<String[]> registrosResultadoPositivo = filtrarPorResultadoTeste(registros, "Positivo", indiceResultadoTeste);
		System.out.println("Número de controles filtrados com resultadoTeste Positivo: " + registrosResultadoPositivo.size());
		
		registrosResultadoPositivo.stream()
                                  .limit(qtd)
	                              .forEach(r -> r[indiceObservacaoUso] = "usado");

		List<String[]> registrosResultadoPositivoUsados = registrosResultadoPositivo.stream()
				                                                                    .filter(r -> r[indiceObservacaoUso] != null 
				                                                                              && !r[indiceObservacaoUso].equals(""))
				                                                                    .collect(Collectors.toList());

		if (registrosResultadoPositivoUsados.size() > 0) {
			System.out.println("Foram marcados " + registrosResultadoPositivoUsados.size() + " registros do controle com resultado Positivo");
		}

		return registrosResultadoPositivoUsados;
	}

	private static List<String[]> marcarRegistrosNegativos(List<String[]> registros, int qtd, 
            											   int indiceResultadoTeste, int indiceObservacaoUso) {
		List<String[]> registrosResultadoNegativo = filtrarPorResultadoTeste(registros, "Negativo", indiceResultadoTeste);
		System.out.println("Número de controles filtrados com resultadoTeste Negativo: " + registrosResultadoNegativo.size());
		
		registrosResultadoNegativo.stream()
								  .limit(qtd)
								  .forEach(r -> r[indiceObservacaoUso] = "usado");
		
		List<String[]> registrosResultadoNegativoUsados = registrosResultadoNegativo.stream()
												                                    .filter(r -> r[indiceObservacaoUso] != null 
												                                               && !r[indiceObservacaoUso].equals(""))
												                                    .collect(Collectors.toList());
		
		if (registrosResultadoNegativoUsados.size() > 0) {
		    System.out.println("Foram marcados " + registrosResultadoNegativoUsados.size() + " registros do controle com resultado Negativo");
		}
		
		return registrosResultadoNegativoUsados;
	}
}
