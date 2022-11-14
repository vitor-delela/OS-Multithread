package Software;

import Hardware.Memory;
import Hardware.Opcode;
import Hardware.Word;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GerenciaProcesso {
    public GerenciaMemoria gerenciaMemoria;
    public Queue<PCB> listaPCBs;
    public int processId = 0;

    public GerenciaProcesso(Memory memory) {
        this.gerenciaMemoria = new GerenciaMemoria(memory);
        this.listaPCBs = new LinkedList<>();
    }

    public PCB create(Word[] p) {
        System.out.println("Novo processo criado");
        PCB processControlBlock;

        int tamanhoAlocar = p.length;

        for(Word w : p){
            if (w.opc.equals(Opcode.LDD) || w.opc.equals(Opcode.STD))
                if (w.p > tamanhoAlocar)
                    tamanhoAlocar = w.p; //se utilizar algum LDD ou STD com posicao de memoria maior que o numero de palavras declaradas, aloca mais paginas
        }

        if (gerenciaMemoria.temEspacoParaAlocar(tamanhoAlocar)) {
            //System.out.println("Alocando espaço para: " + p.length + " palavras...");
            ArrayList<Integer> paginas = gerenciaMemoria.aloca(p);
            processControlBlock = new PCB(processId, paginas, (paginas.get(0)*gerenciaMemoria.tamFrame));
            ++processId;

            listaPCBs.add(processControlBlock);
        } else {
            System.out.println("Sem espaço na memória para criar o processo de ID: " + processId);
            processControlBlock = null;
        }

        return processControlBlock;
    }

    public void finish(PCB processo) {
        System.out.println("Processo encerrado: " + processo.id);
        gerenciaMemoria.desaloca(processo.getAllocatedPages());
        listaPCBs.remove(processo);
    }

    public PCB getProcessByID(int id) {
        for (PCB pcb : listaPCBs){
            if (pcb.getId() == id){
                return pcb;
            }
        }
        return null;
    }

    public void listAllProcesses(){
        System.out.println("Processos: ");
        for(PCB pcb : listaPCBs){
            System.out.println(pcb.toString());
        }
    }

    public int getProcessLineFromMemory(int line, int processId){
        int pageId = getProcessByID(processId).getAllocatedPages().get(line/gerenciaMemoria.tamFrame);
        return pageId*gerenciaMemoria.tamFrame+(line% gerenciaMemoria.tamFrame);
    }

    public boolean hasProcess(int pid){
        for(PCB pcb : listaPCBs)
            if (pcb.id == pid) return true;
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

    public void adicionaExisitingPaginaEmProcesso(int pid, int page){
        if (getProcessByID(pid) != null){
            getProcessByID(pid).adicionaNovaPagina(page);
        }
    }
}
