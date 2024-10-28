package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */

	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_USERLIST = "userlist";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_ADDRESS = "address";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	
	private String nickname;
	private int sessionkey;
	private String[] users;
	private int port;
	private InetSocketAddress address;
	
	/*
	 * Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	
	// CONSTRUCTOR BÁSICO PETICIÓN/ACK
	
	public DirMessage(String op) {
		operation = op;
	}
	
	// CONSTRUCTOR OPERACION LOGIN | LOGIN_FAILED | LOOKUP_ADDRESS
	
	public DirMessage(String op, String nick) {
		operation = op;
		nickname = nick;
	}
	
	// CONSTRUCTOR OPERACION SEND_ADDRESS
	
	public DirMessage(String op, InetSocketAddress add) {
		operation = op;
		address = add;
	}
	
	// CONSTRUCTOR OPERACION LOGINOK
	
	public DirMessage(String op, String nick, int session) {
		operation = op;
		nickname = nick;
		sessionkey = session;
	}
	
	// CONSTRUCTOR OPERACION LOGOUT | UNREGISTER_SERVER
	
	public DirMessage(String op, int session) {
		operation = op;
		sessionkey = session;
	}
	
	// CONSTRUCTOR OPERACION USERLISTOK
	
	public DirMessage(String op, String[] usuarios) {
		operation = op;
		users = Arrays.copyOf(usuarios, usuarios.length);
	}
	
	// CONSTRUCTOR OPERACION REGISTER_SERVER_PORT
	
	public DirMessage(String op, int session, int pt) {
		operation = op;
		sessionkey = session;
		port = pt;
	}
	
	public void setOperation(String op) {
		this.operation = op;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public void setSessionkey(String sessionkey) {
		this.sessionkey = Integer.parseInt(sessionkey);
	}
	
	public int getSessionkey() {
		return sessionkey;
	}
	
	public void setNickname(String nick) {
		nickname = nick;
	}

	public String getNickname() {
		return nickname;
	}
	
	public String[] getUsers() {
		return Arrays.copyOf(users, users.length);
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}
	
	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}
	
	// Método para obtener un array de usuarios a partir de un string donde aparecen usuarios
	// separados por comas.
	public static String[] usersToArray(String users) {
		LinkedList<String> usuariosAux = new LinkedList<String>();
		String auxiliar;
		int j = 0;
		for(int i = 0; i < users.length(); i++) {
			auxiliar = "";
			while(i < users.length() && users.charAt(i) != ',' && users.charAt(i) != '\n') {
				auxiliar = auxiliar + users.charAt(i);
				i++;
			}
			usuariosAux.add(auxiliar);
			++j;
		}
		String[] usuarios = new String[usuariosAux.size()];
		int k = 0;
		for(String user : usuariosAux) {
			usuarios[k] = user;
			k++;
		}
		return usuarios;
	}
	
	// Método para obtener un InetSocketAddress a partir de un string
	public static InetSocketAddress stringToAddress(String address) {
		InetSocketAddress addressParsed = null;
		
		String partes[] = address.split(":");
		if(partes.length == 2) {
			String host = partes[0].split("/")[1];
			int port;
			port = Integer.parseInt(partes[1]);
			addressParsed = new InetSocketAddress(host, port);
		}
		
		return addressParsed;
	}
	
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;


		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();
			
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			
			case FIELDNAME_NICKNAME: {
				m.setNickname(value);
				break;
			}
			
			case FIELDNAME_SESSIONKEY: {
				m.setSessionkey(value);
				break;
			}
			
			case FIELDNAME_USERLIST: {
				m.users = usersToArray(value);
				break;
			}
			
			case FIELDNAME_PORT: {
				m.setPort(value);
				break;
			}
			
			case FIELDNAME_ADDRESS: {
				m.address = stringToAddress(value);
				break;
			}
				
			


			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch(operation) {
		
			case(DirMessageOps.OPERATION_LOGIN):
			case(DirMessageOps.OPERATION_LOGINFAILED):
			case(DirMessageOps.OPERATION_LOOKUP_ADDRESS):
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				break;
				
			case(DirMessageOps.OPERATION_LOGINOK): 
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionkey + END_LINE);
				break;
				
			case(DirMessageOps.OPERATION_LOGOUT):
			case(DirMessageOps.OPERATION_UNREGISTER_SERVER):
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionkey + END_LINE);
				break;
				
			case(DirMessageOps.OPERATION_USERLISTOK):
				sb.append(FIELDNAME_USERLIST + DELIMITER);
				for(String user : users) {
						sb.append(user+',');	
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append(END_LINE);
				break;
				
			case(DirMessageOps.OPERATION_REGISTER_SERVER_PORT):
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionkey + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
				break;
			
			case(DirMessageOps.OPERATION_SEND_ADDRESS): {
				sb.append(FIELDNAME_ADDRESS + DELIMITER + address);
				break;
			}
		}
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
