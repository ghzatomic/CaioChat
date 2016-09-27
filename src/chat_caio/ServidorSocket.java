package chat_caio;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ServidorSocket extends Thread {
	
	private static int portaConexao = 8095;
	
	private String key = Constantes.CHAVE_CRIPTOGRAFIA;
	
	// Parte que controla as conexões por meio de threads.
	// socket deste cliente
	private Socket conexao;
	
	private ClienteDTO cliente;
	// lista que armazena nome de CLIENTES
	private static List<ClienteDTO> clienteList;

	// construtor que recebe o socket deste cliente
	public ServidorSocket(Socket socket) {
		this.conexao = socket;
	}

	public ClienteDTO localizaClienteByNome(String nome){
		if (nome != null && !"".equals(nome)){
			for (ClienteDTO cliFind : this.getClienteList()) {
				if (cliFind.getNome().equals(nome)){
					return cliFind;
				}
			}
		}
		return null;
	}
	
	// testa se nomes são iguais, se for retorna true
	public boolean armazena(ClienteDTO cliente) {
		if (localizaClienteByNome(cliente.getNome()) != null){
			return true;
		}
		// adiciona na lista apenas se não existir
		this.getClienteList().add(cliente);
		return false;
	}

	// remove da lista os CLIENTES que já deixaram o chat
	public void remove(String oldName) {
		ClienteDTO cliLoc = localizaClienteByNome(oldName);
		if (cliLoc != null){
			this.getClienteList().remove(cliLoc);
		}
	}

	public static void main(String args[]) {
		clienteList = new LinkedList<ClienteDTO>();
		try {
			ServerSocket server = new ServerSocket(portaConexao);
			System.out.println("ServidorSocket rodando na porta "+portaConexao);
			// Loop principal.
			while (true) {
				// aguarda algum cliente se conectar.
				// A execução do servidor fica bloqueada na chamada do método
				// accept da
				// classe ServerSocket até que algum cliente se conecte ao
				// servidor.
				// O próprio método desbloqueia e retorna com um objeto da
				// classe Socket
				Socket conexao = server.accept();
				// cria uma nova thread para tratar essa conexão
				Thread t = new ServidorSocket(conexao);
				t.start();
				// voltando ao loop, esperando mais alguém se conectar.
			}
		} catch (IOException e) {
			// caso ocorra alguma excessão de E/S, mostre qual foi.
			System.out.println("IOException: " + e);
		}
	}

	// execução da thread
	public void run() {
		this.setCliente(new ClienteDTO());
		PrintStream saida = null;
		try {
			saida = new PrintStream(this.conexao.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String nomeCliente = null;
		try {
			// objetos que permitem controlar fluxo de comunicação que vem do
			// cliente
			// recebe o nome do cliente
			nomeCliente = Util.decode(Util.getData(this.conexao.getInputStream()));
			// igual a null encerra a execução
			if (nomeCliente == null) {
				return;
			}
			// chamada ao metodo que testa nomes iguais
			ClienteDTO cliLoc = localizaClienteByNome(nomeCliente); 
			if (cliLoc != null) {
				saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
				// fecha a conexao com este cliente
				//this.conexao.close();
				return;
			}else {
				this.getCliente().setNome(nomeCliente);
				this.getCliente().setSaida(saida);
				this.getClienteList().add(this.getCliente());
				sendToAllCript(saida, " ", "entrou !");
				pushUserList();
				// mostra o nome do cliente conectado ao servidor
				System.out.println(nomeCliente
						+ " : Conectado ao Servidor!");
			}
			
			// adiciona os dados de saida do cliente no objeto CLIENTES
			// recebe a mensagem do cliente
			byte[] data = Util.getData(this.conexao.getInputStream());
			//String msg = entrada.read();
			// Verificar se linha é null (conexão encerrada)
			// Se não for nula, mostra a troca de mensagens entre os CLIENTES
			while (data != null) {
				String msgDecript = Util.decode(data);
				if (msgDecript != null){
					if (!"[NOOPERATION]".equals(msgDecript)){
						if (msgDecript.contains(">:")){
							String[] list = msgDecript.split(":");
							String nome = list[0].replace("<", "").replace(">", "");
							String msg = list[1];
							if ("Todos".equals(nome.trim())){
								// reenvia a linha para todos os CLIENTES conectados
								sendToAll(saida, " diz: ", Util.encode(msg));
							}else{
								sendToClient(saida,this.getCliente().getNome(), nome, " diz(privado): ", Util.encode(msg));
							}
						}else{
							// reenvia a linha para todos os CLIENTES conectados
							sendToAll(saida, " diz: ", data);
						}
					}
				}
				
				// espera por uma nova linha.
			    data = Util.getData(this.conexao.getInputStream());
			}
			// se cliente enviar linha em branco, mostra a saida no servidor
			System.out.println(nomeCliente + " saiu do bate-papo!");
			// se cliente enviar linha em branco, servidor envia mensagem de
			// saida do chat aos CLIENTES conectados
			sendToAllCript(saida, " saiu", " do bate-papo!");
			// remove nome da lista
			remove(nomeCliente);
			// exclui atributos setados ao cliente
			pushUserList();
			// fecha a conexao com este cliente
			//this.conexao.close();
		} catch (Exception e) {
			try {
				sendToAllCript(saida, " saiu", " do bate-papo!");
				remove(nomeCliente);
				pushUserList();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Caso ocorra alguma excessão de E/S, mostre qual foi.
			System.out.println("Falha na Conexao... .. ." + " IOException: "
					+ e);
		}
	}

	public void sendToAllCript(PrintStream saida, String acao, String msg)
			throws IOException {
		for (ClienteDTO cli : this.getClienteList()) {
			PrintStream chat = cli.getSaida();
			if (chat != saida) {
				byte[] encript = Util.encode(this.getCliente().getNome() + acao + msg);
				chat.write(encript);
			}
		}
	}
	
	public void pushUserList()
			throws IOException {
		String lista = "";
		for (ClienteDTO cli : this.getClienteList()) {
			if ("".equals(lista)){
				lista = cli.getNome()+",";
			}else{
				lista = lista+","+cli.getNome();
			}
		}
		for (ClienteDTO cli : this.getClienteList()) {
			PrintStream chat = cli.getSaida();
			byte[] encript = Util.encode("[LISTAUSUARIOS]"+lista.trim());
			chat.write(encript);
		}
	}
	
	// enviar uma mensagem para todos, menos para o próprio
	public void sendToAll(PrintStream saida, String acao, byte[] msg)
			throws IOException {
		for (ClienteDTO cli : this.getClienteList()) {
			PrintStream chat = cli.getSaida();
			if (chat != saida) {
				String msgDecript = Util.decode(msg);
				byte[] encript = Util.encode(this.getCliente().getNome() + acao+msgDecript);
				chat.write(encript);
			}
		}
	}
	
	// enviar uma mensagem para todos, menos para o próprio
	public void sendToClient(PrintStream saida,String cliOrigem,String cliente, String acao, byte[] msg)
			throws IOException {
			ClienteDTO cliLoc = localizaClienteByNome(cliente);
			if (cliLoc != null){
				PrintStream chat = cliLoc.getSaida();
				// envia para todos, menos para o próprio usuário
				if (chat != saida) {
					String msgDecript = Util.decode(msg);
					byte[] encript = Util.encode(cliOrigem + acao+msgDecript);
					chat.write(encript);
				}
			}
		}

	public static int getPortaConexao() {
		return portaConexao;
	}

	public static void setPortaConexao(int portaConexao) {
		ServidorSocket.portaConexao = portaConexao;
	}

	public Socket getConexao() {
		return conexao;
	}

	public void setConexao(Socket conexao) {
		this.conexao = conexao;
	}

	public ClienteDTO getCliente() {
		return cliente;
	}

	public void setCliente(ClienteDTO cliente) {
		this.cliente = cliente;
	}

	public List<ClienteDTO> getClienteList() {
		return clienteList;
	}

	public void setClienteList(List<ClienteDTO> clienteList) {
		ServidorSocket.clienteList = clienteList;
	}

}
