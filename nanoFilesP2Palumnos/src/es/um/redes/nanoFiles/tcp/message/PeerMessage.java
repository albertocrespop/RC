package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;

	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	
	private long length;
	
	private byte[] value;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	// HashReceived y InvalidCode
	
	public PeerMessage(byte op) {
		opcode = op;
	}
	
	// CONSTRUCTOR PARA TransferFile , DownloadRequest y SendHash
	
	public PeerMessage(byte op, long longitud, byte[] valor) {
		opcode = op;
		length = longitud;
		value = Arrays.copyOf(valor, valor.length);
	}
	
	/*
	 * TODO: Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	
	public byte getOpcode() {
		return opcode;
	}

	public long getLength() {
		return length;
	}
	
	public byte[] getValue() {
		return Arrays.copyOf(value, value.length);
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}
	
	public void setValue(byte[] value) {
		this.value = Arrays.copyOf(value, value.length);
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
		case(PeerMessageOps.OPCODE_HASH_RECEIVED):
		case(PeerMessageOps.OPCODE_FILE_NOT_FOUND):
			message.setOpcode(opcode);
			break;
		case(PeerMessageOps.OPCODE_SEND_HASH):
		case(PeerMessageOps.OPCODE_DOWNLOAD_REQUEST):
		case(PeerMessageOps.OPCODE_TRANSFER_FILE):
			message.setOpcode(opcode);
			message.setLength(dis.readLong());
			byte[] b = new byte[(int) message.getLength()];
			dis.readFully(b);
			message.setValue(b);
			break;

		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case(PeerMessageOps.OPCODE_FILE_NOT_FOUND):
		case(PeerMessageOps.OPCODE_HASH_RECEIVED):
			break;
		case(PeerMessageOps.OPCODE_SEND_HASH):
		case(PeerMessageOps.OPCODE_DOWNLOAD_REQUEST):
		case(PeerMessageOps.OPCODE_TRANSFER_FILE):
			dos.writeLong(length);
			dos.write(value);
			break;
		
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
