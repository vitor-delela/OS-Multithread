package Software;

import Hardware.Interrupts;

public class InterruptHandling {
    private Escalonador escalonador;
    private GerenciaProcesso gerenciadorProcesso;

    public void configInterruptHandling(Escalonador escalonador, GerenciaProcesso gerenciaProcesso) {
        this.escalonador = escalonador;
        this.gerenciadorProcesso = gerenciaProcesso;
    }

    public void handle(Interrupts irpt, int pc, int runningPid) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);
        switch (irpt){
            case intEscalonador:
                System.out.println("From: InterruptHandling -- escalonador.run()");
                escalonador.run();
                break;
            case intSTOP:
                System.out.println("From: InterruptHandling -- int STOP");
                gerenciadorProcesso.finish(gerenciadorProcesso.getProcessByID(runningPid));
                if (gerenciadorProcesso.getProntos().size() > 0){
                    if (escalonador.getPosicao()>0) escalonador.setPosicao( escalonador.getPosicao() - 1);
                    escalonador.getCpu().setContext(gerenciadorProcesso.getProntos().get(escalonador.getPosicao()).getContexto());
                }
                break;
        }
    }

    public Escalonador getEscalonador() {
        return escalonador;
    }
}