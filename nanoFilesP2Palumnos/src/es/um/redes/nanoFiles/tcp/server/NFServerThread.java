package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.Socket;

public class NFServerThread extends Thread {
	/*
	 * Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServerComm.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */
	private Socket socket = null;
	
	public NFServerThread(Socket s) {
		socket = s;
	}
	
	public void run() {
		assert(socket != null);
		
		try {
			NFServerComm.serveFilesToClient(socket);
			socket.close();
		} catch (IOException e) {
			System.out.println("Server exception: " + e.getMessage());
			e.printStackTrace();
		}
	}


}
