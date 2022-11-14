package Hardware;

import Software.GerenciaProcesso;
import Software.InterruptHandling;
import Software.PCB;
import Software.SysCallHandling;

// ------------------------------------ V M  - constituida de CPU e MEMORIA ------------------------------------ //
// ---------------------------------------- atributos e construcao da VM --------------------------------------- //
public class VM {
  public int tamMem;
  public Word[] m;
  public Memory mem;
  public CPU cpu;
  public GerenciaProcesso gerenteProcesso;
  
  // vm deve ser configurada com endereço de tratamento de interrupcoes e de chamadas de sistema
  public VM(InterruptHandling ih, SysCallHandling sysCall){
    tamMem = 1024;
    mem = new Memory(tamMem);
    m = mem.m;
    gerenteProcesso = new GerenciaProcesso(mem);

    cpu = new CPU(mem,ih,sysCall, true, gerenteProcesso.gerenciaMemoria.tamFrame);  // debug true liga debug
  }

  public PCB criaProcesso(Word[] p){
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

}