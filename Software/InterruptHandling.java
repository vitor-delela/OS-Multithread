package Software;

import Hardware.Interrupts;

// ------------------- I N T E R R U P C O E S  - rotinas de tratamento ---------------------------------- //

public class InterruptHandling {
    public void handle(Interrupts irpt, int pc) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);
    }
}