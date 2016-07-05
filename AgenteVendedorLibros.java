

package com.agentes;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class AgenteVendedorLibros extends Agent {
	// The catalogue of books for sale (maps the title of a book to its price)
	private Hashtable catalogo;
	// The GUI by means of which the user can add books in the catalogue
	private GuiVentaLibros miGui;

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
                System.out.println("Bienvenido! Agente-Vendedor "+getAID().getName()+" es Leido.");
		catalogo = new Hashtable();

		// Create and show the GUI 
		miGui = new GuiVentaLibros(this);
		miGui.showGui();

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("venta-libros");
		sd.setName("Cartera de inversión JADE");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from buyer agents
		addBehaviour(new DemandaOfertaServidor());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new OrdenesCompraServidor());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		miGui.dispose();
		// Printout a dismissal message
		System.out.println("Agente Vendedor "+getAID().getName()+" terminado.");
	}

	/**
     This is invoked by the GUI when the user adds a new book for sale
	 */
	public void ActualizaCatalogo(final String titulo, final int precio) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				catalogo.put(titulo, new Integer(precio));
				System.out.println(titulo+" insertado en catálogo. Precio = "+precio);
			}
		} );
	}

	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Book-seller agents to serve incoming requests 
	   for offer from buyer agents.
	   If the requested book is in the local catalogue the seller agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
	private class DemandaOfertaServidor extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String titulo = msg.getContent();
				ACLMessage respuesta = msg.createReply();

				Integer price = (Integer) catalogo.get(titulo);
				if (price != null) {
					// The requested book is available for sale. Reply with the price
					respuesta.setPerformative(ACLMessage.PROPOSE);
					respuesta.setContent(String.valueOf(price.intValue()));
				}
				else {
					// The requested book is NOT available for sale.
					respuesta.setPerformative(ACLMessage.REFUSE);
					respuesta.setContent("No Disponible");
				}
				myAgent.send(respuesta);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Book-seller agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from buyer agents.
	   The seller agent removes the purchased book from its catalogue 
	   and replies with an INFORM message to notify the buyer that the
	   purchase has been sucesfully completed.
	 */
	private class OrdenesCompraServidor extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String titulo = msg.getContent();
				ACLMessage respuesta = msg.createReply();
                                
                                // EJECUTAR TAREA DE MANUFACTURA
                                
				Integer price = (Integer) catalogo.remove(titulo);
                                
				if (price != null) {
					respuesta.setPerformative(ACLMessage.INFORM);
					System.out.println(titulo+" vendido a agente "+msg.getSender().getName());
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					respuesta.setPerformative(ACLMessage.FAILURE);
					respuesta.setContent("no-disponible");
				}
				myAgent.send(respuesta);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
}
