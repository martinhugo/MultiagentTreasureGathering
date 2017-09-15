# Multiagent Treasure Gathering

## Description

Multiagent Treasure Gathering is an University Project, developed during our Master 1 in Artificial Intelligence at Pierre and Marie Curie University.

A team of agent is deployed in an unknown world to gather the more treasure possible. The world is made of room either empty or containing a treasure, separated by corridors.
It is in this project implemented as a graph, where each agents is initialized on a node.

Ech agent is equiped with a bag which can contain a limited capacity of a predifined type of treasure.
Treasure can be of two type: Treasures or Diamonds. If an agent decide to take a treasure of its type which is bigger than his bag's capacity, a percentage of the treasure is lost during the gathering.
If the gathered treasure isn't his type of treasure, nothing happens.

They can communicate in a limited range only and can't see what is in his neighborhood. They must send message to detect if someone is in the area.

The main goal is to make the agent cooperate to gather the more treasure possible. 
Several approaches were possible, from an exact resolution with meeting-points to more selfish approaches. 

## Chosen approach

Each agent initializes a list of knowledge, named World, which contains every information during the gathering. 

He sends messages depending on a number of modification since the last sending. If someone answer to the message, they exchange all the information they have.

Each agents take his own decision when it's come to collect treasures. An agent chooses to gather a treasure if: 
  * He has not finished his exploration and the treasure is just smaller than his bag capacity.
  * He has finished his exploration and the treasure is the most interesting among all the available treasures.
  
An agent only considers the treasure of its type. At first, he doesn't know his type and has to try to gather an interesting treasure.

A lot of collision may occur during the exploration or the gathering. A complex collision detection has been implemented by the team.

The agent which detects the collision sends a message to his neighborhood to warn the other agent and start the conflict resolution protocol. 
Each agent sends its world's reprensation to the other one and one of the agent is sending the other one a proposal of resolution which can be either move following this path or wait for me.
The moving agent will try to move following the path and may find himself blocked or in an other conflict. The conflict may be restarted by this agent with the waiting agent. 

The waiting agent may find, during an other conflict, a solution and may start to move. In this case he send a message to all the agents in conflict with him to tell them that he has moved.
