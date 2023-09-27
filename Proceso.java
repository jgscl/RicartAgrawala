package clients;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;


public class Proceso extends Thread{
	private int idProceso;
	private int idCoordinador;
	private ArrayList<String> maquinas;
	private Map<Integer, WebTarget> webTargetProcesos;
	private Client client;
	
	// Mapa de estados en los que se puede encontrar el proceso
	private Map<Integer, String> estados;
	private int estadoActual;
	// Mapa de procesos Identificador-Máquina
	private Map<Integer, String> localizacionProcesos;
	private final String proyecto = "/obligatoria";
	private ArrayList<Future<Response>> respuestasRecibidas;
	private Semaphore eleccionPasiva;
	
	
	public Proceso(int idProceso) {
		this.eleccionPasiva = new Semaphore(0);
		this.idProceso = idProceso;
		
		// Inicializacion de maquinas disponibles
		this.maquinas = new ArrayList<String>();
		this.maquinas.add("localhost:8080");
		this.maquinas.add("172.28.230.124:8080");
		this.maquinas.add("172.20.2.35:8080");
		this.client=ClientBuilder.newClient();
		
		// Inicializacion de estados
		this.estados = new HashMap<Integer, String>();
		this.estados.put(0, "acuerdo");
		this.estados.put(1, "eleccion_activa");
		this.estados.put(2, "eleccion_pasiva");
		this.estados.put(3, "parado");
		
		// Inicializacion de mapa localizacion de procesos
		this.localizacionProcesos = new HashMap<Integer, String>();
		this.localizacionProcesos.put(1, "172.28.230.124:8080");
		this.localizacionProcesos.put(2, "172.28.230.124:8080");
		this.localizacionProcesos.put(3, "localhost:8080");
		this.localizacionProcesos.put(4, "localhost:8080");
		this.localizacionProcesos.put(5, "172.20.2.35:8080");
		this.localizacionProcesos.put(6, "172.20.2.35:8080");
		
		// Inicializacion de webtargets
		this.webTargetProcesos = new HashMap<Integer,WebTarget>();
		inicializarMapaUri(this.webTargetProcesos, this.localizacionProcesos);
		
	}
	
	private void inicializarMapaUri(Map<Integer, WebTarget> webTargetProcesos, Map<Integer, String> localizacionProcesos) {
		// TODO Auto-generated method stub
		for (Map.Entry<Integer, String> entry : this.localizacionProcesos.entrySet()) {
			URI newUri = UriBuilder.fromUri("http://" + entry.getValue() + this.proyecto).build();
			WebTarget newWebTarget = this.client.target(newUri);
			this.webTargetProcesos.put(entry.getKey(), newWebTarget);
		}
	}

	@Override
	public void run() {
		System.out.println("Proceso corriendo " + this.getIdProceso());
		
		boolean salir = false;
		while(!salir) {
			String estadoProceso = this.estados.get(this.estadoActual);
			switch(estadoProceso) {
			//ESTADO ACUERDO
			case "acuerdo":
				
					
				if(this.idCoordinador == this.idProceso) {
					this.computar();
				}else {
					this.solicitarTareaCoordinador();
				}
				
					
				
				break;
			//ESTADO ELECCION ACTIVA
			case "eleccion_activa":
				//MANDAMOS ELECCION A LOS PROCESOS CON IDENTIFICADOR MAYOR
				
				for (Map.Entry<Integer, WebTarget> entry : webTargetProcesos.entrySet()) {
					if(entry.getKey()>this.idProceso) {
						System.out.println("Proceso en eleccion activa a proceso con Id: " + entry.getKey());
						entry.getValue().path("rest").path("servicio").path("eleccion").
							queryParam("idProceso",entry.getKey()).queryParam("desdeProceso",this.idProceso).
							request(MediaType.TEXT_PLAIN).get(String.class);
						
						
						
					}
				}
		
				//Si no está en estado de eleccion activa
				if(this.estadoActual!=2) {
					this.idCoordinador = this.idProceso;
					this.estadoActual = 1;
					for(int i=0;i<this.maquinas.size();i++) {
						Client client=ClientBuilder.newClient();
						URI uri=UriBuilder.fromUri("http://" + this.maquinas.get(i) + this.proyecto).build();
						WebTarget target = client.target(uri);
						Response response = target.path("rest").path("servicio").path("coordinador").
							queryParam("coordinador", this.idProceso).request().get();
					}
				}
				
				break;
			//ESTADO ELECCION PASIVA
			case "eleccion_pasiva":
				try {
					this.eleccionPasiva.acquire(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			//ESTADO PARAR
			case "parado":
				salir = true;
				System.out.println("Proceso parado " + this.idProceso);
			}
			
		}

		
	}

	private boolean solicitarTareaCoordinador() {
		Random r = new Random();
		Response response = webTargetProcesos.get(this.idCoordinador).path("rest").path("servicio").path("computar").
			queryParam("idProceso", this.idCoordinador).request().get();
		
		if(response.getStatusInfo() != Response.Status.OK) {
			this.estadoActual = 1;
		}
		try {
			Thread.sleep(r.nextInt(500)  + 500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		// TODO Auto-generated method stub
		
	}

	public void ok() {
		System.out.println("Soy " + this.idProceso + " y he recibido OK!");
		this.estadoActual = 2;
	}
	
	public boolean eleccion(int desdeProceso) {
		this.estadoActual = 1;
		
		System.out.println("Soy " + this.idProceso + " le mando OK a " + desdeProceso);
		
		webTargetProcesos.get(desdeProceso).path("rest").path("servicio").path("ok").
			queryParam("idProceso",desdeProceso).
			request(MediaType.TEXT_PLAIN).get(String.class);
		return true;
	}
	
	
	public void coordinador(int idCoordinador) {
		System.out.println("Soy " + this.idProceso + " y el coordinador es " + idCoordinador);
		this.idCoordinador = idCoordinador;
		this.estadoActual = 0;
		if(this.eleccionPasiva.getQueueLength() == 1)
			this.eleccionPasiva.release(1);
	}
	
	// Se arranca cuando se crea
	public void arrancar() {
		
		this.estadoActual = 1;
		this.start();
	}
	
	public void parar() {
		this.estadoActual = 3;	// PARAR PROCESO (SALE BUCLE RUN)
	}
	
	public boolean computar() {
		Random r = new Random();
		if(this.estadoActual == 3) {
			return false;
		} 
		
		try {
			System.out.println("Soy " + this.getIdProceso() + " y computo ");
			Thread.sleep(r.nextInt(500)  + 500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	
	}
	
	private int cuantosMayores(int idProceso) {
		int contador=0;
		for (Map.Entry<Integer, String> entry : localizacionProcesos.entrySet()) {
			if(entry.getKey() > idProceso) {
				contador++;
			}
		}
		return contador;
	}
	
	
	/*
	 * GETTER & SETTERS DE LOS PARÁMETROS DEL PROCESO
	 */
	
	// IdProceso
	public int getIdProceso() {
		return this.idProceso;
	}
	
	public void setIdProceso(int idProceso) {
		this.idProceso = idProceso;
	}
	
	// IdCoordinador
	public int getIdCoordinador() {
		return this.idCoordinador;
	}
	
	public void setIdCoordinador(int idCoordinador) {
		this.idCoordinador = idCoordinador;
	}
	
	// Estado
	
	public int getEstado() {
		return this.estadoActual;
	}
	
	public void setEstado(int estado) {
		this.estadoActual = estado;
	}
	
}
