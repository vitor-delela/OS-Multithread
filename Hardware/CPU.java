package Hardware;

import Software.InterruptHandling;
import Software.SysCallHandling;

import java.util.ArrayList;

public class CPU {
    /* valores maximo e minimo para inteiros nesta cpu */
    public int maxInt;
    public int minInt;
    
    // característica do processador: contexto da CPU ...
    public int pc; // ... composto de program counter,
    public Word ir; // instruction register,
    public int[] reg; // registradores da CPU
    public Interrupts irpt; // durante instrucao, interrupcao pode ser sinalizada
    ArrayList<Integer> pages;
    public int tamFrame;
    public int runningPid;
    /*
     * base e limite de acesso na memoria
     * por enquanto toda memoria pode ser acessada pelo processo rodando
     * ATE AQUI: contexto da CPU - tudo que precisa sobre o estado de um processo para executa-lo
     * nas proximas versoes isto pode modificar
     */
    public int base;
    public int limite;

    public Memory mem; // mem tem funcoes de dump e o array m de memória 'fisica'
    public Word[] m; // CPU acessa MEMORIA, guarda referencia a 'm'. m nao muda. sempre será um array de palavras

    private InterruptHandling ih; // significa desvio para rotinas de tratamento de Int - se int ligada, desvia
    private SysCallHandling sysCall; // significa desvio para tratamento de chamadas de sistema - trap
    private boolean debug; // se true entao mostra cada instrucao em execucao

    // ref a MEMORIA e interrupt handler passada na criacao da CPU
    public CPU(Memory _mem, InterruptHandling _ih, SysCallHandling _sysCall, boolean _debug, int _tamFrame) {
        maxInt = 32767; // capacidade de representacao modelada
        minInt = -32767; // se exceder deve gerar interrupcao de overflow
        mem = _mem; // usa mem para acessar funcoes auxiliares (dump)
        m = mem.m; // usa o atributo 'm' para acessar a memoria.
        reg = new int[10]; // aloca o espaço dos registradores - regs 8 e 9 usados somente para IO
        ih = _ih; // aponta para rotinas de tratamento de int
        sysCall = _sysCall; // aponta para rotinas de tratamento de chamadas de sistema
        debug = _debug; // se true, print da instrucao em execucao
        tamFrame = _tamFrame;
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

    /*
     * no futuro esta funcao vai ter que ser expandida para setar todo contexto de execucao,
     *  agora, setamos somente os registradores base,limite e pc (deve ser zero nesta versao)
     *  reset da interrupcao registrada
     */
    public void setContext(int _base, int _limite, int _pc) {
        base = _base;
        limite = _limite;
        pc = _pc;
        irpt = Interrupts.noInterrupt;
    }

    public void setContext(int _base, int _limite, int _pc, ArrayList<Integer> _pages, int _runningPid) {
        base = _base;
        limite = _limite;
        pc = _pc;
        irpt = Interrupts.noInterrupt;
        pages = _pages;
        runningPid = _runningPid;
    }

    public int convertePosicaoMemoria(int posicaoPrograma){
        int pageId = pages.get(posicaoPrograma/tamFrame);
        return pageId * tamFrame + (posicaoPrograma % tamFrame);
    }

    /*
     * execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
     */
    public void run() {
        while (true) { // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
            // FETCH
            if (legal(pc)) { // pc valido
                ir = m[pc]; // <<<<<<<<<<<< busca posicao da memoria apontada por pc, guarda em ir
                if (debug) {
                    //for (int r=0; r<reg.length;r++) System.out.print("reg["+r+"] = "+reg[r]+"\t"); //debuga registradores
                    System.out.print("                               pc: " + pc + "       exec: ");
                    mem.dump(ir);
                }
                // --------------------------------------------------------------------------------------------------
                // EXECUTA INSTRUCAO NO ir
                switch (ir.opc) {
                    // conforme o opcode (código de operação) executa
                    // Instrucoes de Busca e Armazenamento em Memoria
                    case LDI: // Rd ← k
                        reg[ir.r1] = ir.p;
                        pc++;
                        break;

                    case LDD: // Rd <- [A]
                        if (legal(ir.p)) {
                            reg[ir.r1] = m[convertePosicaoMemoria(ir.p)].p;
                            //reg[ir.r1] = m[ir.p].p;
                            pc++;
                        }
                        break;

                    case LDX: // RD <- [RS] // NOVA
                        if (legal(reg[ir.r2])) {
                            reg[ir.r1] = m[reg[ir.r2]].p;
                            pc++;
                        }
                        break;

                    case STD: // [A] ← Rs
                        if (legal(ir.p)) {
                            m[convertePosicaoMemoria(ir.p)].opc = Opcode.DATA;
                            m[convertePosicaoMemoria(ir.p)].p = reg[ir.r1];
                            //m[ir.p].opc = Opcode.DATA;
                            //m[ir.p].p = reg[ir.r1];
                            pc++;
                        }
                        ;
                        break;

                    case STX: // [Rd] ←Rs
                        if (legal(reg[ir.r1])) {
                            m[convertePosicaoMemoria(reg[ir.r1])].opc = Opcode.DATA;
                            m[convertePosicaoMemoria(reg[ir.r1])].p = reg[ir.r2];
                            //m[reg[ir.r1]].opc = Opcode.DATA;
                            //m[reg[ir.r1]].p = reg[ir.r2];
                            pc++;
                        }
                        ;
                        break;

                    case MOVE: // RD <- RS
                        reg[ir.r1] = reg[ir.r2];
                        pc++;
                        break;

                    // Instrucoes Aritmeticas
                    case ADD: // Rd ← Rd + Rs
                        reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
                        testOverflow(reg[ir.r1]);
                        pc++;
                        break;

                    case ADDI: // Rd ← Rd + k
                        reg[ir.r1] = reg[ir.r1] + ir.p;
                        testOverflow(reg[ir.r1]);
                        pc++;
                        break;

                    case SUB: // Rd ← Rd - Rs
                        reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
                        testOverflow(reg[ir.r1]);
                        pc++;
                        break;

                    case SUBI: // RD <- RD - k // NOVA
                        reg[ir.r1] = reg[ir.r1] - ir.p;
                        testOverflow(reg[ir.r1]);
                        pc++;
                        break;

                    case MULT: // Rd <- Rd * Rs
                        reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
                        testOverflow(reg[ir.r1]);
                        pc++;
                        break;

                    // Instrucoes JUMP
                    case JMP: // PC <- k
                        pc = convertePosicaoMemoria(ir.p);
                        //pc = ir.p;
                        break;

                    case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                        if (reg[ir.r2] > 0) {
                            pc = convertePosicaoMemoria(reg[ir.r1]);
                            //pc = reg[ir.r1];
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIGK: // If RC > 0 then PC <- k else PC++
                        if (reg[ir.r2] > 0) {
                            pc = convertePosicaoMemoria(ir.p);
                            //pc = ir.p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPILK: // If RC < 0 then PC <- k else PC++
                        if (reg[ir.r2] < 0) {
                            pc = convertePosicaoMemoria(ir.p);
                            //pc = ir.p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIEK: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.r2] == 0) {
                            pc = convertePosicaoMemoria(ir.p);
                            //pc = ir.p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                        if (reg[ir.r2] < 0) {
                            pc = convertePosicaoMemoria(reg[ir.r1]);
                            //pc = reg[ir.r1];
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                        if (reg[ir.r2] == 0) {
                            pc = convertePosicaoMemoria(reg[ir.r1]);
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
                        if (reg[ir.r2] > 0) {
                            //pc = m[convertePosicaoMemoria(ir.p)].p;
                            pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                            //pc = m[ir.p].p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPILM: // If RC < 0 then PC <- k else PC++
                        if (reg[ir.r2] < 0) {
                            //pc = m[convertePosicaoMemoria(ir.p)].p;
                            pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                            //pc = m[ir.p].p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIEM: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.r2] == 0) {
                            //pc = m[convertePosicaoMemoria(ir.p)].p;
                            pc = convertePosicaoMemoria(m[convertePosicaoMemoria(ir.p)].p);
                            //pc = m[ir.p].p;
                        } else {
                            pc++;
                        }
                        break;

                    case JMPIGT: // If RS>RC then PC <- k else PC++
                        if (reg[ir.r1] > reg[ir.r2]) {
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
                        sysCall.trapHandling(convertePosicaoMemoria(reg[9]), runningPid);
                        pc++;
                        break;

                    case SHMALLOC:
                        sysCall.trapHandling(convertePosicaoMemoria(reg[9]), runningPid);
                        pc++;
                        break;

                    case SHMREF:
                        sysCall.trapHandling(convertePosicaoMemoria(reg[9]), runningPid);
                        pc++;
                        break;

                    // Inexistente
                    default:
                        irpt = Interrupts.intInstrucaoInvalida;
                        break;
                }
            }
            // --------------------------------------------------------------------------------------------------
            // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
            if (!(irpt == Interrupts.noInterrupt)) { // existe interrupção
                ih.handle(irpt, pc); // desvia para rotina de tratamento
                break; // break sai do loop da cpu
            }
        } // FIM DO CICLO DE UMA INSTRUÇÃO
    }

    public void setDebug(boolean value){
        debug = value;
    }

}