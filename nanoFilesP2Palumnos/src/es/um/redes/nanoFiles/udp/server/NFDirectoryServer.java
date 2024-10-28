package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	
	// Guarda la dirección del servidor + La session key asociada al servidor
	private HashMap<Integer, InetSocketAddress> registeredServers;



	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		
		socket = new DatagramSocket(DIRECTORY_PORT);
		
		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();
		registeredServers = new HashMap<Integer, InetSocketAddress>();

		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		DatagramPacket dataFromClient = new DatagramPacket(receptionBuffer, DirMessage.PACKET_MAX_SIZE);


		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			//Recibimos a través del socket un datagrama
			socket.receive(dataFromClient);

			//Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = dataFromClient.getLength();

			//Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			
			clientAddr = (InetSocketAddress) dataFromClient.getSocketAddress();


			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
				/*
				 * Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				messageFromClient = new String(receptionBuffer,0,dataLength);

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					String messageToClient;
					if(messageFromClient.equals("login")) {
						messageToClient = "loginok";
					}else {
						messageToClient = "nologin";
					}
					byte[] dataToClient = messageToClient.getBytes();
					
					DatagramPacket pcktToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(pcktToClient);

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random();
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}

					/*
					 * Construir String a partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					DirMessage dirFromClient = DirMessage.fromString(messageFromClient);
					System.out.println("Received " + dirFromClient.getOperation() + " operation." );
					/*
					 * Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					
					DirMessage dirForClient = buildResponseFromRequest(dirFromClient, clientAddr);
					
					/*
					 * Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					
					String dataForClient = dirForClient.toString();
					byte[] dataToClient = dataForClient.getBytes();
					DatagramPacket pcktToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
					socket.send(pcktToClient);
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();
		DirMessage response = null;


		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();
			if(!nicks.containsKey(username)) {
				int newSessionKey = random.nextInt(1000);
				while(nicks.containsValue(newSessionKey)) {
					newSessionKey = random.nextInt(1000);
				}
				nicks.put(username, newSessionKey);
				sessionKeys.put(newSessionKey, username);
				System.out.println("Login succesful, session key: " + newSessionKey + ". Sending message to " + clientAddr.getAddress() + "\n");
				response = new DirMessage(DirMessageOps.OPERATION_LOGINOK, username, newSessionKey);
			}else {
				System.out.println("Login failed: user already logged. Sending error to " + clientAddr.getAddress() + "\n");
				response = new DirMessage(DirMessageOps.OPERATION_LOGINFAILED, username);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_USERLIST: {
			String[] nicknames = new String[nicks.keySet().size()];
			int i = 0;
			for(String nick : nicks.keySet()) {
				nicknames[i] = nick;
				i++;
			}
			response = new DirMessage(DirMessageOps.OPERATION_USERLISTOK, nicknames);
			System.out.println("Userlist ready to be sent. Sendind message to " + clientAddr.getAddress() + "\n");
			break;
		}
		
		case DirMessageOps.OPERATION_LOGOUT: {
			int sk = msg.getSessionkey();
			String user = sessionKeys.get(sk);
			sessionKeys.remove(sk);
			nicks.remove(user);
			System.out.println("Logout succesful for user " + user + ". Sending message to " + clientAddr.getAddress() + "\n");
			response = new DirMessage(DirMessageOps.OPERATION_LOGOUTOK);
			break;
		}
		
		case DirMessageOps.OPERATION_REGISTER_SERVER_PORT: {
			int port = msg.getPort();
			int sk = msg.getSessionkey();
			if(registeredServers != null && !registeredServers.containsKey(sk)) {
				InetSocketAddress dirServer = new InetSocketAddress(clientAddr.getAddress(), port);
				registeredServers.put(sk, dirServer);
				System.out.println("Server registered succesfully. Sending message to " + clientAddr.getAddress() + "\n");
				response = new DirMessage(DirMessageOps.OPERATION_SERVER_PORT_REGISTERED);
			}
			else {
				System.out.println("A server is already registered. Sending error to " + clientAddr.getAddress() + "\n");
				response = new DirMessage(DirMessageOps.OPERATION_SERVER_PORT_FAILED);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_LOOKUP_ADDRESS: {
			String nickname = msg.getNickname();
			
			if(nicks.containsKey(nickname)) {
				int sk = nicks.get(nickname);
				if(registeredServers != null && registeredServers.containsKey(sk)) {
					System.out.println("Address found. Sending message to " + clientAddr.getAddress() + "\n");
					response = new DirMessage(DirMessageOps.OPERATION_SEND_ADDRESS, registeredServers.get(sk));
					break;
				}
			}
			System.out.println("Address not found. Sending message to " + clientAddr.getAddress() + "\n");
			response = new DirMessage(DirMessageOps.OPERATION_ADDRESS_NOT_FOUND);
			break;
		}
		
		case DirMessageOps.OPERATION_UNREGISTER_SERVER: {
			int sk = msg.getSessionkey();
			registeredServers.remove(sk);
			System.out.println("Server removed. Sending message to " + clientAddr.getAddress() + "\n");
			response = new DirMessage(DirMessageOps.OPERATION_SERVER_UNREGISTERED);
			break;
		}

		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"" + "\n");
		}
		return response;

	}
}
