package Software;

import Hardware.Interrupts;

public class InterruptHandling {
    private Escalonador escalonador;

    public void setEscalonador(Escalonador escalonador) {
        this.escalonador = escalonador;
    }

    public void handle(Interrupts irpt, int pc) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);
        switch (irpt){
            case intEscalonador:
                System.out.println("From: InterruptHandling -- escalonador.run()");
                escalonador.run();
                break;
        }
    }
}