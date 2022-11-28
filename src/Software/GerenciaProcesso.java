package Software;

import Hardware.Memory;
import Hardware.Opcode;
import Hardware.Word;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GerenciaProcesso {
    public GerenciaMemoria gerenciaMemoria;
    private Queue<PCB> listaTodosPCBs;
    public static LinkedList<PCB> ListaProntos = new LinkedList<>();
    public static LinkedList<PCB> ListaBloqueados = new LinkedList<>();
    public static PCB EmExecucao = null;

    public int processId = 0;

    public GerenciaProcesso(Memory memory) {
        this.gerenciaMemoria = new GerenciaMemoria(memory);
        this.listaTodosPCBs = new LinkedList<>();
    }

    public PCB create(Word[] p) {
        PCB processControlBlock;

        int tamanhoAlocar = p.length;

        for(Word w : p){
            if (w.opc.equals(Opcode.LDD) || w.opc.equals(Opcode.STD))
                if (w.p > tamanhoAlocar)
                    tamanhoAlocar = w.p; //se utilizar algum LDD ou STD com posicao de memoria maior que o numero de palavras declaradas, aloca mais paginas
        }

        if (gerenciaMemoria.temEspacoParaAlocar(tamanhoAlocar)) {
            ArrayList<Integer> paginas = gerenciaMemoria.aloca(p);
            processControlBlock = new PCB(processId, paginas, (paginas.get(0)*gerenciaMemoria.tamFrame));
            ++processId;

            System.out.println("Novo processo criado: " + processControlBlock.getId());

            listaTodosPCBs.add(processControlBlock);
            ListaProntos.add(processControlBlock);

            // Libera escalonador se nao tem processo rodando
            if (ListaProntos.size() == 1 && EmExecucao == null) {
                Escalonador_Conc.SEMA_ESCALONADOR.release();
            }
        } else {
            System.out.println("Sem espaço na memória para criar o processo de ID: " + processId);
            processControlBlock = null;
        }

        return processControlBlock;
    }

    public void finish(PCB processo) {
        System.out.println("Processo encerrado: " + processo.getId());
        for(int page : processo.getAllocatedPages()){
            gerenciaMemoria.memory.dump(page*16, (page+1)*16);
        }
        gerenciaMemoria.desaloca(processo.getAllocatedPages());
        listaTodosPCBs.remove(processo);
        //escalonador.setProntos(getProntos());

        for (int i = 0; i < ListaProntos.size(); i++) {
            if (ListaProntos.get(i).getId() == processo.getId()) {
                ListaProntos.remove(i);
            }
        }
        for (int i = 0; i < ListaBloqueados.size(); i++) {
            if (ListaBloqueados.get(i).getId() == processo.getId()) {
                ListaBloqueados.remove(i);
            }
        }
    }

    public PCB getProcessByID(int id) {
        for (PCB pcb : listaTodosPCBs){
            if (pcb.getId() == id){
                return pcb;
            }
        }
        return null;
    }

    public static PCB getBlockedProcessById(int id) {
        for (int i = 0; i < ListaBloqueados.size(); i++) {
            if (ListaBloqueados.get(i).getId() == id) {
                return ListaBloqueados.remove(i);
            }
        }
        return null;
    }

    public void listAllProcesses(){
        System.out.println("Processos: ");
        for(PCB pcb : listaTodosPCBs){
            System.out.println(pcb.toString());
        }
    }

    public LinkedList<PCB> getProntos(){
        return ListaProntos;
    }

    public LinkedList<PCB> getBloqueados(){
        return ListaBloqueados;
    }

    public int getProcessLineFromMemory(int line, int processId){
        int pageId = getProcessByID(processId).getAllocatedPages().get(line/gerenciaMemoria.tamFrame);
        return pageId*gerenciaMemoria.tamFrame+(line% gerenciaMemoria.tamFrame);
    }

    public boolean hasProcess(int pid){
        for(PCB pcb : listaTodosPCBs)
            if (pcb.getId() == pid) return true;
        return false;
    }

    public int adicionaPaginaEmProcesso(int pid){
        if (getProcessByID(pid) != null){
            int newPageId = gerenciaMemoria.adicionaPage();
            if (newPageId >= 0){
                getProcessByID(pid).adicionaNovaPagina(newPageId);
                return newPageId;
            }
        }
        return -1;
    }

    public void adicionaExistingPaginaEmProcesso(int pid, int page){
        if (getProcessByID(pid) != null){
            getProcessByID(pid).adicionaNovaPagina(page);
        }
    }

}
