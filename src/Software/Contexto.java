package Software;

import Hardware.Word;

import java.util.ArrayList;

public class Contexto {
    private int base;
    private int limite;
    private ArrayList<Integer> allocatedPages;
    private int[] regs;
    private int programCounter;
    private Word instrucionRegister;
    private int processId;

    public Contexto(int base, int limite, ArrayList<Integer> allocatedPages, int[] regs, int programCounter, Word instrucionRegister, int pid) {
        this.base = base;
        this.limite = limite;
        this.allocatedPages = allocatedPages;
        this.regs = regs;
        this.programCounter = programCounter;
        this.instrucionRegister = instrucionRegister;
        this.processId = pid;
    }

    public int getBase() {
        return base;
    }

    public int getLimite() {
        return limite;
    }

    public ArrayList<Integer> getAllocatedPages() {
        return allocatedPages;
    }

    public int[] getRegs() {
        return regs;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public Word getInstrucionRegister() {
        return instrucionRegister;
    }

    public void addAllocatedPage(int pagina) {
        allocatedPages.add(pagina);
    }

    public void setRegs(int[] regs) {
        this.regs = regs;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public void setInstrucionRegister(Word instrucionRegister) {
        this.instrucionRegister = instrucionRegister;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

}

