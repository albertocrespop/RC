import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

public class Pruebamensajes {
	public static void main(String[] args) {
		DirMessage mensaje = new DirMessage(DirMessageOps.OPERATION_LOOKUP_ADDRESS, "robelto");
		System.out.println(mensaje);
		InetSocketAddress host = new InetSocketAddress("127.0.0.1", 80);
		DirMessage respuesta = new DirMessage(DirMessageOps.OPERATION_SEND_ADDRESS, host);
		System.out.println(respuesta);
		DirMessage respuesta2 = DirMessage.fromString(respuesta.toString());
		System.out.println(respuesta2);
	}
}
