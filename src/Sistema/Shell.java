package Sistema;

import Hardware.VM;
import Programas.Programas;
import Software.GerenciaProcesso;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static Sistema.Sistema.progs;

public class Shell extends Thread {

    private Scanner scanner;
    private GerenciaProcesso gerenciaProcesso;
    public static Semaphore SEMA_SHELL = new Semaphore(1);

    public Shell(GerenciaProcesso gp) {
        super("Shell");
        this.scanner = new Scanner(System.in);
        this.gerenciaProcesso = gp;
        //this.vm = vm;
    }

    @Override
    public void run() {
        while (true){
            try {
                SEMA_SHELL.acquire();
                System.out.println("\n[SHELL] - Escolha o programa:");
                System.out.println("1 - Fibonacci");
                System.out.println("2 - Fatorial");
                System.out.println("3 - ProgMinimo");
                System.out.println("4 - PB");
                System.out.println("5 - Test In");
                System.out.println("6 - Test Out");
                System.out.println("0 - Exit");
                int aux2;
                System.out.println("\n[SHELL] - Esperando input do usuário: ");
                aux2 = scanner.nextInt();
                System.out.println("\n[SHELL] - recebeu o input do usuário [OK]\n");
                int id;

                switch (aux2) {
                    case 1:
                        id = (gerenciaProcesso.create(Programas.fibonacci10.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0 ? "Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 2:
                        id = (gerenciaProcesso.create(Programas.fatorial.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 3:
                        id = (gerenciaProcesso.create(Programas.progMinimo.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 4:
                        id = (gerenciaProcesso.create(Programas.PB.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 5:
                        id = (gerenciaProcesso.create(Programas.testIn.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 6:
                        id = (gerenciaProcesso.create(Programas.testOut.getProgramCode())).getId();
                        if (id < 0) System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                        break;
                    case 0:
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SEMA_SHELL = new Semaphore(0);
        }
    }
}

