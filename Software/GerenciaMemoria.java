package Software;

import Hardware.Memory;
import Hardware.Opcode;
import Hardware.Word;

import java.util.ArrayList;

public class GerenciaMemoria {
    public Memory memory;
    public int tamPg;
    public int tamFrame;
    public int numeroFrames;
    public boolean[] availableFrames;

    public GerenciaMemoria(Memory memory) {
        this.memory = memory;
        this.tamFrame = this.tamPg = 16;
        this.numeroFrames = this.memory.tamMem / this.tamPg;

        availableFrames = initFrames(memory.tamMem, tamFrame);
    }

    // Inicializa o array de frames com valor TRUE
    private boolean[] initFrames(int tamMem, int pageSize) {
        availableFrames = new boolean[(tamMem / pageSize)];
        for (int i = 0; i < availableFrames.length; i++)
            availableFrames[i] = true;
        return availableFrames;
    }

    public boolean temEspacoParaAlocar(int numeroPalavras) {
        int quantidadeDeFramesQueVaiOcupar = 0;

        // Se for exatamente o tamanho da pagina, se não usa um a mais
        if (numeroPalavras % tamFrame == 0) quantidadeDeFramesQueVaiOcupar = ((numeroPalavras / tamFrame));
        else quantidadeDeFramesQueVaiOcupar = ((numeroPalavras / tamFrame) + 1);

        int quantidadeDeFramesDisponiveis = 0;
        for (int i = 0; i < availableFrames.length; i++)
            if (availableFrames[i]) quantidadeDeFramesDisponiveis++;

        return (quantidadeDeFramesQueVaiOcupar <= quantidadeDeFramesDisponiveis);
    }

    // Retornar o conjunto de frames alocados
    public ArrayList<Integer> aloca(Word[] p) {
        int quantidadeDeFramesQueVaiOcupar = 0;

        int tamanhoAlocar = p.length;

        for(Word w : p){
            if (w.opc.equals(Opcode.LDD) || w.opc.equals(Opcode.STD))
                if (w.p > tamanhoAlocar)
                    tamanhoAlocar = w.p;
        }

        //System.out.println("Alocadas " + tamanhoAlocar + " posicoes...");

        // Se for exatamente o tamanho da Pagina, se nao usa um a mais
        if (tamanhoAlocar % tamFrame == 0) quantidadeDeFramesQueVaiOcupar = ((tamanhoAlocar / tamFrame));
        else quantidadeDeFramesQueVaiOcupar = ((tamanhoAlocar / tamFrame) + 1);

        int quantidadeNovosFramesOcupados = 0;
        int posicao = 0;

        ArrayList<Integer> paginas = new ArrayList<>();

        for (int f = 0; f < availableFrames.length; f++) {
            if (availableFrames[f] == true) {
                availableFrames[f] = false;
                quantidadeNovosFramesOcupados++;
                paginas.add(f);

                for (int j = (f*tamFrame); j < (f+1)*tamFrame; j++) {
                    if (posicao < p.length) {
                        memory.m[j].opc = p[posicao].opc;
                        memory.m[j].r1 = p[posicao].r1;
                        memory.m[j].r2 = p[posicao].r2;
                        memory.m[j].p = p[posicao].p;
                        posicao++;
                    } else {
                        break;
                    }
                }
            }

            if (quantidadeNovosFramesOcupados == quantidadeDeFramesQueVaiOcupar){
                //memory.dump(0,memory.tamMem);
                return paginas; //Retorna um array de inteiros com os índices dos frames.
            }

        }
        return null;
    }

    // Dado um array de inteiros com as páginas de um processo, o gerente desloca as páginas.
    public void desaloca(ArrayList<Integer> paginasAlocadas) {
        for (Integer pagina : paginasAlocadas) {
            for (int i = 0; i < availableFrames.length; i++) {
                if (pagina == i) {
                    availableFrames[i] = true; // Libera o frame
                    for (int position = (i * tamFrame); position < (i + 1) * tamFrame; position++) {
                        memory.m[position] = new Word(Opcode.___, -1,-1,-1); //Esvazia memoria
                    }
                }
            }
        }
    }

    public int adicionaPage(){
        for (int i = 0; i < availableFrames.length; i++){
            if (availableFrames[i]){
                availableFrames[i] = false;
                return i;
            }
        }
        return -1;
    }
}
