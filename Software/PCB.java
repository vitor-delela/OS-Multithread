package Software;

import Hardware.Interrupts;

import java.util.ArrayList;

public class PCB {
    public int id;
    public Interrupts interrupt;
    public ArrayList<Integer> allocatedPages;

    // CPU context
    public int pc;
    public ProcessStatus status;
    public int[] reg;

    public PCB(int id, ArrayList<Integer> allocatedPages, int pc) {
        this.allocatedPages = allocatedPages;
        this.id = id;
        this.interrupt = Interrupts.noInterrupt;
        this.pc = pc;
        this.status = ProcessStatus.READY;
        this.reg = new int[10];
    }

    //retorna a lista de paginas de um processo
    public ArrayList<Integer> getAllocatedPages() {
        return this.allocatedPages;
    }

    public int getId() {
        return this.id;
    }

    public void adicionaNovaPagina(int pagina){
        allocatedPages.add(pagina);
    }

    public String toString(){
        return "ID: " + id +
                "\tPages: " + allocatedPages +
                "\tProgram Counter: " + pc +
                "\tStatus: " + status +
                "\tInterrupts: " + interrupt;
    }
}
