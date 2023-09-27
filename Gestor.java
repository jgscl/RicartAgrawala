/*
 * Clase Gestora de procesos y nodos en el algoritmo Bully
 * Borja Lorenzo Adajas
 * Juan Gil Sancho
 * Comprobaciones previas:
 * 		1- Ejecutar la clase Servicio en cada nodo en un servidor apache tomcat
 * 		2- Anotar las respectivas IPs de cada maquina en 3 sitios:
 * 			1- En la inicialización del mapa localizacionProcesos en esta misma clase al final
 * 			2- En la inicialización del mapa localizacionProcesos en la clase Proceso de cada maquina
 * 			3- En el ArrayList maquinas en la clase Proceso de cada maquina
 */

package clients;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

@Singleton
@Path("gestor")
public class Gestor {
	 
	Map<Integer, String> localizacionProcesos;
	static Map<Integer, WebTarget> webTargetProcesos;
	String proyecto = "/obligatoria";
	
	
	public static void main(String[] args)
    {
		
        Scanner sc = new Scanner(System.in);
        
        
        
        Gestor gestor = new Gestor();
        Set<String> procesos = new HashSet<String>();
        
        Boolean salir = false;
        while(!salir) {
        	System.out.println("MENU");
        	System.out.println(" 1- Crear proceso ");
        	System.out.println(" 2- Eliminar proceso ");
        	System.out.println(" 3- Ver procesos activos ");
        	System.out.println(" 4- Apagar ");
        	Integer opcion = sc.nextInt();
        	switch(opcion) {
        		case 1:
        			System.out.println("Introduce numero de proceso a arrancar");
        			System.out.println("(1-6)");
        			Integer procesoArrancar = sc.nextInt();
        			webTargetProcesos.get(procesoArrancar).path("rest").path("servicio").path("arrancar").
					queryParam("idProceso",procesoArrancar).
					request(MediaType.TEXT_PLAIN).get(String.class);
        			
        			break;
        		case 2:
        			System.out.println("Introduce numero de proceso a parar");
        			System.out.println("(1-6)");
        			Integer procesoParar = sc.nextInt();
        			webTargetProcesos.get(procesoParar).path("rest").path("servicio").path("parar").
					queryParam("idProceso",procesoParar).
					request(MediaType.TEXT_PLAIN).get(String.class);
        			break;
        		case 3:
        			for (Map.Entry<Integer, WebTarget> entry : webTargetProcesos.entrySet()) {
        				procesos.add(entry.getValue().path("rest").path("servicio").path("procesos").
    							request(MediaType.TEXT_PLAIN).get(String.class));
        				
        			}
        			System.out.println(procesos);
        			procesos.clear();
        			break;
        		case 4:
        			for (Map.Entry<Integer, WebTarget> entry : webTargetProcesos.entrySet()) {
        				entry.getValue().path("rest").path("servicio").path("parar").
							queryParam("idProceso",entry.getKey()).
							request(MediaType.TEXT_PLAIN).get(String.class);
        			}
        			salir = true;
        			sc.close();
        			break;
        			
        	}
        }
 		
    }

	
	public Gestor() {
		this.localizacionProcesos = new HashMap<Integer, String>();
		this.localizacionProcesos.put(1, "172.28.230.124:8080");
		this.localizacionProcesos.put(2, "172.28.230.124:8080");
		this.localizacionProcesos.put(3, "localhost:8080");
		this.localizacionProcesos.put(4, "localhost:8080");
		this.localizacionProcesos.put(5, "172.20.2.35:8080");
		this.localizacionProcesos.put(6, "172.20.2.35:8080");
		
		this.webTargetProcesos = new HashMap<Integer,WebTarget>();
		Client client=ClientBuilder.newClient();
		for (Map.Entry<Integer, String> entry : this.localizacionProcesos.entrySet()) {
			URI newUri = UriBuilder.fromUri("http://" + entry.getValue() + proyecto).build();
			System.out.println("localizacion procesos" + entry.getKey() + ", " + entry.getValue() +  ", " + newUri);
			WebTarget newWebTarget = client.target(newUri);
			this.webTargetProcesos.put(entry.getKey(), newWebTarget);
		}
		
	}
}
