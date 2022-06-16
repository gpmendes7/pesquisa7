package br.com.covid.pareamento;

import static br.com.covid.util.DataUtil.alterarDiasEmData;
import static br.com.covid.util.DataUtil.dataEstaEmIntervalo;
import static br.com.covid.util.StringUtil.normalizarString;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import br.com.covid.util.ConversaoSusSivep;

public class PareamentoPorCampo {
	
	private static int TAMANHO_PAREAMENTO = 3; 
	
	public static void main(String[] args) throws IOException, ParseException {
		List<String[]> casos = carregarRegistros("src/main/resources/csv/casos.csv");
		
		System.out.println("casos.size(): " + casos.size());
		
		String[] cabecalhoCasos = casos.get(0);
		
		casos = casos.stream()
			         .skip(1)
			         .collect(Collectors.toList());
		
		List<String[]> casosFiltrados = new ArrayList<>(casos);
		
		Scanner scanner = new Scanner(System.in);
		
		int indiceIdadeCasos = -1;
		String campoFaixaEtaria = null;
				
		System.out.println("Aplicar filtro por faixa etária (1-Sim ou 2-Não) ? ");
		int opcaoFaixaEtaria = Integer.parseInt(scanner.nextLine());
		
		int idadeMinima = -1;
		int idadeMaxima = -1;
		
		if(opcaoFaixaEtaria == 1) {
			System.out.println("Optou por filtro de faixa etária!");
			System.out.println("Nome do campo usado por faixa etária: ");
			campoFaixaEtaria = scanner.nextLine(); 
			indiceIdadeCasos = buscarPosicao(campoFaixaEtaria, cabecalhoCasos);
			System.out.println("Idade mínima: ");
			idadeMinima = Integer.parseInt(scanner.nextLine());
			System.out.println("Idade máxima: ");
			idadeMaxima = Integer.parseInt(scanner.nextLine());
			casosFiltrados = filtrarPorFaixaEtaria(casos, idadeMinima, idadeMaxima, indiceIdadeCasos);
		} else {
			System.out.println("Não optou por filtro de faixa etária!");
		}
		
		System.out.println("casosFiltrados.size(): " + casosFiltrados.size());
				
		List<Filtro> filtros = new ArrayList<Filtro>();
		
		for (String campo : cabecalhoCasos) {
			 System.out.println("Aplicar filtro por " + campo + "? (1-Sim ou 2-Não) ");
			 int opcaoFiltro = Integer.parseInt(scanner.nextLine());
			 if(opcaoFiltro == 1) {
				boolean isData =  campo.contains("data");
				Filtro filtro = new Filtro(campo, isData);
				filtro.setNumeroSemanas(1);
				filtros.add(filtro);
			 }
		}
		
		List<String[]> controles = carregarRegistros("src/main/resources/csv/controles.csv");
		
		String[] cabecalhoControles = controles.get(0);
		int indiceIdadeControles = buscarPosicao(campoFaixaEtaria, cabecalhoControles);
		int indiceObservacaoUsoControles = buscarPosicao("observacaoUso", cabecalhoControles);
		
		controles = controles.stream()
                             .skip(1)
                             .collect(Collectors.toList());
		
		System.out.println("Aplicar filtro por valores positivos e negativos (1-Sim ou 2-Não) ? ");
		int opcaoPositivosENegativos = Integer.parseInt(scanner.nextLine());
		
		int indiceCampoPositivosENegativos = -1;
		String campoPositivosENegativos = null;
		List<String> valoresPositivosENegativos = new ArrayList<String>();
		Map<String, String> mapaPositivosENegativos = new HashMap<>();
		
		if(opcaoPositivosENegativos == 1) {
			System.out.println("Optou por filtro de valores positivos e negativos!");
			System.out.println("Nome do campo usado por valores positivos e negativos: ");
			campoPositivosENegativos = scanner.nextLine();
			indiceCampoPositivosENegativos = buscarPosicao(campoPositivosENegativos, cabecalhoControles);
		} else {
			System.out.println("Não optou por filtro de valores positivos e negativos!");
		}
		
		int i;
		
		if(indiceCampoPositivosENegativos != -1 && campoPositivosENegativos != null) {
			for (String[] controle : controles) {
				 if(!valoresPositivosENegativos.contains(controle[indiceCampoPositivosENegativos]))
					 valoresPositivosENegativos.add(controle[indiceCampoPositivosENegativos]);
			}
			
			System.out.println("Valores distintos encontrados para o campo " + campoPositivosENegativos + ":");
		    i = 0;
			
			for(String valorPositivosENegativos: valoresPositivosENegativos) {
				System.out.println((i+1) + " - " + valorPositivosENegativos);
				i++;
			}
			
			System.out.println("Qual valor usar como positivo?");
			int opcaoPositivo = Integer.parseInt(scanner.nextLine());
			System.out.println("Valor escolhido para positivo foi " + valoresPositivosENegativos.get(opcaoPositivo-1));
			mapaPositivosENegativos.put("positivo", valoresPositivosENegativos.get(opcaoPositivo-1));
			
			System.out.println("Qual valor usar como negativo?");
			int opcaoNegativo = Integer.parseInt(scanner.nextLine());
			System.out.println("Valor escolhido para negativo foi " + valoresPositivosENegativos.get(opcaoNegativo-1));
			mapaPositivosENegativos.put("negativo", valoresPositivosENegativos.get(opcaoNegativo-1));
		}
		
		System.out.println("Número de filtros aplicados: " + filtros.size());
		
		List<Filtro> filtrosOrdenados = new ArrayList<Filtro>();
		
		i = 0;
		for(Filtro filtro: filtros) {
			System.out.println((i+1) + " - " + filtro.getCampo());
			i++;
		}
		
		System.out.println("Defina uma ordem para aplicação dos filtros!");
		
		i = 0;
		while(i < filtros.size()) {
			System.out.println("Filtro " + (i+1) + ": ");
			int opcaoFiltro = Integer.parseInt(scanner.nextLine());
			filtrosOrdenados.add(i, filtros.get(opcaoFiltro-1));
			i++;
		}
					
		List<String[]> controlesFiltradosCasosFiltrados = new ArrayList<String[]>();
		
		for (String[] casoFiltrado : casosFiltrados) {	
			List<String[]> controlesPositivosFiltradosCasoFiltrado = new ArrayList<String[]>();
			List<String[]> controlesNegativosFiltradosCasoFiltrado = new ArrayList<String[]>();
			List<String[]> controlesFiltradosCasoFiltrado = new ArrayList<String[]>();
			int indiceFiltroDesmarcar = 0;
			
			while((!valoresPositivosENegativos.isEmpty() && 
				  (controlesPositivosFiltradosCasoFiltrado.size() < TAMANHO_PAREAMENTO || controlesNegativosFiltradosCasoFiltrado.size() < TAMANHO_PAREAMENTO))
				  || (valoresPositivosENegativos.isEmpty() && controlesFiltradosCasoFiltrado.size() < TAMANHO_PAREAMENTO)) {
				
					List<String[]> controlesFiltrados = filtrarControlesNaoUsados(controles, indiceObservacaoUsoControles);
					System.out.println("Número de casos filtrados não usados: " + controlesFiltrados.size());
					
					if(idadeMinima != -1 && idadeMaxima != -1) {
						controlesFiltrados = filtrarPorFaixaEtaria(controlesFiltrados, idadeMinima, idadeMaxima, indiceIdadeControles);
					}
					
					for(Filtro filtro: filtrosOrdenados) {
						if(!filtro.isDesmarcado()) {
							if(!filtro.isData()) {
								int indiceCampoCaso = buscarPosicao(filtro.getCampo(), cabecalhoCasos);
								int indiceCampoControle = buscarPosicao(filtro.getCampo(), cabecalhoControles);
								controlesFiltrados = filtrarPorCampo(controlesFiltrados, casoFiltrado[indiceCampoCaso], indiceCampoControle);
								System.out.println("Número de casos filtrados por campo " + filtro.getCampo() + ": " + controlesFiltrados.size());
							} else {
								int indiceDataCaso = buscarPosicao(filtro.getCampo(), cabecalhoCasos);
								int indiceDataControle = buscarPosicao(filtro.getCampo(), cabecalhoControles);
								controlesFiltrados = filtrarPorData(controlesFiltrados, casoFiltrado[indiceDataCaso], indiceDataControle, 1);
								System.out.println("Número de casos filtrados por campo " + filtro.getCampo() + ": " + controlesFiltrados.size());
							}
						}
					}
					
					if(indiceFiltroDesmarcar <= filtrosOrdenados.size()-1) {
						Filtro filtroASerDesmarcado = filtrosOrdenados.get(indiceFiltroDesmarcar);
						if(!filtroASerDesmarcado.isData()) {
							filtroASerDesmarcado.setDesmarcado(true);
							System.out.println("Filtro por campo " + filtroASerDesmarcado.getCampo() + " desmarcado!");
							indiceFiltroDesmarcar++;
						} else {
							if(filtroASerDesmarcado.getNumeroSemanas() < 4) {
								filtroASerDesmarcado.acrescentarSemanas();
								System.out.println("Filtro por campo " + filtroASerDesmarcado.getCampo() + " com numero de semanas aumentado para " + filtroASerDesmarcado.getNumeroSemanas());
							} else {
								filtroASerDesmarcado.setDesmarcado(true);
								System.out.println("Filtro por campo " + filtroASerDesmarcado.getCampo() + " desmarcado!");
								indiceFiltroDesmarcar++;
							}
						}					
					}
					
					if(!valoresPositivosENegativos.isEmpty()) {
						List<String[]> registrosPositivosMarcados = marcarRegistrosPositivos(controlesFiltrados, TAMANHO_PAREAMENTO, mapaPositivosENegativos.get("positivo"), indiceCampoPositivosENegativos, indiceObservacaoUsoControles);
						System.out.println("registrosPositivosMarcados.size(): " + registrosPositivosMarcados.size());
						controlesPositivosFiltradosCasoFiltrado.addAll(registrosPositivosMarcados);
						
						List<String[]> registrosNegativosMarcados = marcarRegistrosNegativos(controlesFiltrados, TAMANHO_PAREAMENTO, mapaPositivosENegativos.get("negativo"), indiceCampoPositivosENegativos, indiceObservacaoUsoControles);
						System.out.println("registrosNegativosMarcados.size(): " + registrosNegativosMarcados.size());
						controlesNegativosFiltradosCasoFiltrado.addAll(registrosNegativosMarcados);									
					} else {
						List<String[]> registrosMarcados = marcarRegistros(controlesFiltrados, TAMANHO_PAREAMENTO, indiceObservacaoUsoControles);
						System.out.println("registrosMarcados.size(): " + registrosMarcados.size());
						controlesFiltradosCasoFiltrado.addAll(registrosMarcados);						
					}
		}
			
		System.out.println("controlesPositivosFiltradosCasoFiltrado.size(): " + controlesPositivosFiltradosCasoFiltrado.size());
		System.out.println("controlesNegativosFiltradosCasoFiltrado.size(): " + controlesNegativosFiltradosCasoFiltrado.size());
		System.out.println("controlesFiltradosCasoFiltrado.size(): " + controlesFiltradosCasoFiltrado.size());
		
		if(!valoresPositivosENegativos.isEmpty()) {
			i = 0;
			for(String[] controlePositivoFiltradoCasoFiltrado: controlesPositivosFiltradosCasoFiltrado) {
				if(i < TAMANHO_PAREAMENTO) {
					controlesFiltradosCasosFiltrados.add(controlePositivoFiltradoCasoFiltrado);
				} else {
					controlePositivoFiltradoCasoFiltrado[indiceObservacaoUsoControles] = "";
				}
				i++;			
			}
			
			i = 0;
			for(String[] controleNegativoFiltradoCasoFiltrado: controlesNegativosFiltradosCasoFiltrado) {
				if(i < TAMANHO_PAREAMENTO) {
					controlesFiltradosCasosFiltrados.add(controleNegativoFiltradoCasoFiltrado);
				} else {
					controleNegativoFiltradoCasoFiltrado[indiceObservacaoUsoControles] = "";
				}
				i++;			
			}
		} else {
			i = 0;
			for(String[] controleFiltradoCasoFiltrado: controlesFiltradosCasoFiltrado) {
				if(i < TAMANHO_PAREAMENTO) {
					controlesFiltradosCasosFiltrados.add(controleFiltradoCasoFiltrado);
				} else {
					controleFiltradoCasoFiltrado[indiceObservacaoUsoControles] = "";
				}
				i++;			
			}
		}
		
		for(Filtro filtro: filtros) {
			filtro.setDesmarcado(false);
			if(filtro.isData()) {
				filtro.setNumeroSemanas(1);
			}
		}
		
		System.out.println("controlesFiltradosCasosFiltrados.size(): " + controlesFiltradosCasosFiltrados.size());
			
		}
		
		salvarRegistros(casosFiltrados, cabecalhoCasos, controlesFiltradosCasosFiltrados, cabecalhoControles);
		
		scanner.close();
	}

	private static List<String[]> carregarRegistros(String nomeArquivo) throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get(nomeArquivo));
		CSVReader csvReader = new CSVReaderBuilder(reader).build();
		
		return csvReader.readAll();
	}
	
	private static void salvarRegistros(List<String[]> casos, String[] cabecalhoCasos, List<String[]> controles, String[] cabecalhoControles) throws ParseException, FileNotFoundException, IOException {
		 String[] cabecalho = {"id", "municipio", "nomeCompleto", "dataNascimento", "racaCor", "dataNotificacao",
				               "idade", "resultadoTeste", "evolucaoCaso", "sexo", "sexoRedome", "etniaRedome", "semanaNotificacao",
				               "origem"};
	        
		 List<String[]> registros = new ArrayList<>();
		 registros.add(cabecalho);
		 
		 for(String[] caso: casos) {
			 int indiceId = buscarPosicao("id", cabecalhoCasos);
			 int indiceMunicipio = buscarPosicao("municipio", cabecalhoCasos);
			 int indiceNomeCompleto = buscarPosicao("nomeCompleto", cabecalhoCasos);
			 int indiceDataNascimento = buscarPosicao("dataNascimento", cabecalhoCasos);
			 int indiceRacaCor = buscarPosicao("racaCor", cabecalhoCasos);
			 int indiceDataNotificacao = buscarPosicao("dataNotificacao", cabecalhoCasos);
			 int indiceIdade = buscarPosicao("idade", cabecalhoCasos);
			 int indiceResultadoTeste = buscarPosicao("resultadoTeste", cabecalhoCasos);
			 int indiceEvolucaoCaso = buscarPosicao("evolucaoCaso", cabecalhoCasos);
			 int indiceSexo = buscarPosicao("sexo", cabecalhoCasos);
			 int indiceSexoRedome = buscarPosicao("sexoRedome", cabecalhoCasos);
			 int indiceEtniaRedome = buscarPosicao("etniaRedome", cabecalhoCasos);
			 int indiceSemanaNotificacao = buscarPosicao("semanaNotificacao", cabecalhoCasos);
			 
			 String[] registroCaso = {caso[indiceId], caso[indiceMunicipio], caso[indiceNomeCompleto],
									  caso[indiceDataNascimento], caso[indiceRacaCor], caso[indiceDataNotificacao],
									  caso[indiceIdade], caso[indiceResultadoTeste], caso[indiceEvolucaoCaso],
									  caso[indiceSexo], caso[indiceSexoRedome], caso[indiceEtniaRedome], caso[indiceSemanaNotificacao],
									  "caso"};
					 
			 registros.add(registroCaso);
		 }
		
		 
		 for(String[] controle: controles) {
			 int indiceId = buscarPosicao("id", cabecalhoControles);
			 int indiceMunicipio = buscarPosicao("municipio", cabecalhoControles);
			 int indiceNomeCompleto = buscarPosicao("nomeCompleto", cabecalhoControles);
			 int indiceDataNascimento = buscarPosicao("dataNascimento", cabecalhoControles);
			 int indiceRacaCor = buscarPosicao("racaCor", cabecalhoControles);
			 int indiceDataNotificacao = buscarPosicao("dataNotificacao", cabecalhoControles);
			 int indiceIdade = buscarPosicao("idade", cabecalhoControles);
			 int indiceResultadoTeste = buscarPosicao("resultadoTeste", cabecalhoControles);
			 int indiceEvolucaoCaso = buscarPosicao("evolucaoCaso", cabecalhoControles);
			 int indiceSexo = buscarPosicao("sexo", cabecalhoControles);
			 int indiceSexoRedome = buscarPosicao("sexoRedome", cabecalhoControles);
			 int indiceEtniaRedome = buscarPosicao("etniaRedome", cabecalhoControles);
			 int indiceSemanaNotificacao = buscarPosicao("semanaNotificacao", cabecalhoControles);
			 
			 String[] registroControle = {controle[indiceId], controle[indiceMunicipio], controle[indiceNomeCompleto],
									  ConversaoSusSivep.converterDataSusParaSivep(controle[indiceDataNascimento]), 
									  controle[indiceRacaCor], ConversaoSusSivep.converterDataSusParaSivep(controle[indiceDataNotificacao]),
									  controle[indiceIdade], controle[indiceResultadoTeste], controle[indiceEvolucaoCaso],
									  controle[indiceSexo], controle[indiceSexoRedome], controle[indiceEtniaRedome], controle[indiceSemanaNotificacao],
									  "controle"};
					 
			 registros.add(registroControle);
		 }
		 
		 String nomeArquivo = "src/main/resources/csv/pareamento.csv";

         try (var fos = new FileOutputStream(nomeArquivo);
              var osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
              var writer = new CSVWriter(osw)) {
              writer.writeAll(registros);
        }
		
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
	
	private static List<String[]> filtrarPorCampo(List<String[]> registros, String campo, int indiceCampo) {
		return registros.stream()
				        .filter(r -> normalizarString(r[indiceCampo]).equals(normalizarString(campo)))
				        .collect(Collectors.toList());
	}
	
	private static List<String[]> filtrarPorData(List<String[]> registros, String data, int indiceData, int numeroSemanas) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
		Date dataNotificacaoCaso = sdf1.parse(data);
		Date data1 = alterarDiasEmData(dataNotificacaoCaso, numeroSemanas * -7);
		Date data2 = alterarDiasEmData(dataNotificacaoCaso, numeroSemanas * 7);
		
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		
		List<String[]> registrosFiltrados = new ArrayList<>();
		
		for (String[] registro : registros) {
			Date dataNotificacaoControle = sdf2.parse(registro[indiceData]);
			if(dataEstaEmIntervalo(data1, data2, dataNotificacaoControle)) {
				registrosFiltrados.add(registro);
			}
		}
		
		return registrosFiltrados;
	}
	
	private static List<String[]> marcarRegistros(List<String[]> registros, int qtd, int indiceObservacaoUso) {
		registros.stream()
			     .limit(qtd)
			     .forEach(r -> r[indiceObservacaoUso] = "usado");
		
		List<String[]> registrosUsados = registros.stream()
			                                      .filter(r -> r[indiceObservacaoUso] != null 
			                                               && !r[indiceObservacaoUso].equals(""))
			                                      .collect(Collectors.toList());
		
		return registrosUsados;
	}

	
	private static List<String[]> marcarRegistrosPositivos(List<String[]> registros, int qtd, String valorPositivo,
            											   int indiceResultadoTeste, int indiceObservacaoUso) {
		List<String[]> registrosResultadoPositivo = filtrarPorCampo(registros, valorPositivo, indiceResultadoTeste);
		
		registrosResultadoPositivo.stream()
								  .limit(qtd)
								  .forEach(r -> r[indiceObservacaoUso] = "usado");
		
		List<String[]> registrosResultadoPositivoUsados = registrosResultadoPositivo.stream()
												                                    .filter(r -> r[indiceObservacaoUso] != null 
												                                               && !r[indiceObservacaoUso].equals(""))
												                                    .collect(Collectors.toList());
		
		return registrosResultadoPositivoUsados;
	}
	
	private static List<String[]> marcarRegistrosNegativos(List<String[]> registros, int qtd, String valorNegativo,
			                                               int indiceResultadoTeste, int indiceObservacaoUso) {
		List<String[]> registrosResultadoNegativo = filtrarPorCampo(registros, valorNegativo, indiceResultadoTeste);
		
		registrosResultadoNegativo.stream()
								  .limit(qtd)
								  .forEach(r -> r[indiceObservacaoUso] = "usado");
		
		List<String[]> registrosResultadoNegativoUsados = registrosResultadoNegativo.stream()
																					.filter(r -> r[indiceObservacaoUso] != null 
																					        && !r[indiceObservacaoUso].equals(""))
																					.collect(Collectors.toList());
		
		return registrosResultadoNegativoUsados;
	}
}
