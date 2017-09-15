package mas.agents;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import env.Environment;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.behaviours.FSMExploBehaviour;
import mas.beliefs.ConflictLog;
import mas.beliefs.World;
import mas.protocol.ConflictProtocol;
import mas.protocol.Protocol;
import mas.protocol.ProtocolState;


public class ExplorationAgent extends abstractAgent{

	private static final long serialVersionUID = -1784844593772918359L;
	private World world;
	private HashMap<String, Protocol> protocols;
	public final static String type = "EXPLORATION";



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time.
	 * 			1) set the agent attributes
	 *	 		2) add the behaviours
	 *
	 */
	protected void setup(){

		super.setup();

		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve
		final Object[] args = getArguments();
		if(args[0]!=null){
			deployAgent((Environment) args[0]);
			// Register the agent in the DFD
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType(ExplorationAgent.type);
			sd.setName(getLocalName());
			dfd.addServices(sd);

			try {
				DFService.register(this, dfd);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}


		//Creation du graphe et du sac Ã  dos.

		doWait(2000);


		this.world = new World(this);

		// Recuperation of all the agents in the system
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(ExplorationAgent.type);
		dfd.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, dfd);
			ArrayList<AID> agents = new ArrayList<AID>();
			
			for(DFAgentDescription agent:result){
				agents.add(agent.getName());
			}

			// Initialization of the agents list
			this.world.initAgents(agents, this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		this.protocols = new HashMap<String, Protocol>();

		//Add the behaviors
		addBehaviour(new FSMExploBehaviour(this));
		doWait((int)(Math.random() * 500));
		System.out.println("the agent "+this.getLocalName()+ " is started");
//		try {
//		System.out.println("Press a key to allow the agent "+this.getLocalName() +" to execute its first move");
//		System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}
	
	/**
	 * Remove all the waiting conflict of the agent, Deplacer méthode dans ExplorationAgent.
	 * 
	 */
	public void removeWaitingConflicts() {
		for(String id:this.getWorld().getGraph().getIds()){
			ConflictLog log = this.getWorld().getGraph().getNode(id).getConflictLog();
			
			if(log!=null){
				if(this.getProtocols().containsKey(log.getConflictID()) && ((ConflictProtocol) this.getProtocols().get(log.getConflictID())).getWaiting()){
					((ConflictProtocol) this.getProtocols().get(log.getConflictID())).setState(ProtocolState.DONE);
					((ConflictProtocol) this.getProtocols().get(log.getConflictID())).setWaiting(false);
				}
			}
		}

	}
	
	/**
	 * Returns true if no other protocol of this type already exists with the message's sender
	 * @param msg
	 * @return
	 */
	public boolean acceptNewProtocol(ACLMessage msg) {
		AID sender = msg.getSender();
		String protocolName = msg.getProtocol();
		
		Set<String> protocolsIDs = this.getProtocols().keySet();
		for(String id: protocolsIDs){
			Protocol protocol = this.getProtocols().get(id);	
			if(protocol.getClass().getName().equals(protocolName) && sender.equals(protocol.getDest()) && !protocol.getConversationID().equals(msg.getConversationId())){
				if(protocol.getClass().getName().equals(ConflictProtocol.class.getName()) && ((ConflictProtocol) protocol).getWaiting()){
					((ConflictProtocol) protocol).setWaiting(false);
					((ConflictProtocol) protocol).setState(ProtocolState.DONE);
				}
				return false;
			}
		}
		
		return true;
	}

	public HashMap<String, Protocol> getProtocols(){
		return protocols;
	}
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}


	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}


}
