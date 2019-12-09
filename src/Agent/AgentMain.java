package Agent;

import java.io.IOException;

public class AgentMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        Agent agent = new Agent ("64.106.20.224", 4444);
        // starts the display along???
        AgentDisplay display = new AgentDisplay(agent);
        //agent.setDisplay(display);
        display.startUp();


    }
}
