package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected void testCommunicationWithDirectory(String directoryHostname) throws IOException {
		assert (NanoFiles.testMode);
		System.out.println("[testMode] Testing communication with directory...");
		/*
		 * Crea un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * lo utiliza para hacer una prueba de comunicación con el directorio.
		 */
		DirectoryConnector directoryConnector = new DirectoryConnector(directoryHostname);
		if (directoryConnector.testSendAndReceive()) {
			System.out.println("[testMode] Test PASSED!");
		} else {
			System.err.println("[testMode] Test FAILED!");
		}
	}

	/**
	 * Método para conectar con el directorio y obtener la "sessionKey" que se
	 * deberá utilizar en lo sucesivo para identificar a este cliente ante el
	 * directorio
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	protected boolean doLogin(String directoryHostname, String nickname) throws IOException {

		/*
		 * Debe crear un objeto DirectoryConnector a partir del parámetro
		 * directoryHostname y guardarlo en el atributo correspondiente para que pueda
		 * ser utilizado por el resto de métodos de esta clase. A continuación,
		 * utilizarlo para comunicarse con el directorio y tratar de realizar el
		 * "login", informar por pantalla del éxito/fracaso e imprimir la clave de
		 * sesión asignada por el directorio. Devolver éxito/fracaso de la operación.
		 */
		boolean result = false;
		
		directoryConnector = new DirectoryConnector(directoryHostname);
		if(directoryConnector.getDirectoryAddress() != null) {
			result = directoryConnector.logIntoDirectory(nickname);
		}
		
		if(result) {
			System.out.println("Login succesful. Session key: " + directoryConnector.getSessionKey());
		}else {
			System.out.println("Login could not be performed.");
		}
		
		return result;
	}

	/**
	 * Método para desconectarse del directorio: cerrar sesión y dar de baja el
	 * nombre de usuario registrado
	 * @throws IOException 
	 */
	public boolean doLogout() throws IOException {
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * dar de baja a este usuario. Se debe enviar la clave de sesión para
		 * identificarse. Devolver éxito/fracaso de la operación.
		 */

		boolean result = false;

		result = directoryConnector.logoutFromDirectory();
		
		if(result) {
			System.out.println("Logout succesful.");
		}
		else {
			System.out.println("Logout could not be performed.");
		}

		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 * @throws IOException 
	 */
	protected boolean getAndPrintUserList() throws IOException {
		/*
		 * Obtener la lista de usuarios registrados. Comunicarse con el directorio
		 * (a través del directoryConnector) para obtener la lista de nicks registrados
		 * e imprimirla por pantalla. Devolver éxito/fracaso de la operación.
		 */
		boolean result = false;
		String[] users = directoryConnector.getUserList();
		if(users != null) {
			if(users.length > 0) {
				result = true;
			}
			
			System.out.println("Currently registered users: ");
			for(String user : users) {
				System.out.println("- " + user);
			}
			LinkedList<String> servers = new LinkedList<String>();
			for(String server : users) {
				if(directoryConnector.lookupServerAddrByUsername(server) != null) {
					servers.add(server);
				}
			}
			if(servers.size() > 0) {
				System.out.println("Registered servers: ");
				for(String server : servers) {
					System.out.println("- " + server);
				}
			}
		}

		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected boolean getAndPrintFileList() {
		/*
		 * TODO: Obtener la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para obtener la lista de ficheros e imprimirla
		 * por pantalla (método FileInfo.printToSysout). Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;



		return result;
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado
	 * 
	 * @param serverPort el puerto en el que está escuchando nuestro servidor de
	 *                   ficheros
	 */

	public boolean registerFileServer(int serverPort) {
		/*
		 * Darse de alta en el directorio como servidor. Comunicarse con el
		 * directorio (a través del directoryConnector) para enviar el número de puerto
		 * TCP en el que escucha el servidor de ficheros que habremos arrancado
		 * previamente. Se debe enviar la clave de sesión para identificarse. Devolver
		 * éxito/fracaso de la operación.
		 */
		// Llamamos al directory connector para registrar el puerto en el directorio
		boolean result = directoryConnector.registerServerPort(serverPort);
		if(result) {
			System.out.println("User registered as a server in Directory.");
		}
		else {
			System.out.println("User couldn't be registered as a server. Posibble reasons:");
			System.out.println("- Couldn't establish connection with the directory.");
			System.out.println("- Nickname already registered as a server.");
		}


		return result;
	}

	/**
	 * Método para enviar al directorio la lista de ficheros que este peer servidor
	 * comparte con el resto (ver método filelist).
	 * 
	 */
	protected boolean publishLocalFiles() {
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * enviar la lista de ficheros servidos por este peer. Los ficheros de la
		 * carpeta local compartida están disponibles en NanoFiles.db). Se debe enviar
		 * la clave de sesión para identificarse. Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;



		return result;
	}

	/**
	 * Método para consultar al directorio el nick de un peer servidor y obtener
	 * como respuesta la dirección de socket IP:puerto asociada a dicho servidor
	 * 
	 * @param nickname el nick del servidor por cuya IP:puerto se pregunta
	 * @return La dirección de socket del servidor identificado por dich nick, o
	 *         null si no se encuentra ningún usuario con ese nick que esté
	 *         sirviendo ficheros.
	 */
	private InetSocketAddress lookupServerAddrByUsername(String nickname) {
		/*
		 * Obtener IP:puerto de un servidor de ficheros a partir de su nickname.
		 * Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar la dirección de socket en la que el usuario con 'nickname' está
		 * sirviendo ficheros. Si la operación fracasa (no se obtiene una respuesta con
		 * IP:puerto válidos), se debe devolver null.
		 */
		InetSocketAddress serverAddr = null;
		InetSocketAddress serverAddrTemp = directoryConnector.lookupServerAddrByUsername(nickname);
		if(serverAddrTemp != null) {
			serverAddr = serverAddrTemp;
		}

		return serverAddr;
	}

	/**
	 * Método para obtener la dirección de socket asociada a un servidor a partir de
	 * una cadena de caracteres que contenga: i) el nick del servidor, o ii)
	 * directamente una IP:puerto.
	 * 
	 * @param serverNicknameOrSocketAddr El nick o IP:puerto del servidor por el que
	 *                                   preguntamos
	 * @return La dirección de socket del peer identificado por dicho nick, o null
	 *         si no se encuentra ningún peer con ese nick.
	 */
	public InetSocketAddress getServerAddress(String serverNicknameOrSocketAddr) {
		InetSocketAddress fserverAddr = null;
		/*
		 * Averiguar si el nickname es en realidad una cadena "IP:puerto", en cuyo
		 * caso no es necesario comunicarse con el directorio (simplemente se devuelve
		 * un InetSocketAddress); en otro caso, utilizar el método
		 * lookupServerAddrByUsername de esta clase para comunicarse con el directorio y
		 * obtener la IP:puerto del servidor con dicho nickname. Devolver null si la
		 * operación fracasa.
		 */
		if (serverNicknameOrSocketAddr.contains(":")) { // Then it has to be a socket address (IP:port)
			/*
			 * Extraer la dirección IP y el puerto de la cadena y devolver un
			 * InetSocketAddress. Para convertir un string con la IP a un objeto InetAddress
			 * se debe usar InetAddress.getByName()
			 */
			String[] Address = serverNicknameOrSocketAddr.split(":");
			try {
				InetAddress IP = InetAddress.getByName(Address[0]);
				int port = Integer.parseInt(Address[1]);
				fserverAddr = new InetSocketAddress(IP,port);
			} catch (UnknownHostException e) {
				System.err.println("Address conversion error: " + e.getMessage());
				e.printStackTrace();
			}


		} else {
			/*
			 * Si es un nickname, preguntar al directorio la IP:puerto asociada a
			 * dicho peer servidor.
			 */
			fserverAddr = lookupServerAddrByUsername(serverNicknameOrSocketAddr);
		}
		return fserverAddr;
	}

	/**
	 * Método para consultar al directorio los nicknames de los servidores que
	 * tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 */
	public boolean getAndPrintServersNicknamesSharingThisFile(String fileHashSubstring) {
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar por aquellos servidores que están sirviendo un determinado fichero,
		 * y obtener una lista con sus nicknames. Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;



		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por su hash.
	 * 
	 * @param fileHashSubstring una subcadena del hash del fichero por el que se
	 *                          pregunta
	 * @return Una lista de direcciones de socket de los servidores que comparten
	 *         dicho fichero, o null si dicha subcadena del hash no identifica
	 *         ningún fichero concreto (no existe o es una subcadena ambigua)
	 * 
	 */
	public LinkedList<InetSocketAddress> getServerAddressesSharingThisFile(String downloadTargetFileHash) {
		LinkedList<InetSocketAddress> serverAddressList = null;
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar por aquellos servidores que están sirviendo un determinado fichero,
		 * y obtener una lista con sus nicknames (método
		 * getServerNicknamesSharingThisFile). A continuación, obtener la dirección de
		 * socket de cada servidor a partir de su nickname (método getServerAddress), y
		 * devolver una lista con dichas direcciones. Devolver null si la operación
		 * fracasa.
		 * 
		 */




		return serverAddressList;
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	public boolean unregisterFileServer() {
		/*
		 * TODO: Comunicarse con el directorio (a través del directoryConnector) para
		 * darse de baja como servidor de ficheros. Se debe enviar la clave de sesión
		 * para identificarse.
		 */
		boolean result = false;

		result = directoryConnector.unregisterServer();

		if(result) {
			System.out.println("User unregistered as a server in Directory.");
		}
		else {
			System.out.println("User could not be unregistered as a server in Directory.");
		}
		
		return result;
	}

	protected InetSocketAddress getDirectoryAddress() {
		return directoryConnector.getDirectoryAddress();
	}

}
