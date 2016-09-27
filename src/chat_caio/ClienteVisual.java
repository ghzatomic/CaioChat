package chat_caio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public class ClienteVisual {

	private Timer timer;
	
	private Socket socket;
	
	private String key = Constantes.CHAVE_CRIPTOGRAFIA;
	
	private static int linhas = 10;
	private static int colunas = 40;
	
	private JFrame frame = null;
	
	private JList listUsuarios;
	
	private DefaultListModel listModel = new DefaultListModel();
	
	private PrintStream saida = null;
	
	private JButton limparText = null;

	private JTextField textFieldGeral = null;
	
	private JTextPane textAreaMensagensWeb = null;
	
	private JScrollPane jScrollPaneTextArea;
	
	private JScrollPane jScrollPaneListUsuarios;
	
	private StyledDocument styledDocument = null;
	
	private JPanel panelRoot = null;
	
	public ClienteVisual(Socket socket) throws Exception {
		this.socket = socket;
		saida = new PrintStream(this.socket.getOutputStream());		
		iniciaComponentes();
		timer = new Timer();
        timer.schedule(new RemindTask(),0, 5*1000);
	}
	
	class RemindTask extends TimerTask {
        public void run() {
            try {
				saida.write(Util.encode("[NOOPERATION]"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           // timer.cancel(); //Terminate the timer thread
        }
    }
	
	private void iniciaComponentes(){
		getFrame();
	}
	
	private JFrame getFrame() {
		if (frame == null){
			final Dimension dimensions = new Dimension(534, 221);
			frame = new JFrame("Contra-senha");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(this.getPanelRoot());
			frame.setResizable(true);
			getTextFieldGeral().requestFocus();
			frame.setVisible(true);
			frame.setSize(dimensions);
		}
		return frame;
	}
	
	private JTextField getTextFieldGeral() {
		final String padrao = "Digite e aperte <enter>";
		if (this.textFieldGeral == null){
			this.textFieldGeral = new JTextField(padrao,colunas);
			this.textFieldGeral.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {}
				
				public void focusGained(FocusEvent e) {
					if (padrao.equals(textFieldGeral.getText())){
						getTextFieldGeral().setText("");
					}
				}
			});
			this.textFieldGeral.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String privateUsers = "";
					if (!"".equals(textFieldGeral.getText().trim())){
						String pulaLinha = "";
						if (!"".equals(getTextAreaText())){
							pulaLinha = "\n";
						}
						try {
							Object[] valores = getListUsuarios().getSelectedValues();
							if (valores.length == 1){
								if ("Todos".equals((String)valores[0])){
									enviarMensagem(textFieldGeral.getText());
								}else{
									privateUsers = (String)valores[0];
									enviarMensagemEspecifica((String)valores[0],textFieldGeral.getText());
								}
							}else{
								for (Object object : valores) {
									String nome = (String) object;
									if ("Todos".equals(nome)){
										enviarMensagem(textFieldGeral.getText());
									}else{
										if (privateUsers != ""){
											privateUsers+=","+nome;
										}else{
											privateUsers+=nome;
										}
										enviarMensagemEspecifica(nome,textFieldGeral.getText());
									}
								}
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (privateUsers != ""){
							addMensagem("Eu ->("+privateUsers+"):"+textFieldGeral.getText());
						}else{
							addMensagem("Eu : "+textFieldGeral.getText());
						}
						updateScrollTextArea();
					}
					textFieldGeral.setText("");
				}
			});
		}
		return textFieldGeral;
	}
	
	private void updateScrollTextArea(){
		//getjScrollPaneTextArea().getVerticalScrollBar().setValue(getjScrollPaneTextArea().getVerticalScrollBar().getMaximum());
		this.getjScrollPaneTextArea().getVerticalScrollBar().setValue(this.getjScrollPaneTextArea().getVerticalScrollBar().getMaximum()+1);
	}

	private JPanel getPanelRoot() {
		if (this.panelRoot == null){
			this.panelRoot = new JPanel(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(getTextFieldGeral(),BorderLayout.CENTER);
			panel.add(getLimparText(),BorderLayout.EAST);
			
			c.fill = GridBagConstraints.HORIZONTAL;
		    c.gridx = 0;
		    c.gridy = 1;
		    panelRoot.add(panel, c);
			
		    JPanel panelAreas = new JPanel(new GridBagLayout());
		    
		    c.fill = GridBagConstraints.HORIZONTAL;
		    c.weightx = 0.2;
		    c.weighty = 1;
		    c.gridx =0;
		    c.gridy =0;
		    panelAreas.add(getjScrollPaneListUsuarios(),c);
		    
		    c.fill = GridBagConstraints.HORIZONTAL;
		    c.weightx = 1;
		    c.weighty = 1;
		    c.gridx =1;
		    c.gridy =0;
		    panelAreas.add(getjScrollPaneTextArea(),c);
		    
		    c.fill = GridBagConstraints.HORIZONTAL;
		    c.gridx = 0;
		    c.gridy = 0;
			this.panelRoot.add(panelAreas,c);
		}
		return panelRoot;
	}

	private JTextPane getTextAreaMensagens() {
		return this.getTextAreaMensagensWeb();
		// Editado para a substituicao do textArea para o web text area
		/*if (this.textAreaMensagens == null){
			this.textAreaMensagens = new JTextArea(linhas,colunas);
			this.textAreaMensagens.setEditable(false);
			this.textAreaMensagens.setLineWrap(true);
			this.textAreaMensagens.setWrapStyleWord(true);
		}
		return textAreaMensagens;*/
	}
	
	
	
	private JScrollPane getjScrollPaneTextArea() {
		if (this.jScrollPaneTextArea == null){
			jScrollPaneTextArea = new JScrollPane(this.getTextAreaMensagens());
			jScrollPaneTextArea.setLayout(new ScrollPaneLayout());
			jScrollPaneTextArea.setVerticalScrollBarPolicy(
	                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPaneTextArea.setPreferredSize(new Dimension(250, 145));
			jScrollPaneTextArea.setMinimumSize(new Dimension(300, 200));
		}
		return jScrollPaneTextArea;
	}

	private JList getListUsuarios() {
		if (listUsuarios == null){
			listModel.addElement("Todos");
			listUsuarios = new JList(listModel);
			listUsuarios.setSelectedIndex(0);
			listUsuarios.setSize(new Dimension(534, 221));
		}
		return listUsuarios;
	}

	private JScrollPane getjScrollPaneListUsuarios() {
		if (this.jScrollPaneListUsuarios == null){
			jScrollPaneListUsuarios = new JScrollPane(this.getListUsuarios());
			jScrollPaneListUsuarios.setAutoscrolls(true);
		}
		return jScrollPaneListUsuarios;
	}
	
	public void refreshListUsuarios(String[] usuarios){
		int indice = listUsuarios.getSelectedIndex();
		if (indice == -1){
			listUsuarios.setSelectedIndex(0);
		}
		listModel.clear();
		listModel.addElement("Todos");
		for (String string : usuarios) {
			if (!string.equals("")){
				listModel.addElement(string);
			}
		}
		listUsuarios.setSelectedIndex(indice);
	}
	
	
	
	public JButton getLimparText() {
		if (this.limparText == null){
			this.limparText = new JButton("Limpar");
			this.limparText.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					getTextAreaMensagens().setText("");
					getTextFieldGeral().requestFocus();
				}
			});
		}
		return limparText;
	}

	
	
	public JTextPane getTextAreaMensagensWeb() {
		if (textAreaMensagensWeb == null){
			textAreaMensagensWeb = new JTextPane();
			textAreaMensagensWeb.setBackground(Color.white);
	        textAreaMensagensWeb.setEditable(false);
	        textAreaMensagensWeb.setAutoscrolls(true);
	        styledDocument = textAreaMensagensWeb.getStyledDocument();
	        DefaultCaret caret = (DefaultCaret)textAreaMensagensWeb.getCaret();
	        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		}
		return textAreaMensagensWeb;
	}

	private StyledDocument getStyledDocument() {
		if (styledDocument == null){
			this.getTextAreaMensagens();
		}
		return styledDocument;
	}

	private void enviarMensagem(String mensagem) throws Exception{
		byte[] encript = Util.encode(mensagem);
		saida.write(encript);
	}
	
	private void enviarMensagemEspecifica(String usuario,String mensagem) throws Exception{
		byte[] encript = Util.encode("<"+usuario+">:"+mensagem);
		saida.write(encript);
	}
	
	public void addMensagem(String mensagem){
	    if (mensagem.contains("diz(privado):")){
	    	addMensagemPrivado(mensagem);
	    }else{
	    	String pulaLinha = "";
			if (!"".equals(getTextAreaText())){
				pulaLinha = "\n";
			}
	    	try {
	    		Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
	    	    Style estiloNormal = this.getStyledDocument().addStyle( "regular", def );
				this.getStyledDocument().insertString(this.getStyledDocument().getLength(), pulaLinha+mensagem, estiloNormal);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	updateScrollTextArea();
	    	getFrame().toFront();
	    }
	}
	
	public void addMensagemPrivado(String mensagem){
		String pulaLinha = "";
		if (!"".equals(getTextAreaText())){
			pulaLinha = "\n";
		}
		
		Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
	    Style estiloNormal = this.getStyledDocument().addStyle( "regular", def );
		
    	Style bold = this.getStyledDocument().addStyle( "bold", estiloNormal );
        StyleConstants.setBold( bold, true );
        try {
			this.getStyledDocument().insertString(this.getStyledDocument().getLength(), pulaLinha+mensagem, bold);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		//getTextAreaMensagens().setText(getTextAreaMensagens().getText()+pulaLinha+mensagem);
		this.getjScrollPaneTextArea().getVerticalScrollBar().setValue(this.getjScrollPaneTextArea().getVerticalScrollBar().getMaximum());
		getFrame().toFront();
	}
	
	private String getTextAreaText(){
		return this.getTextAreaMensagens().getText();
	}
	
}
