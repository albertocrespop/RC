package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * Crear dis/dos a partir del socket
		 */
		
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			while(!socket.isClosed()) {
				// Recibimos mensaje
				PeerMessage messageFromClient = PeerMessage.readMessageFromInputStream(dis);
				switch(messageFromClient.getOpcode()) {
					case PeerMessageOps.OPCODE_DOWNLOAD_REQUEST:
						String hash = new String(messageFromClient.getValue());
						FileInfo[] matches = FileInfo.lookupHashSubstring(NanoFiles.db.getFiles(), hash);
						if(matches.length != 1) {
							PeerMessage fnf = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
							fnf.writeMessageToOutputStream(dos);
							dos.close();
						}
						else {
							hash = matches[0].fileHash;
							
							// ENVIAMOS EL HASH AL CLIENTE
							PeerMessage sendHash = new PeerMessage(PeerMessageOps.OPCODE_SEND_HASH, hash.length(),
									hash.getBytes());
							sendHash.writeMessageToOutputStream(dos);
							
							// RECIBIMOS OK
							PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
							if(response.getOpcode() == PeerMessageOps.OPCODE_HASH_RECEIVED) {
								String filepath = NanoFiles.db.lookupFilePath(hash);
								// COPIAMOS DATOS DEL FICHERO
								File f = new File(filepath);
								DataInputStream disFile = new DataInputStream(new FileInputStream(f));
								long fLength = f.length();
								byte contenido[] = new byte[(int) fLength];
								disFile.readFully(contenido);
								disFile.close();
								// CREAMOS EL MENSAJE
								PeerMessage transfer = new PeerMessage(PeerMessageOps.OPCODE_TRANSFER_FILE,fLength,contenido);
								transfer.writeMessageToOutputStream(dos);
								dos.close();
							}
							else {
								PeerMessage ic = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
								ic.writeMessageToOutputStream(dos);
							}
						}
						break;
						
					
					default:
						messageFromClient = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
						break;
				}
						
			}
		} catch (IOException e) {
			System.out.println("Server exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		
		/*
		 * Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
		 */
		
		/*
		 * Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo.
		 */



	}




}
