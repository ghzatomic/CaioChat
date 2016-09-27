package chat_caio;

import java.io.PrintStream;

public class ClienteDTO {

	private String nome;
	
	private PrintStream saida;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public PrintStream getSaida() {
		return saida;
	}

	public void setSaida(PrintStream saida) {
		this.saida = saida;
	}
	
	
	
}
