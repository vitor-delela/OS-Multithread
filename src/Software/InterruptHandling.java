package Software;

import Hardware.CPU;
import Hardware.Interrupts;
import Hardware.Opcode;
import Sistema.Console;
import Sistema.Shell;

public class InterruptHandling {
    private GerenciaProcesso gerenteProcesso;
    private CPU cpu;

    public void configInterruptHandling(GerenciaProcesso gerenciaProcesso, CPU cpu) {
        this.gerenteProcesso = gerenciaProcesso;
        this.cpu = cpu;
    }

    public void handle(Interrupts irpt, int pc, int runningPid) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
        System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);
        switch (irpt){
            case intEscalonador:
                salvaProcesso();
                break;
            case intSTOP:
                System.out.println("Final de programa.\n");
                encerraProcesso();
                break;
            case intIO_FINISHED:
                System.out.println("Operação IO de processo concluída.");
                rotinaTerminoIO();
                break;
            case intTRAP:
                System.out.println("Chamada de sistema [TRAP].");
                chamadaConsole();
                break;
            case intEnderecoInvalido:
                System.out.println("Endereco invalido: programa acessando endereço fora de limites permitidos.");
                encerraProcesso();
                break;
            case intInstrucaoInvalida:
                System.out.println("Instrucao invalida: a instrucao carregada é invalida.");
                System.exit(0);
                encerraProcesso();
                break;
            case intOverflow:
                System.out.println("Overflow de numero inteiro.");
                encerraProcesso();
                break;
        }
    }

    private void salvaProcesso() {
        gerenteProcesso.EmExecucao = null;
        PCB process = cpu.unloadPCB();// Salva PCB
        gerenteProcesso.ListaProntos.add(process);// Coloca na fila de prontos
        cpu.irpt = (Interrupts.noInterrupt);

        // Libera escalonador
        if (Escalonador_Conc.SEMA_ESCALONADOR.availablePermits() == 0 && gerenteProcesso.EmExecucao == null) {
            Escalonador_Conc.SEMA_ESCALONADOR.release();
        }
    }

    private void encerraProcesso() {
        if (gerenteProcesso.EmExecucao != null) {
            // Finaliza processo (perde a referencia).
            gerenteProcesso.finish(gerenteProcesso.getProcessByID(cpu.runningPid));
            gerenteProcesso.EmExecucao = null;
        }

        cpu.irpt = (Interrupts.noInterrupt);

        // Libera escalonador.
        if (Escalonador_Conc.SEMA_ESCALONADOR.availablePermits() == 0) {
            Escalonador_Conc.SEMA_ESCALONADOR.release();
        } else {
            Shell.SEMA_SHELL.release();
        }
    }

    private void rotinaTerminoIO() {
        gerenteProcesso.EmExecucao = null;
        int finishedIOProcessId = Console.getFirstIOProcessId();
        PCB finishedIOProcess = gerenteProcesso.getBlockedProcessById(finishedIOProcessId);

        PCB interruptedProcess = cpu.unloadPCB();// Salva PCB.
        gerenteProcesso.ListaProntos.add(interruptedProcess);// Coloca o processo interrompido na fila de prontos.
        gerenteProcesso.ListaProntos.addFirst(finishedIOProcess);// Colocando processo que terminou IO na fila de prontos na 1a posição para ser executado logo em seguida.

        // Escreve o valor na memória ou printa ele na tela.
        int physicalAddress = gerenteProcesso.gerenciaMemoria.translate(finishedIOProcess.getReg()[8], finishedIOProcess.getAllocatedPages());
        if (finishedIOProcess.getReg()[7] == 1) {
            cpu.m[physicalAddress].opc = Opcode.DATA;
            cpu.m[physicalAddress].p = finishedIOProcess.getIOValue();
        } else {
            System.out.println("\n[Output from process with ID = " + finishedIOProcess.getId() + "]: " + finishedIOProcess.getIOValue() + "\n");
        }

        cpu.irpt = (Interrupts.noInterrupt);// Resetando interruptFlag da CPU.

        // Libera escalonador.
        if (Escalonador_Conc.SEMA_ESCALONADOR.availablePermits() == 0 && gerenteProcesso.EmExecucao == null) {
            Escalonador_Conc.SEMA_ESCALONADOR.release();
        }
    }

    private void chamadaConsole() {
        gerenteProcesso.EmExecucao = null;
        PCB process = cpu.unloadPCB();
        IORequest ioRequest;
        // Verifica se o pedido é de leitura ou de escrita.
        if (cpu.regs[7] == 1) {
            System.out.println("Requisicao de IO para leitura adicionada.");
            ioRequest = new IORequest(process, IORequest.OperationTypes.READ);
        } else {
            System.out.println("Requisicao de IO para escrita adicionada.");
            ioRequest = new IORequest(process, IORequest.OperationTypes.WRITE);
        }

        gerenteProcesso.ListaBloqueados.add(process);// Coloca na lista de bloqueados
        Console.requisicoesIO.add(ioRequest);// Cria uma requisição de IO na lista
        cpu.irpt = (Interrupts.noInterrupt);// Resetando interruptFlag da CPU
        Console.SEMA_CONSOLE.release();// Libera o console

        // Libera escalonador.
        if (Escalonador_Conc.SEMA_ESCALONADOR.availablePermits() == 0 && gerenteProcesso.EmExecucao == null) {
            Escalonador_Conc.SEMA_ESCALONADOR.release();
        }
    }

    public void rotinaSemProcessosListados() {
        int finishedIOProcessId = Console.getFirstIOProcessId();
        PCB finishedIOProcess = gerenteProcesso.getBlockedProcessById(finishedIOProcessId);
        int physicalAddress = gerenteProcesso.gerenciaMemoria.translate(finishedIOProcess.getReg()[8], finishedIOProcess.getAllocatedPages());
        if (finishedIOProcess.getReg()[7] == 1) {
            cpu.m[physicalAddress].opc = Opcode.DATA;
            cpu.m[physicalAddress].p = finishedIOProcess.getIOValue();
        } else {
            System.out.println("\n[Output from process with ID = " + finishedIOProcess.getId() + "]: " + finishedIOProcess.getIOValue() + "\n");
        }
        gerenteProcesso.ListaProntos.addFirst(finishedIOProcess);

        // Libera escalonador
        if (Escalonador_Conc.SEMA_ESCALONADOR.availablePermits() == 0 && gerenteProcesso.EmExecucao == null) {
            Escalonador_Conc.SEMA_ESCALONADOR.release();
        }
    }
}