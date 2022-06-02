package br.com.covid.pareamento;

public class Filtro {
	
	private String campo;
	
	private boolean isData;
	
	private int numeroSemanas;
	
	private boolean desmarcado;

	public Filtro(String campo, boolean isData) {
		this.campo = campo;
		this.isData = isData;
		this.desmarcado = false;
	}
	
	public String getCampo() {
		return campo;
	}
	
	public boolean isData() {
		return isData;
	}
	
	public void setNumeroSemanas(int numeroSemanas) {
		this.numeroSemanas = numeroSemanas;
	}
	
	public void acrescentarSemanas() {
		this.numeroSemanas++;
	}
	
	public int getNumeroSemanas() {
		return numeroSemanas;
	}
	
	public boolean isDesmarcado() {
		return desmarcado;
	}

	public void setDesmarcado(boolean desmarcado) {
		this.desmarcado = desmarcado;
	}
	
}
