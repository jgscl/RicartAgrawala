package services;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import clients.Proceso;

@Singleton
@Path("servicio")	//ruta a la clase
public class Servicio {
	
	// Lista de procesos arrancados en esta máquina
	ArrayList<Proceso> procesos = new ArrayList<Proceso>();
	
	// Constructor del servicio
	public Servicio() {
		
	}
	
	
	/*
	 * URL para parar todos los procesos en la máquina
	*/
	@Path("apagar")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String apagar() {
		String ret = "Procesos ";
		int i;
		if(procesos.isEmpty()) {
			return "No hay procesos arrancados aún";
		}
		for(i=0; i<procesos.size();i++) {
			System.out.println("a");
			ret += procesos.get(i).getIdProceso() + " ";
			Proceso aux = procesos.get(i);
			procesos.remove(i);
			aux.parar();
			System.out.println("Aki mato a" + i);
		}
		return ret + " parados";
	}
	
	/*
	 * URL para arrancar un proceso con un identificador concreto
	 * @param idProceso Integer para elegir el identificador del nuevo proceso
	 * @return si se ha creado el nuevo proceso o si ya existe
	*/
	@Path("arrancar")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String arrancar(@QueryParam(value="idProceso") int idProceso) {
		for(int i=0;i<procesos.size();i++) {
			if(procesos.get(i).getIdProceso() == idProceso) {
				return "Ese proceso ya existe";
			}
		}
		Proceso newProceso = new Proceso(idProceso);
		procesos.add(newProceso);
		newProceso.arrancar();
		return "Proceso creado";
		
	}
	
	/*
	 * URL para pedir la computacion al coordinador
	 * @param idProceso Id del proceso a quien le vamos a pedir computar (coordinador)
	 * @return Response.Status.OK si el proceso existe o Response.Status.NOT_ACCEPTABLE si el proceso no existe
	*/
	@Path("computar")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response computar(@QueryParam(value="idProceso") int idProceso) {
		boolean existeProceso = false;
		for(int i=0;i<procesos.size();i++) {
			if(procesos.get(i).getIdProceso() == idProceso) {
				/* Si al computar se recibe true, es que no esta parado
				 * asi se envia un OK */
				if(procesos.get(i).computar()) {
					return Response.status(Response.Status.OK).build();
				}	
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();	
	}
	
	/*
	 * URL para informar a los procesos del nuevo coordinador
	 * @param coordinador Id del proceso del nuevo coordinador
	*/
	@Path("coordinador")
	@GET
	public void coordinador(@QueryParam(value="coordinador") int coordinador) {
		for(int i=0;i<procesos.size();i++) {
			procesos.get(i).coordinador(coordinador);
		}
	}
	
	/*
	 * URL para solicitar un proceso de elección a los procesos
	 * @param idProceso Id del proceso a quien le vamos a solicitar elecciones
	 * @param desdeProceso Id del proceso desde donde se pide elecciones (para su posterior respuesta)
	 * @return Response.Status.OK si el proceso existe o Response.Status.NOT_ACCEPTABLE si el proceso no existe
	*/
	@Path("eleccion")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String eleccion(@QueryParam(value="idProceso") int idProceso, @QueryParam(value="desdeProceso") int desdeProceso) {
		for(int i=0;i<procesos.size();i++) {
			if(procesos.get(i).getIdProceso() == idProceso) {
				boolean ret = procesos.get(i).eleccion(desdeProceso);
			}
		}
		return "";
	}
	
	/*
	 * URL para responder a un proceso ante una petición de eleccion
	 * @param idProceso Id del proceso a quien le vamos a responder
	*/
	@Path("ok")
	@GET
	public void ok(@QueryParam(value="idProceso") int idProceso) {
		for(int i=0;i<procesos.size();i++) {
			if(procesos.get(i).getIdProceso() == idProceso) {
				procesos.get(i).ok();
			}
		}
	}
	
	/*
	 * URL para parar un proceso con un identificador concreto
	 * @param idProceso Integer para elegir el identificador del proceso a parar
	 * @return si se ha parado el proceso o si no existe un proceso con ese id
	*/
	@Path("parar")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String parar(@QueryParam(value="idProceso") int idProceso) {
		for(int i=0;i<this.procesos.size();i++) {
			if(this.procesos.get(i).getIdProceso() == idProceso) {
				this.procesos.get(i).parar();
				this.procesos.remove(i);
				return "Proceso parado";
			}
		}
		return "No hay procesos con ese id";
	}
	
	/*
	 * URL para recoger que procesos están ejecutandose
	 * @return id de procesos ejecutandose
	*/
	@Path("procesos")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String numProcesos() {
		String ret = "";
		if(this.procesos.isEmpty()) {
			return ret;
		}
		for(int i=0;i<this.procesos.size();i++) {
			ret = ret + this.procesos.get(i).getIdProceso();
			ret = ret + " ";
		}
		return ret;
	}
	
	/*
	 * URL para comprobar el funcionamiento del servicio
	*/
	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Todo parece correcto";
	}
	
	
}
