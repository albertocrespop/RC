package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;



public class NFServerSimple {

	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;
	
	// Hilo que actúa como entrada: usado para detectar fgstop
	
	class LectorEntrada implements Runnable{
		private BufferedReader buffer;
		private volatile boolean stopped = false;
		
		public LectorEntrada(BufferedReader buf) {
	        buffer = buf;
	    }
		
		public void run() {
			String linea;
			   try {
				while (!stopped && (linea = buffer.readLine()) != null) {
				       if (linea.contains(STOP_SERVER_COMMAND)) {
				           stopped = true;
				       }
				   }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public boolean isStopped() {
			return stopped;
		}
	}
	
	public NFServerSimple() throws IOException {
		/*
		 * Crear una direción de socket a partir del puerto especificado
		 */
		try{
			InetSocketAddress serverSocketAddress = new InetSocketAddress(PORT);
			serverSocket = new ServerSocket();
			serverSocket.bind(serverSocketAddress);
		}
		catch(BindException be) {
			InetSocketAddress serverSocketAddress = new InetSocketAddress(0);
			serverSocket = new ServerSocket();
			serverSocket.bind(serverSocketAddress);
		}
		serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
		/*
		 * Crear un socket servidor y ligarlo a la dirección de socket anterior
		 */
		


	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * @throws IOException  
	 */
	public void run() throws IOException {
		/*
		 * Comprobar que el socket servidor está creado y ligado
		 */
		
		
		
		/*
		 * Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		BufferedReader bufer = new BufferedReader(new InputStreamReader(System.in));
		LectorEntrada entrada = new LectorEntrada(bufer);
		Thread hiloEntrada = new Thread(entrada);
		hiloEntrada.start();
		
		if(serverSocket != null && serverSocket.getInetAddress() != null) {
			System.out.println("NFServerSimple running on " + serverSocket.getInetAddress() + 
					":" + serverSocket.getLocalPort());
			System.out.println("Enter 'fgstop' to stop server");
			
			while(true) {
				try {
					Socket socket = serverSocket.accept();
					System.out.println("New connection established: " +
						socket.getInetAddress().toString() + ":" + socket.getPort());
					NFServerComm.serveFilesToClient(socket);
					socket.close();
					
				} catch(SocketTimeoutException se) {
					if(entrada.isStopped()) {
						break;
					}
				}
				catch (IOException e) {
					System.out.println("Server exception: " + e.getMessage());
					e.printStackTrace();
					break;
				}
			}
		}
		
		
		/*
		 * Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */


		serverSocket.close();
		System.out.println("NFServerSimple stopped. Returning to the nanoFiles shell...");
	}
}
