# sisop_t2
Trabalho Pratico 2 - Sistemas Operacionais

Nomes dos integrantes: Vitor Felipe Leitenski Delela e Diego Klein

Seção Implementação: 
  O programa implementa todas as características solicitadas.
  Uma detalhe é o fato de que não há uma identificação se a entrada que será digitada será computada para o scanner do Shell ou do Console, então é necessário olhar pelos últimos prints no terminal qual dessas threads solicitou o input do usuário primeiro. Por vezes os dois scanners irão solicitar entradas em seguida, então a primeira será computada para aquele que fez a solicitação primeiro.

Seção Testes
  Ao executar o programa, a thread do Shell irá iniciar sua execução. Os programas de 1 a 4 não possuem requisições IO, logo o resultado da execução será o dump da memória dos frames utilizados após a execução, com os respectivos resultados. 
  O programa 5 utiliza entrada IO, logo o resultado esperado é criar o novo processo e executá-lo até a instrução de TRAP. Após isso, deve rodar o Shell novamente para solicitar a criação de um novo processo. Ao mesmo tempo, deve rodar o Console para o pedido de IO para o processo. A primeira entrada é referente ao Shell e a segunda ao Console. Após isso, seguirá com o escalonamente dos dois processos.
  O programa 6 utiliza saída IO, logo o resultado esperado é criar o novo processo e executá-lo até a instrução de TRAP. Após isso, deve rodar o Shell novamente para solicitar a criação de um novo processo. Ao mesmo tempo, deve rodar o Console para a saída de IO para o processo.
