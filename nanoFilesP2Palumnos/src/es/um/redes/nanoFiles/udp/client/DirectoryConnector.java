package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;

	public DirectoryConnector(String address) throws IOException {
		try {
			InetAddress serverIp = InetAddress.getByName(address);
			directoryAddress = new InetSocketAddress(serverIp, DIRECTORY_PORT);

			socket = new DatagramSocket();
		} catch(UnknownHostException uhe) {
			System.out.println("Directory host address couldn't be resolved.");
		}
		
		
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException 
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		int num_reenvios = 0;
		boolean respuestaRecibida = true;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * Enviar datos en un datagrama al directorio y recibir una respuesta. El
		 * array devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
		 * recepción al completo.
		 */
		/*
		 * Una vez el envío y recepción asumiendo un canal confiable (sin
		 * pérdidas) esté terminado y probado, debe implementarse un mecanismo de
		 * retransmisión usando temporizador, en caso de que no se reciba respuesta en
		 * el plazo de TIMEOUT. En caso de salte el timeout, se debe reintentar como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		/*
		 * Las excepciones que puedan lanzarse al leer/escribir en el socket deben
		 * ser capturadas y tratadas en este método. Si se produce una excepción de
		 * entrada/salida (error del que no es posible recuperarse), se debe informar y
		 * terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */
		DatagramPacket pktToServer = new DatagramPacket(requestData, requestData.length,directoryAddress); 
		DatagramPacket pktFromServer = new DatagramPacket(responseData, responseData.length);
		
		do{
			try {
				socket.send(pktToServer);
				socket.setSoTimeout(TIMEOUT);
				socket.receive(pktFromServer);
				respuestaRecibida = true;
			}catch(SocketTimeoutException e){
				num_reenvios++;
				respuestaRecibida = false;
				System.out.println("Attempting connection... Number of attempts: " + num_reenvios);
			}catch(IOException io) {
				System.out.println("An I/O error has ocurred.");
				io.printStackTrace();
			}
		}while(!respuestaRecibida && num_reenvios < MAX_NUMBER_OF_ATTEMPTS);
			
		if(respuestaRecibida) {
			response = new byte[pktFromServer.getLength()];
			
			for(int i = 0; i<=pktFromServer.getLength()-1; i++) {
				response[i] = responseData[i];
			}
			
			if (response != null && response.length == responseData.length) {
				System.err.println("Your response is as large as the datagram reception buffer!!\n"
						+ "You must extract from the buffer only the bytes that belong to the datagram!");
			}
		}
		else {
			System.err.println("Error: max number of attempts (" + MAX_NUMBER_OF_ATTEMPTS + ") reached.");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 * @throws IOException 
	 */
	
	public boolean testSendAndReceive() throws IOException {
		/*
		 * Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe
		 * enviar un datagrama con la cadena "login" y comprobar que la respuesta
		 * recibida es "loginok". En tal caso, devuelve verdadero, falso si la respuesta
		 * no contiene los datos esperados.
		 */
		boolean success = false;
		String strToServer = "login";
		byte[] messageToServer = strToServer.getBytes();
		byte[] messageFromServer;
		
		messageFromServer = sendAndReceiveDatagrams(messageToServer);
		
		
		
		String strFromServer = new String(messageFromServer, 0 ,messageFromServer.length);
		if(strFromServer.compareTo("loginok\0") == 0) {
			success = true;
		}
		
		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 * @throws IOException 
	 */
	public boolean logIntoDirectory(String nickname) throws IOException {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase
		// DirMessageOps
		// 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		// 3.Crear un datagrama con los bytes en que se codifica la cadena
		// 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		// 5.Convertir respuesta recibida en un objeto DirMessage (método
		// DirMessage.fromString)
		// 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		// 7.Devolver éxito/fracaso de la operación

		DirMessage loginMessage = new DirMessage(DirMessageOps.OPERATION_LOGIN, nickname);
		
		byte msgServer[] = loginMessage.toString().getBytes();
		byte msgFromServer[] = null;
		
		msgFromServer = sendAndReceiveDatagrams(msgServer);
		
		if(msgFromServer != null) {
			String stringFromServer = new String(msgFromServer, 0 ,msgFromServer.length);
			DirMessage dirFromServer = DirMessage.fromString(stringFromServer);
			if(dirFromServer.getOperation().equals(DirMessageOps.OPERATION_LOGINOK)) {
				sessionKey =  dirFromServer.getSessionkey();
				success = true; 
			}
		}
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public String[] getUserList() throws IOException{
		String[] userlist = null;
		// Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage userlistRequest = new DirMessage(DirMessageOps.OPERATION_USERLIST);
		
		byte msgToServer[] = userlistRequest.toString().getBytes();
		byte msgFromServer[] = null;
		
		msgFromServer = sendAndReceiveDatagrams(msgToServer);
		
		if(msgFromServer != null) {
			String stringFromServer = new String(msgFromServer, 0, msgFromServer.length);
			DirMessage dirmsgFromServer = DirMessage.fromString(stringFromServer);
			if(dirmsgFromServer.getOperation().equals(DirMessageOps.OPERATION_USERLISTOK)) {
				userlist = dirmsgFromServer.getUsers();
			}
		}

		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 * @throws IOException 
	 */
	public boolean logoutFromDirectory() throws IOException {
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage logoutMessage = new DirMessage(DirMessageOps.OPERATION_LOGOUT, sessionKey);
				boolean success = false;
				byte msgServer[] = logoutMessage.toString().getBytes();
				byte msgFromServer[] = null;
				
				msgFromServer = sendAndReceiveDatagrams(msgServer);
				
				if(msgFromServer != null) {
					String stringFromServer = new String(msgFromServer, 0 ,msgFromServer.length);
					DirMessage dirFromServer = DirMessage.fromString(stringFromServer);
					if(dirFromServer.getOperation().equals(DirMessageOps.OPERATION_LOGOUTOK)) {
						sessionKey =  INVALID_SESSION_KEY;
						success= true; 
					}
				}

		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		boolean success = false;
		DirMessage registerPort = 
				new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_PORT, sessionKey, serverPort);
		byte msgServer[] = registerPort.toString().getBytes();
		byte msgFromServer[] = null;
		
		msgFromServer = sendAndReceiveDatagrams(msgServer);
		
		if(msgFromServer != null) {
			String strFromServer = new String(msgFromServer, 0 ,msgFromServer.length);
			DirMessage dirFromServer = DirMessage.fromString(strFromServer);
			if(dirFromServer.getOperation().equals(DirMessageOps.OPERATION_SERVER_PORT_REGISTERED)) {
				success = true;
			}
		}

		return success;
	}
	
	/**
	 * Método para dar de baja al servidor de ficheros.
	 * 
	 * @param sessionkey La session key del usuario ejecutando el servidor
	 * @return Verdadero si el directorio ha podido borrar el servidor.
	 */
	
	public boolean unregisterServer() {
		boolean success = false;
		
		DirMessage unregisterPort = 
				new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER, sessionKey);
		
		byte msgServer[] = unregisterPort.toString().getBytes();
		byte msgFromServer[] = null;
		
		msgFromServer = sendAndReceiveDatagrams(msgServer);
		
		if(msgFromServer != null) {
			String strFromServer = new String(msgFromServer, 0 ,msgFromServer.length);
			DirMessage dirFromServer = DirMessage.fromString(strFromServer);
			if(dirFromServer.getOperation().equals(DirMessageOps.OPERATION_SERVER_UNREGISTERED)) {
				success = true;
			}
		}
		
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;
		
		DirMessage lookupAddress = 
				new DirMessage(DirMessageOps.OPERATION_LOOKUP_ADDRESS, nick);
		byte msgServer[] = lookupAddress.toString().getBytes();
		byte msgFromServer[] = null;
		
		msgFromServer = sendAndReceiveDatagrams(msgServer);
		
		if(msgFromServer != null) {
			String strFromServer = new String(msgFromServer, 0 ,msgFromServer.length);
			DirMessage dirFromServer = DirMessage.fromString(strFromServer);
			if(dirFromServer.getOperation().equals(DirMessageOps.OPERATION_SEND_ADDRESS)) {
				serverAddr = dirFromServer.getAddress();
			}
		}
		
		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;

		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar

		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return nicklist;
	}




}
