package Software;

import Hardware.Interrupts;
import Hardware.Opcode;
import Hardware.Word;
import java.util.ArrayList;

public class PCB {
    public int id;
    public Interrupts interrupt;
    public ArrayList<Integer> allocatedPages;
    private Contexto contexto;
    // CPU context
    public int pc;
    public ProcessStatus status;
    public int[] reg;

    public PCB(int _id, ArrayList<Integer> _allocatedPages, int _pc) {
        this.allocatedPages = _allocatedPages;
        this.id = _id;
        this.interrupt = Interrupts.noInterrupt;
        this.pc = _pc;
        this.status = ProcessStatus.READY;
        this.reg = new int[10];
        this.contexto = new Contexto(0,1024,allocatedPages,reg, pc, new Word(Opcode.___,-1,-1,-1), id);
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

    public Contexto getContexto() {
        return contexto;
    }

    public void saveContexto(int pc, int[] reg, Word ir){
        contexto.setProgramCounter(pc);
        contexto.setRegs(reg);
        contexto.setInstrucionRegister(ir);
    }

    public String toString(){
        return "ID: " + id +
                "\tPages: " + allocatedPages +
                "\tProgram Counter: " + pc +
                //"\tStatus: " + status +
                "\tInterrupts: " + interrupt;
    }
}
