package Sistema;

import Hardware.VM;
import Hardware.Word;
import Programas.Programas;
import Software.Escalonador;
import Software.InterruptHandling;
import Software.PCB;
import Software.SysCallHandling;

public class Sistema{   // a VM com tratamento de interrupções
    public VM vm;
	public Escalonador escalonador;
    public InterruptHandling ih;
    public SysCallHandling sysCall;
    public static Programas progs;

    public Sistema() { // a VM com tratamento de interrupções
        ih = new InterruptHandling();
        sysCall = new SysCallHandling();
        vm = new VM(ih, sysCall);
		escalonador = new Escalonador(vm.gerenteProcesso.getProntos(), vm.cpu);

        sysCall.setVM(vm);
		ih.setEscalonador(escalonador);
    }

	public int carregaPrograma(Word[] programa){
		return vm.criaProcesso(programa).getId();
	}

	public void encerraProcesso (PCB processo){
		vm.encerraProcesso(processo);
	}

	public void encerraProcessoById (int processId){
		vm.encerraProcesso(vm.gerenteProcesso.getProcessByID(processId));
	}

	public void carregaAndExecutaPrograma(Word[] programa){
		int pid = carregaPrograma(programa);
		runByProcessId(pid);
	}

	public void runByProcessId (int pid){
		vm.cpu.setContext(vm.gerenteProcesso.getProcessByID(pid).getContexto());
		vm.cpu.run();
	}

	public void runWithEscalonador() {
		System.out.println("Iniciando execução dos processos prontos com escalonador");
		vm.cpu.escalonadorStatus(true);
		PCB running = null;
		if (vm.gerenteProcesso.getProntos() != null){
			running = vm.gerenteProcesso.getProntos().getFirst();
			vm.cpu.setContext(running.getContexto());
			vm.cpu.run();
			System.out.println("---------------------------------- Escalonador executado ");
		}
		else System.out.println("Sem processos prontos.");
	}

	public int getProgramCounterbyProcessId(int pid){
		return vm.gerenteProcesso.getProcessByID(pid).pc;
	}

	public void listarProcessos(){
		vm.listaProcessos();
	}

	public void changeDebug(boolean newValue){
		vm.cpu.setDebug(newValue);
	}

	public void dumpMemoria(int posicaoInicial, int posicaoFinal){
		vm.mem.dump(posicaoInicial, posicaoFinal);
	}

	public void dumpPCB(int processId){
		System.out.println(vm.gerenteProcesso.getProcessByID(processId).toString());
	}

	public boolean existeProcesso(int pid){
		return vm.existeProcesso(pid);
	}
}
