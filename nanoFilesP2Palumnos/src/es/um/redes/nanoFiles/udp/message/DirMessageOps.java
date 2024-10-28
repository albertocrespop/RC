package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes del protocolo de comunicación con el directorio.
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	
	//Operaciones login
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_LOGINOK = "loginok";
	public static final String OPERATION_LOGINFAILED = "login_failed";
	
	//Operaciones logout
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_LOGOUTOK = "logoutok";
	
	//Operacion userlist
	public static final String OPERATION_USERLIST = "userlist";
	public static final String OPERATION_USERLISTOK = "userlistok";
	
	//Operacion register server port
	public static final String OPERATION_REGISTER_SERVER_PORT = "register_server_port";
	public static final String OPERATION_SERVER_PORT_REGISTERED = "server_port_registered";
	public static final String OPERATION_SERVER_PORT_FAILED = "server_port_failed";
	
	//Operacion unregister server port
	public static final String OPERATION_UNREGISTER_SERVER = "unregister_server";
	public static final String OPERATION_SERVER_UNREGISTERED = "server_unregistered";
	
	//Operacion lookup address
	public static final String OPERATION_LOOKUP_ADDRESS = "lookup_address";
	public static final String OPERATION_SEND_ADDRESS = "send_address";
	public static final String OPERATION_ADDRESS_NOT_FOUND = "address_not_found";



}
