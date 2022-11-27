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

    public static LinkedList<IORequest> IO_REQUESTS = new LinkedList<>();
    public static LinkedList<Integer> FINISHED_IO_PROCESS_IDS = new LinkedList<>();

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
                IORequest ioRequest = IO_REQUESTS.removeFirst();
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
        String inputAsString = reader.nextLine();
        System.out.println("\n[CONSOLE] - Recebeu o input do usuário [OK]\n");
        int input;
        try {
            input = Integer.parseInt(inputAsString);
        } catch (NumberFormatException error) {
            System.out
                    .println("\n[Console] O valor de IO digitado não é um número, será usado o valor -1 neste caso.\n");
            input = -1;
        }
        process.setIOValue(input);
        addFinishedIOProcessId(process.getId());
        removeIORequest(process.getId());
        if (gerenteProcesso.getProntos().size() <= 0) {
            cpu.getInterruptHandling().noOtherProcessRunningRoutine();
        }
    }

    private void write(PCB process) {
        System.out.println("\n[Process ID = " + process.getId() + " - WRITE]\n");
        int physicalAddress = gerenteProcesso.gerenciaMemoria.translate(process.getReg()[8], process.getAllocatedPages());
        int output = cpu.m[physicalAddress].p;
        process.setIOValue(output);
        addFinishedIOProcessId(process.getId());
        removeIORequest(process.getId());
        if (gerenteProcesso.getProntos().size() <= 0) {
            cpu.getInterruptHandling().noOtherProcessRunningRoutine();
        }
    }

    private static void removeIORequest(int processId) {
        for (int i = 0; i < IO_REQUESTS.size(); i++) {
            if (IO_REQUESTS.get(i).getProcess().getId() == processId) {
                IO_REQUESTS.remove(i);
            }
        }
    }

    public static void addFinishedIOProcessId(int id) {
        FINISHED_IO_PROCESS_IDS.add(id);
    }

    public static int getFirstFinishedIOProcessId() {
        return FINISHED_IO_PROCESS_IDS.removeFirst();
    }

}

