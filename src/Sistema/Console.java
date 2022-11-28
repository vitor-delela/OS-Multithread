package Sistema;

import Hardware.CPU;
import Software.GerenciaProcesso;
import Software.IORequest;
import Software.PCB;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Console extends Thread {

    public static Semaphore SEMA_CONSOLE = new Semaphore(0);

    private CPU cpu;
    private Scanner reader;
    private GerenciaProcesso gerenteProcesso;

    public static LinkedList<IORequest> requisicoesIO = new LinkedList<>();
    public static LinkedList<Integer> IOsCompletasPIDs = new LinkedList<>();

    public Console(CPU cpu, GerenciaProcesso gerenteProcesso) {
        super("Console");
        this.cpu = cpu;
        this.reader = new Scanner(System.in);
        this.gerenteProcesso = gerenteProcesso;
    }

    @Override
    public void run() {
        while (true) {
            try {
                SEMA_CONSOLE.acquire();
                // Entrou algum processo bloqueado.
                IORequest ioRequest = requisicoesIO.removeFirst();
                if (ioRequest.getOperationType() == IORequest.OperationTypes.READ) {
                    read(ioRequest.getProcess());
                } else {
                    write(ioRequest.getProcess());
                }
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }

    private void read(PCB process) {
        System.out.println("\n[Process ID = " + process.getId() + " - READ] [CONSOLE] - Esperando input do usuário...:\n");
        int input = reader.nextInt();
        System.out.println("\n[CONSOLE] - Recebeu o input do usuário [OK]\n");
        process.setIOValue(input);
        addCompletedIO(process.getId());
        removeIORequest(process.getId());
        if (gerenteProcesso.getProntos().size() <= 0) {
            cpu.getInterruptHandling().rotinaSemProcessosListados();
        }
    }

    private void write(PCB process) {
        System.out.println("\n[Process ID = " + process.getId() + " - WRITE]\n");
        int endereco = gerenteProcesso.gerenciaMemoria.translate(process.getReg()[8], process.getAllocatedPages());
        int output = cpu.m[endereco].p;
        process.setIOValue(output);
        addCompletedIO(process.getId());
        removeIORequest(process.getId());
        if (gerenteProcesso.ListaProntos.size() <= 0) {
            cpu.getInterruptHandling().rotinaSemProcessosListados();
        }
    }

    public static void addCompletedIO(int id) {
        IOsCompletasPIDs.add(id);
    }

    public static int getFirstIOProcessId() {
        return IOsCompletasPIDs.removeFirst();
    }

    private static void removeIORequest(int processId) {
        for (int i = 0; i < requisicoesIO.size(); i++) {
            if (requisicoesIO.get(i).getProcess().getId() == processId) {
                requisicoesIO.remove(i);
            }
        }
    }

}

