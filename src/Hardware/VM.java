package Hardware;

import Sistema.Console;
import Software.Escalonador_Conc;
import Sistema.Shell;
import Software.GerenciaProcesso;
import Software.InterruptHandling;
import Software.PCB;
import Software.SysCallHandling;

// ------------------------------------ V M  - constituida de CPU e MEMORIA ------------------------------------ //
// ---------------------------------------- atributos e construcao da VM --------------------------------------- //
public class VM {
  public final int tamMem = 1024;
  public Word[] m;
  public Memory mem;
  public CPU cpu;
  public GerenciaProcesso gerenteProcesso;

  private Shell shell;
  private Escalonador_Conc dispatcher;
  private Console console;
  
  public VM(InterruptHandling ih){
    mem = new Memory(tamMem);
    m = mem.m;
    gerenteProcesso = new GerenciaProcesso(mem);

    cpu = new CPU(mem,ih, true, gerenteProcesso.gerenciaMemoria.tamFrame, new int[10]);  // debug true liga debug

    shell = new Shell(gerenteProcesso);
    dispatcher = new Escalonador_Conc(cpu, gerenteProcesso);
    console = new Console(cpu, gerenteProcesso);
  }

  public PCB criaProcesso(Word[] p){
    System.out.println("entrou");
    return gerenteProcesso.create(p);
  }

  public void encerraProcesso(PCB pcb){
    gerenteProcesso.finish(pcb);
  }

  public void listaProcessos(){
    gerenteProcesso.listAllProcesses();
  }

  public boolean existeProcesso(int pid){
    return gerenteProcesso.hasProcess(pid);
  }

//  public void configEscalonador(){
//    gerenteProcesso.setEscalonador(cpu.getInterruptHandling().getEscalonador());
//  }

  public void startThreads() {
    shell.start();
    cpu.start();
    dispatcher.start();
    console.start();
  }

}