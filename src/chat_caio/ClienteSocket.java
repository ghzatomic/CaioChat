package chat_caio;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JOptionPane;

public class ClienteSocket extends Thread {
	private static int portaConexao = 8095;
	
	private String key = Constantes.CHAVE_CRIPTOGRAFIA;
	
	//O Visual
	private ClienteVisual visual = null;
	
	// parte que controla a recepção de mensagens do cliente
	private Socket conexao;

	// construtor que recebe o socket do cliente
	public ClienteSocket(Socket socket) {
		this.conexao = socket;
		try {
			visual = new ClienteVisual(this.conexao);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Problema com o socket !");
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		try {
			// Instancia do atributo conexao do tipo Socket, conecta a IP do
			// Servidor, Porta
			//Socket socket = new Socket(JOptionPane.showInputDialog("Digite ip: ","127.0.0.1"), portaConexao);
			Socket socket = new Socket("192.168.1.250", portaConexao);
			// Instancia do atributo saida, obtem os objetos que permitem
			// controlar o fluxo de comunicação
			PrintStream saida = new PrintStream(socket.getOutputStream());
			
			String meuNome = "";
			meuNome = InetAddress.getLocalHost().getHostName();
			if ("".equals(meuNome) ){
				meuNome = JOptionPane.showInputDialog("Digite seu nome: ");
			}
			// envia o nome digitado para o servidor
			if ("".equals(meuNome.trim()) || " ".equals(meuNome.trim())){
				JOptionPane.showMessageDialog(null, "Pode tratar de por um nome ai zé !");
				System.exit(0);
			}
			saida.write(Util.encode(meuNome));
			// instancia a thread para ip e porta conectados e depois inicia ela
			Thread thread = new ClienteSocket(socket);
			thread.start();
			// Cria a variavel msg responsavel por enviar a mensagem para o
			// servidor
			/*String msg;
			while (true) {
				// cria linha para digitação da mensagem e a armazena na
				// variavel msg
				System.out.print("Mensagem > ");
				msg = teclado.readLine();
				// envia a mensagem para o servidor
				saida.println(msg);
			}*/
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Falha na Conexao... .. ." + " IOException: "
					+ e);
		}
	}

	// execução da thread
	public void run() {
		try {
			// recebe mensagens de outro cliente através do servidor
			BufferedReader entrada = new BufferedReader(new InputStreamReader(
					this.conexao.getInputStream()));
			// cria variavel de mensagem
			String msg;
			while (true) {
				// pega o que o servidor enviou
			    byte[] data = Util.getData(this.conexao.getInputStream());
				// se a mensagem contiver dados, passa pelo if, caso contrario
				// cai no break e encerra a conexao
				if (data == null) {
					System.out.println("Conexão encerrada!");
					System.exit(0);
				}
				// imprime a mensagem recebida
				String msgDecript = Util.decode(data);
				if (msgDecript.contains("[LISTAUSUARIOS]")){
					msgDecript=msgDecript.replace("[LISTAUSUARIOS]", "");
					String[] usrs = msgDecript.split(",");
					visual.refreshListUsuarios(usrs);
				}else{
					visual.addMensagem(msgDecript);
				}
			}
		} catch (IOException e) {
			// caso ocorra alguma exceção de E/S, mostra qual foi.
			visual.addMensagem("Ocorreu uma Falha... .. ." + " IOException: "
					+ e);
		}
	}
}
