package Sistema;

import Hardware.CPU;
import Software.GerenciaProcesso;
import Software.PCB;

import java.util.concurrent.Semaphore;

public class Dispatcher extends Thread {

    public static Semaphore SEMA_DISPATCHER = new Semaphore(0);

    private CPU cpu;
    private GerenciaProcesso gerenteProcesso;

    public Dispatcher(CPU cpu, GerenciaProcesso gerenteProcesso) {
        super("Dispatcher");
        this.cpu = cpu;
        this.gerenteProcesso = gerenteProcesso;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Espera processo pronto.
                SEMA_DISPATCHER.acquire();
                if (gerenteProcesso.getProntos().size() > 0) {
                    gerenteProcesso.RUNNING = gerenteProcesso.READY_LIST.removeFirst();
                    PCB nextProccess = gerenteProcesso.RUNNING;
                    System.out.println("\nEscalonando processo com ID = " + nextProccess.getId());
                    cpu.setContext(nextProccess.getContexto());
                    // CPU liberada.
                    cpu.SEMA_CPU.release();
                }
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }

}
