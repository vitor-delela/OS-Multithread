package Software;

import Hardware.CPU;
import Hardware.Interrupts;
import Hardware.Opcode;
import Sistema.Console;
import Sistema.Dispatcher;

public class InterruptHandling {
    private Escalonador escalonador;
    private GerenciaProcesso gerenteProcesso;
    private CPU cpu;

    public void configInterruptHandling(Escalonador escalonador, GerenciaProcesso gerenciaProcesso) {
        this.escalonador = escalonador;
        this.gerenteProcesso = gerenciaProcesso;
        this.cpu = escalonador.getCpu();
    }

    public void handle(Interrupts irpt, int pc, int runningPid) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);
        switch (irpt){
            case intEscalonador:
                escalonador.run();
                break;
            case intSTOP:
                gerenteProcesso.finish(gerenteProcesso.getProcessByID(runningPid));
                if (gerenteProcesso.getProntos().size() > 0){
                    if (escalonador.getPosicao()>0) escalonador.setPosicao( escalonador.getPosicao() - 1);
                    escalonador.getCpu().setContext(gerenteProcesso.getProntos().get(escalonador.getPosicao()).getContexto());
                }
                break;
            case intIO_FINISHED:
                System.out.println("Operação IO de processo concluída.");
                ioFinishedRoutine();
                break;
            case intTRAP:
                System.out.println("Chamada de sistema [TRAP].");
                packageForConsole();
                break;


        }
    }

    private void ioFinishedRoutine() {
        gerenteProcesso.RUNNING = null;
        int finishedIOProcessId = Console.getFirstFinishedIOProcessId();
        PCB finishedIOProcess = gerenteProcesso.getBlockedProcessById(finishedIOProcessId);
        // Salva PCB.
        PCB interruptedProcess = cpu.unloadPCB();

        // Coloca o processo interrompido na fila de prontos.
        gerenteProcesso.READY_LIST.add(interruptedProcess);
        // Resetando interruptFlag da CPU.
        cpu.irpt = (Interrupts.noInterrupt);
        // Colocando processo que terminou IO na fila de prontos na primeira
        // posição para ser executado logo em seguida.
        gerenteProcesso.READY_LIST.addFirst(finishedIOProcess);
        // Escreve o valor (ioRequestValue) na memória ou printa ele na tela.
        int physicalAddress = gerenteProcesso.gerenciaMemoria.translate(finishedIOProcess.getReg()[8], finishedIOProcess.getAllocatedPages());
        if (finishedIOProcess.getReg()[7] == 1) {
            cpu.m[physicalAddress].opc = Opcode.DATA;
            cpu.m[physicalAddress].p = finishedIOProcess.getIOValue();
        } else {
            System.out.println(
                    "\n[Output from process with ID = " + finishedIOProcess.getId() + "]: "
                            + finishedIOProcess.getIOValue() + "\n");
        }
        // Libera escalonador.
        if (Dispatcher.SEMA_DISPATCHER.availablePermits() == 0 && gerenteProcesso.RUNNING == null) {
            Dispatcher.SEMA_DISPATCHER.release();
        }
    }

    private void packageForConsole() {
        gerenteProcesso.RUNNING = null;
        PCB process = cpu.unloadPCB();
        IORequest ioRequest;
        // Verifica se o pedido é de leitura ou de escrita.
        if (cpu.regs[8] == 1) {
            ioRequest = new IORequest(process, IORequest.OperationTypes.READ);
        } else {
            ioRequest = new IORequest(process, IORequest.OperationTypes.WRITE);
        }
        // Coloca na lista de bloqueados.
        gerenteProcesso.BLOCKED_LIST.add(process);
        // Cria uma requisição de IO na lista.
        Console.IO_REQUESTS.add(ioRequest);
        // Resetando interruptFlag da CPU.
        cpu.irpt = (Interrupts.noInterrupt);
        // Libera o console.
        Console.SEMA_CONSOLE.release();
        // Libera escalonador.
        if (Dispatcher.SEMA_DISPATCHER.availablePermits() == 0 && gerenteProcesso.RUNNING == null) {
            Dispatcher.SEMA_DISPATCHER.release();
        }
    }

    public void noOtherProcessRunningRoutine() {
        int finishedIOProcessId = Console.getFirstFinishedIOProcessId();
        PCB finishedIOProcess = gerenteProcesso.getBlockedProcessById(finishedIOProcessId);
        int physicalAddress = gerenteProcesso.gerenciaMemoria.translate(finishedIOProcess.getReg()[8], finishedIOProcess.getAllocatedPages());
        if (finishedIOProcess.getReg()[7] == 1) {
            cpu.m[physicalAddress].opc = Opcode.DATA;
            cpu.m[physicalAddress].p = finishedIOProcess.getIOValue();
        } else {
            System.out.println("\n[Output from process with ID = " + finishedIOProcess.getId() + "]: " + finishedIOProcess.getIOValue() + "\n");
        }
        gerenteProcesso.READY_LIST.addFirst(finishedIOProcess);
        // Libera escalonador.
        if (Dispatcher.SEMA_DISPATCHER.availablePermits() == 0 && gerenteProcesso.RUNNING == null) {
            Dispatcher.SEMA_DISPATCHER.release();
        }
    }

    public Escalonador getEscalonador() {
        return escalonador;
    }
}