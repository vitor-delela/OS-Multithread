package Hardware;

// -------------- Possiveis interrupcoes que esta CPU gera -------------- //
public enum Interrupts {
    noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow, intSTOP;
}