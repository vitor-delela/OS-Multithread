package Software;

import Hardware.Interrupts;
import Hardware.Opcode;
import Hardware.Word;
import java.util.ArrayList;

public class PCB {
    private Contexto contexto;
    public Interrupts interrupt;
    public ProcessStatus status;
    private int ioValue;
    //public int id;
    //public ArrayList<Integer> allocatedPages;
    //public int pc;
    //public int[] reg;

    public PCB(int _id, ArrayList<Integer> _allocatedPages, int _pc) {
        this.interrupt = Interrupts.noInterrupt;
        this.status = ProcessStatus.READY;
        this.contexto = new Contexto(0,1024,_allocatedPages, new int[10], _pc, new Word(Opcode.___,-1,-1,-1), _id);
        this.ioValue = -1;
        // this.allocatedPages = _allocatedPages;
        // this.id = _id;
        // this.pc = _pc;
        // this.reg = new int[10];
    }

    //retorna a lista de paginas de um processo
    public ArrayList<Integer> getAllocatedPages() {
        return this.contexto.getAllocatedPages();
    }

    public int getId() {
        return this.contexto.getProcessId();
    }

    public void adicionaNovaPagina(int pagina){
        contexto.addAllocatedPage(pagina);
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
        return "ID: " + contexto.getProcessId() +
                "\tPages: " + contexto.getAllocatedPages() +
                "\tProgram Counter: " + contexto.getProgramCounter() +
                //"\tStatus: " + status +
                "\tInterrupts: " + interrupt;
    }

    public int getIOValue() {
        return ioValue;
    }

    public void setIOValue(int ioValue) {
        this.ioValue = ioValue;
    }

    public int[] getReg() {
        return contexto.getRegs();
    }

    public int getPc() {
        return contexto.getProgramCounter();
    }
}
