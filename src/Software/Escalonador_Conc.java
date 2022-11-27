package Software;

import Hardware.CPU;
import Sistema.Shell;

import java.util.concurrent.Semaphore;

public class Escalonador_Conc extends Thread {

    public static Semaphore SEMA_ESCALONADOR = new Semaphore(0);

    private CPU cpu;
    private GerenciaProcesso gerenteProcesso;

    public Escalonador_Conc(CPU cpu, GerenciaProcesso gerenteProcesso) {
        super("Escalonador");
        this.cpu = cpu;
        this.gerenteProcesso = gerenteProcesso;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Espera processo pronto.
                SEMA_ESCALONADOR.acquire();
                if (gerenteProcesso.READY_LIST.size() > 0) {
                    gerenteProcesso.RUNNING = gerenteProcesso.READY_LIST.removeFirst();
                    PCB nextProccess = gerenteProcesso.RUNNING;
                    System.out.println("\nEscalonando processo com ID = " + nextProccess.getId());
                    cpu.setContext(nextProccess.getContexto());
                    // CPU liberada.
                    cpu.SEMA_CPU.release();
                }else {
                    Shell.SEMA_SHELL.release();
                }
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }

}
