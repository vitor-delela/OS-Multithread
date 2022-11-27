package Sistema;

import Software.GerenciaProcesso;
import java.util.Scanner;
import static Sistema.Sistema.progs;

public class Shell extends Thread {

    private Scanner scanner;
    private GerenciaProcesso gerenciaProcesso;

    public Shell(GerenciaProcesso gerenciaProcesso) {
        super("Shell");
        this.scanner = new Scanner(System.in);
        this.gerenciaProcesso = gerenciaProcesso;
    }

    @Override
    public void run() {
        System.out.println("\nEscolha o programa:");
        System.out.println("1 - Fibonacci");
        System.out.println("2 - FibonacciTRAP");
        System.out.println("3 - Fatorial");
        System.out.println("4 - FatorialTRAP");
        System.out.println("5 - ProgMinimo");
        System.out.println("6 - PB");
        System.out.println("7 - PC");
        System.out.println("9 - Exit");
        System.out.print("Programa: ");
        int aux2;
        System.out.println("\n[SHELL] - esperando input do usuário...\n");
        aux2 = scanner.nextInt();
        System.out.println("\n[SHELL] - recebeu o input do usuário [OK]\n");
        int id;

        switch (aux2) {
            case 1:
                id = gerenciaProcesso.create(progs.fibonacci10).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 2:
                id = gerenciaProcesso.create(progs.fibonacciTRAP).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 3:
                id = gerenciaProcesso.create(progs.fatorial).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 4:
                id = gerenciaProcesso.create(progs.fatorialTRAP).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 5:
                id = gerenciaProcesso.create(progs.progMinimo).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 6:
                id = gerenciaProcesso.create(progs.PB).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            case 7:
                id = gerenciaProcesso.create(progs.PC).getId();
                System.out.println((id<0?"Não foi possível criar o processo.": "Processo criado - Identificador do Processo: " + id));
                break;
            default:
                break;
        }
    }
}

