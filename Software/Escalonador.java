package Software;

import Hardware.CPU;

import java.util.LinkedList;
import java.util.List;

public class Escalonador {

    private LinkedList<PCB> prontos;
    private int posicao;
    private PCB runningProcess;
    private CPU cpu;

    public Escalonador(LinkedList<PCB> prontos, CPU cpu) {
        this.prontos = prontos;
        this.cpu = cpu;
        this.posicao = 0;
    }

    public void run() {
        System.out.println("Posição escalonador: " + posicao);
        System.out.println("tamanho da linked list de prontos = " + prontos.size());

        if (!prontos.isEmpty()) {
            PCB pcb = prontos.get(posicao);
            pcb.saveContexto(cpu.pc, cpu.regs, cpu.ir);

            posicao = (posicao + 1) % prontos.size();
            PCB pcbNovo = prontos.get(posicao);
            cpu.setContext(pcbNovo.getContexto());
            runningProcess = pcbNovo;
        }
        else {
            System.out.println("FROM: ESCALONADOR -- Lista de Prontos Vazia.");
        }

    }

    public PCB getRunningProcess() {
        return runningProcess;
    }

    public void setRunningProcessAsNull(){
        this.runningProcess = null;
    }

    public List<PCB> getProntos() {
        return prontos;
    }
}
