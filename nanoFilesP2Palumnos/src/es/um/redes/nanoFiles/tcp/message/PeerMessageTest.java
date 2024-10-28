package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		byte[] b = "Hola Virgilio me cago en tus muelas.".getBytes();
		PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_TRANSFER_FILE, b.length, b);
		msgOut.writeMessageToOutputStream(fos);
		
		
		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		}
		else if(msgOut.getLength() != msgIn.getLength()){
			System.err.println("Length does not match!");
		}
		else if(msgOut.getValue().equals(msgIn.getValue())) {
			System.err.println("Value does not match!");
		}
		else {
			System.out.println("Message match!");
			System.out.println(msgIn.getOpcode() + "\n" + msgIn.getLength());
			for(byte dato : msgIn.getValue()) {
				char letra = (char) dato;
				System.out.print(letra);
			}
		}
	}

}