package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {

	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;

	public NFServer() throws IOException {
		/*
		 * Crear un socket servidor y ligarlo a cualquier puerto disponible
		 */
		InetSocketAddress serverSocketAddress = new InetSocketAddress(0);
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddress);
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		assert(stopServer == false);
		
		while(!stopServer) {
			try {
				serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
					// Espera nuevas conexiones
					Socket socket = serverSocket.accept();
					
					// Se ha establecido la conexión
					
					System.out.println(
							"\nNew client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
					System.out.print("(nanoFiles@nf-shared)");
					// Se realiza la comunicación con dicho cliente a través de hilos
					NFServerThread hiloServer = new NFServerThread(socket);
					hiloServer.run();
				} catch (SocketException se) {
					System.out.println("Server stopped.");
					break;
				} catch(SocketTimeoutException ste) {
					if(stopServer) {
						break;
					}
				} catch (IOException ex) {
					System.out.println("Server exception: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		
		/*
		 * Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		
		/*
		 * (Opcional) Crear un hilo nuevo de la clase NFServerThread, que llevará
		 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
		 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
		 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
		 * cliente conectado, no podremos tener más de un cliente conectado a este
		 * servidor.
		 */
		}
	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public void stopServer() {
		stopServer = true;
		closeServerSocket();
	}
	
	private void closeServerSocket() {
        if(serverSocket != null && !serverSocket.isClosed()) {
        	try {
            	serverSocket.close();
            } catch (IOException e) {
            	System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
	/**
	 * Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
	 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
	 * 3) Obtener el puerto de escucha del servidor etc.
	 */




}
