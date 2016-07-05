package com.agentes;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.*;

import java.util.ArrayList;    //MINE

public class AgenteCompradorLibros extends Agent {
	 
        int n=0; //Contador
        int N; //Numero de tareas
	private String[] ListaTareas; // La lista de tareas a realizar
        private AID[] AgentesEscogidos;
        
        
	// The list of known seller agents
	private AID[] AgentesVendedores;                    
	// Put agent initializations here
        ArrayList<AID> ofertantes;    //MINE
        ArrayList<Integer> ofertas;   //MINE
        
        //variables de cronómetro para actualizacion de feromonas:
        long inicio,fin,Tk;  // Tk=fin-inicio
        
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hola! Agente-Comprador "+
                        getAID().getName()+" es Leido.");

		//Obtener tareas a realizar como argumentos:
		Object[] args = getArguments();
                
              //imprimiendo argumentos.
                N=args.length;
                System.out.println("Cantidad de Tareas: "+ N);
                ListaTareas=new String[N];        //tamaño de lista tareas depende de la cantidad de tareas
                AgentesEscogidos=new AID[N];      //tamaño de lista de agentes escogidos depende de la cantidad de tareas
                
               for(int i=0; i<N ;i++) {
                ListaTareas [i] = (String) args[i];
                System.out.println("La Tarea "+i+" es: "+(String) args[i]);
               }
             
              //==========INICIO DE CRONÓMETRO
              Calendar time1 = Calendar.getInstance();
              inicio = time1.getTimeInMillis();
              
              
		if (args != null && args.length > 0) {
                    
			//ListaTareas [0] = (String) args[0]; Ya lo estoy haciendo arriba mk...
			//System.out.println("Libro Objetivo: "+ListaTareas[0]);                    
			// Add a TickerBehaviour that schedules a request to seller agents every 5 seconds
			addBehaviour(new TickerBehaviour(this, 5000) {
                          
				protected void onTick() {
                                System.out.println("\033[33m Inicio del onTick");
                    			
                                        System.out.println("Trato de Comprar "+
                                                ListaTareas[n]);
					
                                        // Update the list of seller agents. BUSQUEDA DE SERVICIOS EN DF
					DFAgentDescription template = 
                                                new DFAgentDescription();
					ServiceDescription sd = 
                                                new ServiceDescription();
					sd.setType("venta-libros");
                                        //sd.setName(STATE_READY);  //BUSCAR ESTE LIBRO EN ESPECIFICO WEY!!!
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Se encuentran los siguientes agentes de vendedor:");
						AgentesVendedores = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							AgentesVendedores[i] = result[i].getName();
							System.out.println(AgentesVendedores[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
                                        //FIN DE LA BUSQUEDA DE SERVICIOS EN EL DF

					// Perform the request
                                        //inicializar lista de vendedores y sus ofertas
                                        ofertantes=new ArrayList<AID>();       //MINE
                                        ofertas=new ArrayList<Integer>();          //MINE
                    
					myAgent.addBehaviour(new SolicitudCompra());
                                        
                                System.out.println("\033[33m Fin del onTick");
				}
                                
			} );
                }
		else {
			// Make the agent terminate
			System.out.println("No se encuentra el libro Especificado");
			doDelete();
		}
	}

	// Put agent clean-up operations here
        
        //AQUI DEBE REGRESAR AL HORMIGUERO!
        //Para actualizar feromonas necesito enviar: Tk,ListaTareas, AgentesEscogidos
        //myAgent.addBehaviour(new ActualizarFeromonas());
        
        
        
        //Y MUERE LA PUTA HORMIGA.
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agente-Comprador "+getAID().getName()+
                        " Terminado.");
	}

	
        
        
        /**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller 
	   agents the target book.
	 */                 
        
	private class SolicitudCompra extends Behaviour {
		AID bestSeller; // The agent who provides the best offer >>>>>      selectedSeller
		private int bestPrice;  // The best offered price                >>>>>      selectedPrice
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

                             
		public void action() {
                    
                                                
			switch (step) {
			case 0:
                            
                           // System.out.println("Hola mi pez, ando en el step 0...");
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < AgentesVendedores.length; ++i) {
					cfp.addReceiver(AgentesVendedores[i]);
				} 
				cfp.setContent(ListaTareas[n]);
				cfp.setConversationId("Comercio-libros");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Comercio-libros"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
                            //System.out.println("Hola mi pez, ando en el step 1... creando listas de posibilidades y eligiendo al final");
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer 
                                                
                                                //CREA LAS LISTAS DE POSIBILIDADES
                                                ofertantes.add(reply.getSender());                  //MINE
                                                ofertas.add(Integer.parseInt(reply.getContent()));  //MINE
                                                
//                                              int price = Integer.parseInt(reply.getContent());                                                
//						if (bestSeller == null || price < bestPrice) {
//							// This is the best offer at present
//							bestPrice = price;
//							bestSeller = reply.getSender();
//						}
                                        }
                                        
					repliesCnt++;
					
                                        if (repliesCnt >= AgentesVendedores.length) {
						// We received all replies....so:
                                                
                                                //HORA DA ROLETA!
                                                int SUM=0;    //como as ofertas são int, a soma delas é int tambm
                                                double prob[];
                                                prob=new double[ofertas.size()];                                               
                                                double sumprob=0;//como as probabilidades são double, a sua soma tambm é double
                                                
                                                
                                                //Fazer a soma das ofertas    
                                                for (int i=0;i<ofertas.size();i++){SUM=SUM+ofertas.get(i);}
                                                
                                                //calcular as probabilidades
                                                for (int i=0;i<ofertas.size();i++)
                                                {
                                                    prob[i]=sumprob+(double)ofertas.get(i)/SUM;
                                                    sumprob=prob[i];
                                                }
                                                    
                                                //Numero aleatório de 0 a 1
                                                double rnd=Math.random();
                                                
                                                //Escolha do agente vendedor:
                                                for (int i=0;i<ofertas.size();i++)
                                                {
                                                    if (bestSeller==null)
                                                    {                                                        
                                                        if(rnd<prob[i])
                                                        {
                                                        bestSeller = ofertantes.get(i);         //MINE
                                                        bestPrice = ofertas.get(i);             //MINE
                                                        }                                                    
                                                    }
                                                }
                                                
                                                
						step = 2; 
					}
                                        
				}
				else {
					block();
				}
				break;
			case 2:
                            //System.out.println("Hola mi pez, ando en el step 2...enviando solicitud");
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(ListaTareas[n]);
				order.setConversationId("Comercio-libros");
				order.setReplyWith("Orden"+System.currentTimeMillis());
				myAgent.send(order);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Comercio-libros"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
                            //System.out.println("Hola mi pez, ando en el step 3...esperando respuesta de solicitud ");
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						//llenar array de agentes escogidos
                                                AgentesEscogidos[n]=reply.getSender();
                                                System.out.println("\033[32m "+ListaTareas[n]+" Comprado con éxito de agente "+AgentesEscogidos[n].getName());
                                                
                                                System.out.println("\033[32m Precio = "+bestPrice);
                                                
                                                
                                                n++; //Avanzo a la siguiente tarea                                                
                                                if(n==N)
                                                {
                                                System.out.println("Los agentes finalmente escogidos fueron:");
                                                     for (int i=0;i<N;i++)
                                                     {
                                                         System.out.println("Para la tarea "+ListaTareas[i]+", la máquina "+ AgentesEscogidos[i].getName());
                                                     }
                                                     
                                                Calendar time2 = Calendar.getInstance();
                                                fin = time2.getTimeInMillis();                                                
                                                Tk=fin-inicio;
                                                System.out.println("Y la vaina duró "+Tk+", milisegundos ");
                                                
                                                
                                                //PASAR A ACTUALIZACION DE FEROMONAS.
                                                myAgent.doDelete();  //OJO Matar hormiga sólo cuando actualice todas las feromonas...eh?)
                                                }                                               
					}
					else {
                                            
                                            System.out.println("\033[33mfalla: libro solicitado ya está vendido.");						
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

		public boolean done() {
			if (step == 2 && bestSeller == null) {
				System.out.println("\033[31mFalla: "+ListaTareas[n]+" no esta a la venta");
			}
			return ((step == 2 && bestSeller == null) || step == 4);
		}
	}  // End of inner class RequestPerformer
}