//package mas.behaviours;
//
//import jade.core.behaviours.OneShotBehaviour;
//import mas.agents.ExplorationAgent;
//import mas.protocol.FloodSynchroProtocol;
//
//public class StartSynchroBehaviour extends OneShotBehaviour{
//	
//	private static final long serialVersionUID = -2237816435251255416L;
//
//	public StartSynchroBehaviour(ExplorationAgent myAgent){
//		super(myAgent);
//	}
//	
//	@Override
//	public void action() {
//		// If an update needs to be sent
//		FloodSynchroProtocol floodProtocol = new FloodSynchroProtocol((ExplorationAgent) myAgent, true);
//		((ExplorationAgent) myAgent).getProtocols().put(floodProtocol.getConversationID(), floodProtocol);
//		((ExplorationAgent) myAgent).sendMessage(floodProtocol.getInitiationMessage());
//	}
//	
//}
