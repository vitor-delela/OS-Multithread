package Hardware;

import Sistema.Console;
import Software.Contexto;
import Software.InterruptHandling;
import Software.PCB;
import Software.SysCallHandling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class CPU extends Thread{
    public Semaphore SEMA_CPU = new Semaphore(0);

    /* valores maximo e minimo para inteiros nesta cpu */
    public int maxInt;
    public int minInt;
    
    // característica do processador: contexto da CPU ...
    public int pc; // ... composto de program counter,
    public Word ir; // instruction register,
    public int[] regs; // registradores da CPU
    public Interrupts irpt; // durante instrucao, interrupcao pode ser sinalizada
    public ArrayList<Integer> pages;

    public int tamFrame;
    public int runningPid;

    public int base;
    public int limite;

    public Memory mem; // mem tem funcoes de dump e o array m de memória 'fisica'
    public Word[] m; // CPU acessa MEMORIA, guarda referencia a 'm'. m nao muda. sempre será um array de palavras

    private InterruptHandling interruptHandling; // significa desvio para rotinas de tratamento de Int - se int ligada, desvia
    //private SysCallHandling sysCall; // significa desvio para tratamento de chamadas de sistema - trap
    private boolean debug; // se true entao mostra cada instrucao em execucao

    // usado pelo escalonador
    private int delta = 0;
    private final int deltaMax = 5;
    public boolean escalonadorStatus;

    // ref a MEMORIA e interrupt handler passada na criacao da CPU
    public CPU(Memory _mem, InterruptHandling _interruptHandling, boolean _debug, int _tamFrame, int[] _regs) {
        super("CPU");
        maxInt = 32767; // capacidade de representacao modelada
        minInt = -32767; // se exceder deve gerar interrupcao de overflow
        mem = _mem; // usa mem para acessar funcoes auxiliares (dump)
        m = mem.m; // usa o atributo 'm' para acessar a memoria.
        //regs = new int[10];
        interruptHandling = _interruptHandling; // aponta para rotinas de tratamento de int
       // sysCall = _sysCall; // aponta para rotinas de tratamento de chamadas de sistema
        debug = _debug; // se true, print da instrucao em execucao
        tamFrame = _tamFrame;

        this.regs = _regs;// aloca o espaço dos registradores - regs 8 e 9 usados somente para IO
        escalonadorStatus = true;
    }

    // _todo acesso a memoria tem que ser verificado
    private boolean legal(int e) {
        return (e >= 0 && e <= mem.tamMem);
    }

    // toda operacao matematica deve avaliar se ocorre overflow
    private boolean testOverflow(int v) {
        if ((v < minInt) || (v > maxInt)) {
            irpt = Interrupts.intOverflow;
            return false;
        }
        return true;
    }

    public void escalonadorStatus(boolean flag){
        this.escalonadorStatus = flag;
    }

    public void setContext(Contexto _contexto) {
        this.regs = _contexto.getRegs();
        this.pages = _contexto.getAllocatedPages();
        this.base = _contexto.getBase();
        this.limite = _contexto.getLimite();
        this.pc = _contexto.getProgramCounter();
        this.ir = _contexto.getInstrucionRegister();
        this.runningPid = _contexto.getProcessId();
        irpt = Interrupts.noInterrupt;
    }

    public int convertePosicaoMemoria(int posicaoPrograma){
        int pageId = pages.get(posicaoPrograma/tamFrame);
        return pageId * tamFrame + (posicaoPrograma % tamFrame);
    }

    public void run() {
        while (true){
            try {
                // Espera semaforo.
                SEMA_CPU.acquire();

                while (true) {
                    if (legal(pc)) { // pc valido
                        ir = m[pc]; // busca posicao da memoria apontada por pc, guarda em ir
                        if (debug) {
                            System.out.print("                               pc: " + pc + "       exec: ");
                            mem.dump(ir);
                        }

                        delta++;

                        // EXECUTA INSTRUCAO NO ir
                        switch (ir.opc) {
                            // conforme o opcode (código de operação) executa
                            // Instrucoes de Busca e Armazenamento em Memoria
                            case LDI: // Rd ← k
                                regs[ir.r1] = ir.p;
                                pc++;
                                break;

                            case LDD: // Rd <- [A]
                                if (legal(ir.p)) {
                                    regs[ir.r1] = m[convertePosicaoMemoria(ir.p)].p;
                                    //reg[ir.r1] = m[ir.p].p;
                                    pc++;
                                }
                                break;

                            case LDX: // RD <- [RS] // NOVA
                                if (legal(regs[ir.r2])) {
                                    regs[ir.r1] = m[regs[ir.r2]].p;
                                    pc++;
                                }
                                break;

                            case STD: // [A] ← Rs
                                if (legal(ir.p)) {
                                    m[convertePosicaoMemoria(ir.p)].opc = Opcode.DATA;
                                    m[convertePosicaoMemoria(ir.p)].p = regs[ir.r1];
                                    //m[ir.p].opc = Opcode.DATA;
                                    //m[ir.p].p = reg[ir.r1];
                                    pc++;
                                }
                                ;
                                break;

                            case STX: // [Rd] ←Rs
                                if (legal(regs[ir.r1])) {
                                    m[convertePosicaoMemoria(regs[ir.r1])].opc = Opcode.DATA;
                                    m[convertePosicaoMemoria(regs[ir.r1])].p = regs[ir.r2];
                                    //m[reg[ir.r1]].opc = Opcode.DATA;
                                    //m[reg[ir.r1]].p = reg[ir.r2];
                                    pc++;
                                }
                                ;
                                break;

                            case MOVE: // RD <- RS
                                regs[ir.r1] = regs[ir.r2];
                                pc++;
                                break;

                            // Instrucoes Aritmeticas
                            case ADD: // Rd ← Rd + Rs
                                regs[ir.r1] = regs[ir.r1] + regs[ir.r2];
                                testOverflow(regs[ir.r1]);
                                pc++;
                                break;

                            case ADDI: // Rd ← Rd + k
                                regs[ir.r1] = regs[ir.r1] + ir.p;
                                testOverflow(regs[ir.r1]);
                                pc++;
                                break;

                            case SUB: // Rd ← Rd - Rs
                                regs[ir.r1] = regs[ir.r1] - regs[ir.r2];
                                testOverflow(regs[ir.r1]);
                                pc++;
                                break;

                            case SUBI: // RD <- RD - k // NOVA
                                regs[ir.r1] = regs[ir.r1] - ir.p;
                                testOverflow(regs[ir.r1]);
                                pc++;
                                break;

                            case MULT: // Rd <- Rd * Rs
                                regs[ir.r1] = regs[ir.r1] * regs[ir.r2];
                                testOverflow(regs[ir.r1]);
                                pc++;
                                break;

                            // Instrucoes JUMP
                            case JMP: // PC <- k
                                pc = convertePosicaoMemoria(ir.p);
                                //pc = ir.p;
                                break;

                            case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                                if (regs[ir.r2] > 0) {
                                    pc = convertePosicaoMemoria(regs[ir.r1]);
                                    //pc = reg[ir.r1];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIGK: // If RC > 0 then PC <- k else PC++
                                if (regs[ir.r2] > 0) {
                                    pc = convertePosicaoMemoria(ir.p);
                                    //pc = ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPILK: // If RC < 0 then PC <- k else PC++
                                if (regs[ir.r2] < 0) {
                                    pc = convertePosicaoMemoria(ir.p);
                                    //pc = ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIEK: // If RC = 0 then PC <- k else PC++
                                if (regs[ir.r2] == 0) {
                                    pc = convertePosicaoMemoria(ir.p);
                                    //pc = ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                                if (regs[ir.r2] < 0) {
                                    pc = convertePosicaoMemoria(regs[ir.r1]);
                                    //pc = reg[ir.r1];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                                if (regs[ir.r2] == 0) {
                                    pc = convertePosicaoMemoria(regs[ir.r1]);
                                    //pc = reg[ir.r1];
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIM: // PC <- [A]
                                pc = m[convertePosicaoMemoria(ir.p)].p;
                                //pc = m[ir.p].p;
                                break;

                            case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                                if (regs[ir.r2] > 0) {
                                    //pc = m[convertePosicaoMemoria(ir.p)].p;
                                    pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                                    //pc = m[ir.p].p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPILM: // If RC < 0 then PC <- k else PC++
                                if (regs[ir.r2] < 0) {
                                    //pc = m[convertePosicaoMemoria(ir.p)].p;
                                    pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                                    //pc = m[ir.p].p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIEM: // If RC = 0 then PC <- k else PC++
                                if (regs[ir.r2] == 0) {
                                    //pc = m[convertePosicaoMemoria(ir.p)].p;
                                    pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                                    //pc = m[ir.p].p;
                                } else {
                                    pc++;
                                }
                                break;

                            case JMPIGT: // If RS>RC then PC <- k else PC++
                                if (regs[ir.r1] > regs[ir.r2]) {
                                    pc = convertePosicaoMemoria(ir.p);
                                    //pc = ir.p;
                                } else {
                                    pc++;
                                }
                                break;

                            // outras
                            case STOP: // por enquanto, para execucao
                                irpt = Interrupts.intSTOP;
                                break;

                            case DATA:
                                irpt = Interrupts.intInstrucaoInvalida;
                                break;

                            // Chamada de sistema
                            case TRAP:
                                irpt = Interrupts.intTRAP;
                                //sysCall.trapHandling(convertePosicaoMemoria(regs[9]), runningPid);
                                pc++;
                                break;

                            // Inexistente
                            default:
                                irpt = Interrupts.intInstrucaoInvalida;
                                break;
                        }
                    }
                    else{
                        irpt = Interrupts.intEnderecoInvalido;
                    }

                    // Verifica Escalonador
                    if ((delta >= deltaMax) && (irpt == Interrupts.noInterrupt)){
                        delta = 0;
                        irpt = Interrupts.intEscalonador;
                    }

                    // Segue o loop se nao houve interrupcao.
                    if (irpt == Interrupts.noInterrupt) {
                        // Checa se algum IO terminou.
                        if (Console.FINISHED_IO_PROCESS_IDS.size() > 0) {
                            irpt = Interrupts.intIO_FINISHED;
                            break;
                        }
                        continue;
                    }

                    // Houve interrupcao, deve ser tratada fora do loop.
                    break;

                } // FIM DO CICLO DE UMA INSTRUÇÃO

                // Trata interrupção.
                interruptHandling.handle(irpt, pc, runningPid); // desvia para rotina de tratamento
                delta = 0;

            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }

    }

    public void setDebug(boolean value){
        debug = value;
    }

    public InterruptHandling getInterruptHandling() {
        return interruptHandling;
    }

    public PCB unloadPCB() {
        return new PCB(runningPid, new ArrayList<Integer>(pages), pc, regs.clone());
    }
}