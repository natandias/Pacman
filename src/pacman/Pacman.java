package pacman;

import java.awt.*;
import java.applet.Applet;

public class Pacman extends Applet implements Runnable {

    Dimension d;
    Font largefont = new Font("Helvetica", Font.BOLD, 24);
    Font smallfont = new Font("Helvetica", Font.BOLD, 14);

    FontMetrics fmsmall, fmlarge;
    Graphics goff;
    Image ii;
    Thread thethread;
    MediaTracker thetracker = null;
    Color dotcolor = new Color(192, 192, 0);
    int bigdotcolor = 192;
    int dbigdotcolor = -2;
    Color mazecolor;

    boolean ingame = false;
    boolean showtitle = true;
    boolean scared = false;
    boolean dying = false;

    final int screendelay = 120;
    final int blocksize = 24;
    final int nrofblocks = 15;
    final int scrsize = nrofblocks * blocksize;
    final int animdelay = 8;
    final int pacanimdelay = 2;
    final int ghostanimcount = 2;
    final int pacmananimcount = 4;
    final int maxghosts = 12;
    final int pacmanspeed = 6;

    int animcount = animdelay;
    int pacanimcount = pacanimdelay;
    int pacanimdir = 1;
    int count = screendelay;
    int ghostanimpos = 0;
    int pacmananimpos = 0;
    int nrofghosts = 6;
    int pacsleft, score;
    int deathcounter;
    int[] dx, dy;
    int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;

    Image ghost1, ghost2, ghostscared1, ghostscared2;
    Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    Image pacman3up, pacman3down, pacman3left, pacman3right;
    Image pacman4up, pacman4down, pacman4left, pacman4right;

    int pacmanx, pacmany, pacmandx, pacmandy;
    int reqdx, reqdy, viewdx, viewdy;
    int scaredcount, scaredtime;
    final int maxscaredtime = 120;
    final int minscaredtime = 20;

    final short level1data[] = {
        19, 26, 26, 22, 9, 12, 19, 26, 22, 9, 12, 19, 26, 26, 22,
        37, 11, 14, 17, 26, 26, 20, 15, 17, 26, 26, 20, 11, 14, 37,
        17, 26, 26, 20, 11, 6, 17, 26, 20, 3, 14, 17, 26, 26, 20,
        21, 3, 6, 25, 22, 5, 21, 7, 21, 5, 19, 28, 3, 6, 21,
        21, 9, 8, 14, 21, 13, 21, 5, 21, 13, 21, 11, 8, 12, 21,
        25, 18, 26, 18, 24, 18, 28, 5, 25, 18, 24, 18, 26, 18, 28,
        6, 21, 7, 21, 7, 21, 11, 8, 14, 21, 7, 21, 7, 21, 03,
        4, 21, 5, 21, 5, 21, 11, 10, 14, 21, 5, 21, 5, 21, 1,
        12, 21, 13, 21, 13, 21, 11, 10, 14, 21, 13, 21, 13, 21, 9,
        19, 24, 26, 24, 26, 16, 26, 18, 26, 16, 26, 24, 26, 24, 22,
        21, 3, 2, 2, 6, 21, 15, 21, 15, 21, 3, 2, 2, 06, 21,
        21, 9, 8, 8, 4, 17, 26, 8, 26, 20, 1, 8, 8, 12, 21,
        17, 26, 26, 22, 13, 21, 11, 2, 14, 21, 13, 19, 26, 26, 20,
        37, 11, 14, 17, 26, 24, 22, 13, 19, 24, 26, 20, 11, 14, 37,
        25, 26, 26, 28, 3, 6, 25, 26, 28, 3, 6, 25, 26, 26, 28};

    final int validspeeds[] = {1, 2, 3, 4, 6, 8};
    final int maxspeed = 6;

    int currentspeed = 3;
    short[] screendata;

    public String getAppletInfo() {
        return ("PacMan - by Brian Postma");
    }

    public void start() {
        resize(360, 400);
        short i;
        GetImages();
        d = size();
        screendata = new short[nrofblocks * nrofblocks];
        Graphics g;
        setBackground(Color.black);
        g = getGraphics();
        g.setFont(smallfont);
        fmsmall = g.getFontMetrics();
        g.setFont(largefont);
        fmlarge = g.getFontMetrics();
        ghostx = new int[maxghosts];
        ghostdx = new int[maxghosts];
        ghosty = new int[maxghosts];
        ghostdy = new int[maxghosts];
        ghostspeed = new int[maxghosts];
        dx = new int[4];
        dy = new int[4];
        // Start
        if (thethread == null) {
            thethread = new Thread(this);
            thethread.start();
        }
        GameInit();
    }

    public void GameInit() {
        pacsleft = 3;
        score = 0;
        scaredtime = maxscaredtime;
        LevelInit();
        nrofghosts = 6;
        currentspeed = 3;
        scaredtime = maxscaredtime;
    }

// inicializa dados para desenhar a tela
    public void LevelInit() {
        int i;
        // copia os dados da matriz "level1data" para a matrriz "screendata"
        for (i = 0; i < nrofblocks * nrofblocks; i++) {
            screendata[i] = level1data[i];
        }

        LevelContinue();
    }

// inicializa dados para movimentação dos "fantasmas" e do pac-man
    public void LevelContinue() {
        short i;
// ajusta movimento inicial de todos os fantasmas para a direita
        int dx = 1;
        int random;
// inicializa todos os fantasmas
        for (i = 0; i < nrofghosts; i++) {
            // ajusta as posições x e y de todos os fantasmas
            // 7 * blocksize (faz com que os fantasmas comecem no meio da tela)
            ghosty[i] = 7 * blocksize;
            ghostx[i] = 7 * blocksize;
            // ajusta o movimento do fantasma atual para direita (dx = 1)
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            // ajusta o movimento do próximo fantasma para a esquerda (dx = -1)
            dx = -dx;
            // sorteia uma velocidade para cada fantasma  (entre 1 e 4)
            random = (int) (Math.random() * (currentspeed + 1));
            // se a velocidade for 4 ajusta esta velocidade para 3
            if (random > currentspeed) {
                random = currentspeed;
            }
            // atribui a velocidade sorteada para cada fantasma
            ghostspeed[i] = validspeeds[random];
        }
        // ajusta as duas bordas da "casa dos fantasmas"
        screendata[7 * nrofblocks + 6] = 10;
        screendata[7 * nrofblocks + 8] = 10;
        // ajusta a posicão horizontal do pac-man no centro
        pacmanx = 7 * blocksize;
        // ajusta a posicão vertical do pac-man na 11 posição
        pacmany = 11 * blocksize;
        // deixa inerte a movimentação do pac-man
        pacmandx = 0;
        pacmandy = 0;
        // informa que não foi precionada nenhuma tecla de direção
        reqdx = 0;
        reqdy = 0;
        // deiva o pac-man virado da a esquerda
        viewdx = -1;
        viewdy = 0;
        // informa que o pac-man não está morrendo nem o fantasma está assustado
        dying = false;
        scared = false;
    }

// inicializa imagens do jogo
    public void GetImages() {
        thetracker = new MediaTracker(this);
// recupera imagens GIF e as armazena em objetos "Image"
        ghost1 = Toolkit.getDefaultToolkit().getImage("pacpix/Ghost1.gif");
        thetracker.addImage(ghost1, 0);
        ghost2 = Toolkit.getDefaultToolkit().getImage("pacpix/Ghost2.gif");
        thetracker.addImage(ghost2, 0);
        ghostscared1 = Toolkit.getDefaultToolkit().getImage("pacpix/GhostScared1.gif");
        thetracker.addImage(ghostscared1, 0);
        ghostscared2 = Toolkit.getDefaultToolkit().getImage("pacpix/GhostScared2.gif");
        thetracker.addImage(ghostscared2, 0);

        pacman1 = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan1.gif");
        thetracker.addImage(pacman1, 0);
        pacman2up = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan2up.gif");
        thetracker.addImage(pacman2up, 0);
        pacman3up = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan3up.gif");
        thetracker.addImage(pacman3up, 0);
        pacman4up = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan4up.gif");
        thetracker.addImage(pacman4up, 0);

        pacman2down = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan2down.gif");
        thetracker.addImage(pacman2down, 0);
        pacman3down = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan3down.gif");
        thetracker.addImage(pacman3down, 0);
        pacman4down = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan4down.gif");
        thetracker.addImage(pacman4down, 0);

        pacman2left = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan2left.gif");
        thetracker.addImage(pacman2left, 0);
        pacman3left = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan3left.gif");
        thetracker.addImage(pacman3left, 0);
        pacman4left = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan4left.gif");
        thetracker.addImage(pacman4left, 0);

        pacman2right = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan2right.gif");
        thetracker.addImage(pacman2right, 0);
        pacman3right = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan3right.gif");
        thetracker.addImage(pacman3right, 0);
        pacman4right = Toolkit.getDefaultToolkit().getImage("pacpix/PacMan4right.gif");
        thetracker.addImage(pacman4right, 0);
// espera até que todas as imagens estejam armazenadas
        try {
            thetracker.waitForAll();
        } catch (InterruptedException e) {
            return;
        }
    }

// ajusta movimentação do pac-man
    public boolean keyDown(Event e, int key) {
// se está "em jogo" ajusta os movimento
// OBS: regdx indica movimento horizontal e regdy indica movimento vertical
        if (ingame) {
            if (key == Event.LEFT) {
                reqdx = -1;
                reqdy = 0;
            } else if (key == Event.RIGHT) {
                reqdx = 1;
                reqdy = 0;
            } else if (key == Event.UP) {
                reqdx = 0;
                reqdy = -1;
            } else if (key == Event.DOWN) {
                reqdx = 0;
                reqdy = 1;
            } else if (key == Event.ESCAPE) {
                ingame = false;
            }
        } // se não está "em jogo" espera o usuário teclar 's' ou 'S'
        else {
            if (key == 's' || key == 83) {
                ingame = true;
                GameInit();
            }
        }
        return true;
    }

// zera as variavies que definem a movimentção do pac-man (não para movimentação)
    public boolean keyUp(Event e, int key) {
// se soltar as teclas de movimento sera os variáveis de movimentação
        if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN) {
            reqdx = 0;
            reqdy = 0;
        }
        return true;
    }

// Desenha o tela grafica
    public void paint(Graphics g) {
        String s;
        Graphics gg;
        d = size();
// cria um buffer da tela grafica (gera otimização no desempenho)
        if (goff == null && d.width > 0 && d.height > 0) {
            ii = createImage(d.width*2, d.height*2);
            goff = ii.getGraphics();
        }
        if (goff == null || ii == null) {
            return;
        }
// desenha um retângulo preto qu envolve toda a tela (limpa a tela)
        goff.setColor(Color.black);
        goff.fillRect(0, 0, d.width, d.height);
// Desenha o cenário
        DrawMaze();
// Desenha o placar
        DrawScore();
// Faz a animação (alterna entre tipos de fantasmas e pac-men)
        DoAnim();
// Inicia o jogo
        if (ingame) {
            PlayGame();
        } // Inicia a apresentação
        else {
            PlayDemo();
        }
// Copia o "buffer de video" para o applet
        g.drawImage(ii, 0, 0, this);
    }

// alterna os "gifs", criando a ilusão de animação
    public void DoAnim() {
// animcount e animdelay começam com 8
// ghostanimpos começa com 0
// pacanimcount começa com 2

// decrementa animcount
        animcount--;

// se animcount for menor que 0
        if (animcount <= 0) {
//animcount volta para 8
            animcount = animdelay;
// incrementa ghostanimpos
            ghostanimpos++;
// se ghostanimpos for maior ou igual a 2
            if (ghostanimpos >= ghostanimcount) // ghostanimpos volta para 0
            {
                ghostanimpos = 0;
            }
        }
// Ou seja, no final ghostanimpos sempre sai como 0 ou 1 (alternados)

// decrementa pacanimcount
        pacanimcount--;
// se pacanimcount form menor ou igual a o
        if (pacanimcount <= 0) {
// pacanimcount volta para 2
            pacanimcount = pacanimdelay;

            pacmananimpos = pacmananimpos + pacanimdir;
            if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0) {
                pacanimdir = -pacanimdir;
            }
        }
    }

    public void PlayGame() {
        if (dying) {
// chama o método que mostra o pac-man morrendo
            Death();
        } else {
// chama o método que altera o cenário enquando os fantasmas estão assustados
            CheckScared();
// chama o método que move o pac-man
            MovePacMan();
// chama o método que desenha o pac-man
            DrawPacMan();
// chama o método que move os fantasmas
            MoveGhosts();
// chama o método que verifica o cenário
            CheckMaze();
        }
    }

    public void PlayDemo() {
// chama o método que altera o cenário enquando os fantasmas estão assustados
        CheckScared();
// chama o método que move os fantasmas
        MoveGhosts();
// chama o método que mostra a tela de abertura
        ShowIntroScreen();
    }

    public void Death() {
        int k;
// "deathcounter" começa com 64
        deathcounter--;
// usa manipulação bit a bit para saber a posição pac-mam
// qualquer valor com "& 15" so pode dar de 0 a 15, que dividido por 4
// dá intervalos regulares de 0 a 3 (quando convertidos para inteiro)
        k = (deathcounter & 15) / 4;
        switch (k) {
// Desenha Pac-man para cima
            case 0:
                goff.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
                break;
// Desenha Pac-man para direita
            case 1:
                goff.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
                break;
// Desenha Pac-man para baixo
            case 2:
                goff.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
                break;
// Desenha Pac-man para esquerda
            default:
                goff.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
        }
// Quando acabar o tempo de animação
        if (deathcounter == 0) {
// retira uma vida do pac-man
            pacsleft--;
// Se não houver mais pac-men termina o jogo
            if (pacsleft == 0) {
                ingame = false;
            }
// Inicializa o nivel e recomeça o jogo (independente se "ingame")
            LevelContinue();
        }
    }

    public void MoveGhosts() {
        short i;
        int pos;
        int count;
// define movimento para cada fantasma
        for (i = 0; i < nrofghosts; i++) {
// verifica se um fantasma está na posição de ínicio de um bloco
// ou seja, se ele pode mudar de direção
            if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
// verifica em que indice da matriz do cenário o pac-man está
                pos = ghostx[i] / blocksize + nrofblocks * (int) (ghosty[i] / blocksize);
// inicia uma variavel de identificação dos movimentos possíveis
// (se nenhum movimento for possível "count" continua0)
                count = 0;
// se nesta posição do cenário não existe um barra no lado esquerdo
// e o fantama não está movendo para a direita
                if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
// habilita  como possivel o movimento do fantasma para a esquerda
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
// se nesta posição do cenário não existe um barra no lado de cima
// e o fantama não está movendo para a baixo
                if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
// habilita  como possivel o movimento do fantasma para cima
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
// se nesta posição do cenário não existe uma barra no lado direito
// e o fantama não está movendo para esquerda
                }
                if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
// habilita  como possivel o movimento do fantasma para direita
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
// se nesta posição do cenário não existe uma barra no lado de baixo
// e o fantama não está movendo para cima
                if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
// habilita  como possivel o movimento do fantasma para baixo
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }
// se nada se moveu ainda (count == 0)
                if (count == 0) {
// se todos os lados do cenário estão fechado, o fantasma fica parado
// OBS: 00001111 & 1111 = 1111 (15)
                    if ((screendata[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } else {
// o fantasma se move na direção contrária
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }
                } else {
// se count != 0, ou seja, existe pelo menos um movimento possível
// sorteia para um fantasma um dos movimento possíveis para ele
                    count = (int) (Math.random() * count);
                    if (count > 3) {
                        count = 3;
                    }
                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }
            }
// se um fantasma não está na posição de ínicio de um bloco
// ou seja, se ele não pode mudar de direção
// avança fantasma na direção  e na velociadade em que ele já está
            ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
// chama a função que desenha o fantasma
            DrawGhost(ghostx[i] + 1, ghosty[i] + 1);
// verifica se houve uma colição entre o pac-man e um fantasma
            if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12)
                    && pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12) && ingame) {
// se o fantasma está assustado, acrescenta 10 ponto ao placar
// e coloca o fantasma preso no meio da tela
                if (scared) {
                    score += 10;
                    ghostx[i] = 7 * blocksize;
                    ghosty[i] = 7 * blocksize;
                } // se o fantasma não está assustado, ajusta o status do pac-man como "morrendo"
                else {
                    dying = true;
                    deathcounter = 64;
                }
            }
        }
    }

// desenha os fantasmas na tela
    public void DrawGhost(int x, int y) {
// desenha o fantasma "tipo 0" não assutado
        if (ghostanimpos == 0 && !scared) {
            goff.drawImage(ghost1, x, y, this);
        } // desenha o fantasma "tipo 1" não assutado
        else if (ghostanimpos == 1 && !scared) {
            goff.drawImage(ghost2, x, y, this);
        } // desenha o fantasma "tipo 0" assutado
        else if (ghostanimpos == 0 && scared) {
            goff.drawImage(ghostscared1, x, y, this);
        } // desenha o fantasma "tipo 1" não assutado
        else if (ghostanimpos == 1 && scared) {
            goff.drawImage(ghostscared2, x, y, this);
        }
    }

// move o pac-man e atualiza o cenário
    public void MovePacMan() {
        int pos;
        short ch;
// se foi precionada alguma tecla "contraria" a direção do pac-man
// invertive a sua direção atual
        if (reqdx == -pacmandx && reqdy == -pacmandy) {
// reajusta a direção horizontal e vertical do pac-man
            pacmandx = reqdx;
            pacmandy = reqdy;
// reajusta a visão (gif) do pac-man de acordo com seu movimeto
            viewdx = pacmandx;
            viewdy = pacmandy;
        }
// verifica se o pac-man está na posição de ínicio de um bloco
// ou seja, se ele pode mudar de direção
        if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
// localiza o bloco de cenário em que o pac-man está
            pos = pacmanx / blocksize + nrofblocks * (int) (pacmany / blocksize);
// copia o valor deste bloco de canário para "ch"
            ch = screendata[pos];
// se o pac-man estiver sobre um pílula pequena
// EX: 00011111 & 00010000 = 00010000, outro = 00101111 & 00010000 = 00000000
            if ((ch & 16) != 0) {
// ajusta a posição atual do cenário retirando a pílula pequena
// EX: 00011111 & 00001111 = 00001111, outro = 00010101 & 00001111 = 00000101
                screendata[pos] = (short) (ch & 15);
                score++;
            }
// se o pac-man estiver sobre um pílula grande
// EX: 00101111 & 00100000 = 00100000, outro = 00011111 & 00100000 = 00000000
            if ((ch & 32) != 0) {
// ajusta o status dos fantasmas para assustado
                scared = true;
                scaredcount = scaredtime;
// ajusta a posição atual do cenário retirando a pílula pequena
// EX: 00101111 & 00001111 = 00001111, outro = 00100101 & 00001111 = 00000101
                screendata[pos] = (short) (ch & 15);
                score += 5;
            }
// se foi precionada alguma tecla de movimento
            if (reqdx != 0 || reqdy != 0) {
// OBS: Verificação com prova por contradição
// verifica se foi precionada a tecla para a esquerda e
// se nesta posição do cenário não existe um barra no lado esquerdo,
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || // ou se foi precionada a tecla para a direita e
                        // se nesta posição do cenário não existe um barra no lado direito,
                        (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || // ou se foi precionada a tecla para a cima e
                        // se nesta posição do cenário não existe um barra no lado superior,
                        (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || // ou se foi precionada a tecla para a baixo e
                        // se nesta posição do cenário não existe um barra no lado inferior
                        (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
// reajusta a direção horizontal e vertical do pac-man
                    pacmandx = reqdx;
                    pacmandy = reqdy;
// reajusta a visão (gif) do pac-man de acordo com seu movimeto
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }

// se o pac-man estiver indo para a esquerda
// e existir um barra no lado esquerdo,
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0)
                    || // ou se o pac-man estiver indo para a direita
                    // e existir um barra no lado direito,
                    (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0)
                    || // ou se o pac-man estiver indo para a cima
                    // e existir um barra no lado superior,
                    (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0)
                    || // ou se o pac-man estiver indo para a baixo
                    // e existir um barra no lado inferior,
                    (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
// deixa a movimentação do pac-mam inerte
                pacmandx = 0;
                pacmandy = 0;
            }
        }
// ajusta a posição do pac-man no cenário,
// de acordo com seu movimento e sua velociadade
        pacmanx = pacmanx + pacmanspeed * pacmandx;
        pacmany = pacmany + pacmanspeed * pacmandy;
    }

    public void DrawPacMan() {
        if (viewdx == -1) {
            DrawPacManLeft();
        } else if (viewdx == 1) {
            DrawPacManRight();
        } else if (viewdy == -1) {
            DrawPacManUp();
        } else {
            DrawPacManDown();
        }
    }

    public void DrawPacManUp() {
        switch (pacmananimpos) {
            case 1:
                goff.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                goff.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                goff.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    public void DrawPacManDown() {
        switch (pacmananimpos) {
            case 1:
                goff.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                goff.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                goff.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    public void DrawPacManLeft() {
        switch (pacmananimpos) {
            case 1:
                goff.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                goff.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                goff.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    public void DrawPacManRight() {
        switch (pacmananimpos) {
            case 1:
                goff.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                goff.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                goff.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

// Desenha o Cenário da tela
    public void DrawMaze() {
        short i = 0;
        int x, y;

// define a cor das bordas
        bigdotcolor = bigdotcolor + dbigdotcolor;
        if (bigdotcolor <= 64 || bigdotcolor >= 192) {
            dbigdotcolor = -dbigdotcolor;
        }
// faz um loop que preenche todas as posições horizontais e verticais (15 x 15)
// loop para desenhar blocos de cenário na vertical
        for (y = 0; y < scrsize; y += blocksize) {
// loop para desenhar blocos de cenário na horizontal
            for (x = 0; x < scrsize; x += blocksize) {
// escolhe a cor das bordas (azul ou vinho)
// se o primeiro bit do byte atual de screendata estiver ligado (1)
// desenha um linha no lado esquerdo do atual bloco de cenário
                goff.setColor(mazecolor);
                if ((screendata[i] & 1) != 0) {
                    goff.drawLine(x, y, x, y + blocksize - 1);
                }
// se o segundo bit do byte atual de screendata estiver ligado (1)
// desenha um linha no lado superior do atual bloco de cenário
                if ((screendata[i] & 2) != 0) {
                    goff.drawLine(x, y, x + blocksize - 1, y);
                }
// se o terceiro bit do byte atual de screendata estiver ligado (1)
// desenha um linha no lado direito do atual bloco de cenário
                if ((screendata[i] & 4) != 0) {
                    goff.drawLine(x + blocksize - 1, y, x + blocksize - 1, y + blocksize - 1);
                }
// se o quarto bit do byte atual de screendata estiver ligado (1)
// desenha um linha no lado inferior do atual bloco de cenário
                if ((screendata[i] & 8) != 0) {
                    goff.drawLine(x, y + blocksize - 1, x + blocksize - 1, y + blocksize - 1);
                }
// se o quito bit do byte atual de screendata estiver ligado (1) ajusta
// a cor para amarelo e desenha um ponto no centro do atual bloco de cenário
                if ((screendata[i] & 16) != 0) {
                    goff.setColor(dotcolor);
                    goff.fillRect(x + 11, y + 11, 2, 2);
                }
// se o sexto bit do byte atual de screendata estiver ligado (1) ajusta
// a cor para laranja e desenha uma pastilha no cecntro atual bloco de cenário
                if ((screendata[i] & 32) != 0) {
                    goff.setColor(new Color(224, 224 - bigdotcolor, bigdotcolor));
                    goff.fillRect(x + 8, y + 8, 8, 8);
                }
                i++;
            }
        }
    }

    public void ShowIntroScreen() {
        String s;

        goff.setFont(largefont);

        goff.setColor(new Color(0, 32, 48));
        goff.fillRect(16, scrsize / 2 - 40, scrsize - 32, 80);
        goff.setColor(Color.white);
        goff.drawRect(16, scrsize / 2 - 40, scrsize - 32, 80);

        if (showtitle) {
            s = "Java PacMan";
            scared = false;

            goff.setColor(Color.white);
            goff.drawString(s, (scrsize - fmlarge.stringWidth(s)) / 2 + 2, scrsize / 2 - 20 + 2);
            goff.setColor(new Color(96, 128, 255));
            goff.drawString(s, (scrsize - fmlarge.stringWidth(s)) / 2, scrsize / 2 - 20);

            s = "(c)2000 by Brian Postma";
            goff.setFont(smallfont);
            goff.setColor(new Color(255, 160, 64));
            goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2, scrsize / 2 + 10);

            s = "b.postma@hetnet.nl";
            goff.setColor(new Color(255, 160, 64));
            goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2, scrsize / 2 + 30);
        } else {
            goff.setFont(smallfont);
            goff.setColor(new Color(96, 128, 255));
            s = "'S' to start game";
            goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2, scrsize / 2 - 10);
            goff.setColor(new Color(255, 160, 64));
            s = "Use cursor keys to move";
            goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2, scrsize / 2 + 20);
            scared = true;
        }
        count--;
        if (count <= 0) {
            count = screendelay;
            showtitle = !showtitle;
        }
    }

// Desenha o placar na tela
    public void DrawScore() {
        int i;
        String s;
// ajusta a fonte e cor da string a ser desenhada
        goff.setFont(smallfont);
        goff.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
// desenha a palavra "Score" na parte inferior direita da tela
        goff.drawString(s, scrsize / 2 + 96, scrsize + 16);
// desenha os pac-men restantes na parte inferior esquerda da tela
        for (i = 0; i < pacsleft; i++) {
            goff.drawImage(pacman3left, i * 28 + 8, scrsize + 1, this);
        }
    }

    public void CheckScared() {
        scaredcount--;
        if (scaredcount <= 0) {
            scared = false;
        }

        if (scared && scaredcount >= 30) {
            mazecolor = new Color(192, 32, 255);
        } else {
            mazecolor = new Color(32, 192, 255);
        }

        if (scared) {
            screendata[7 * nrofblocks + 6] = 11;
            screendata[7 * nrofblocks + 8] = 14;
        } else {
            screendata[7 * nrofblocks + 6] = 10;
            screendata[7 * nrofblocks + 8] = 10;
        }
    }

    public void CheckMaze() {
        short i = 0;
        boolean finished = true;

        while (i < nrofblocks * nrofblocks && finished) {
            if ((screendata[i] & 48) != 0) {
                finished = false;
            }
            i++;
        }
        if (finished) {
            score += 50;
            DrawScore();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            if (nrofghosts < maxghosts) {
                nrofghosts++;
            }
            if (currentspeed < maxspeed) {
                currentspeed++;
            }
            scaredtime = scaredtime - 20;
            if (scaredtime < minscaredtime) {
                scaredtime = minscaredtime;
            }
            LevelInit();
        }
    }

    public void run() {
        long starttime;
        Graphics g;

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        g = getGraphics();

        while (true) {
            starttime = System.currentTimeMillis();
            try {
                paint(g);
                starttime += 40;
                Thread.sleep(Math.max(0, starttime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stop() {
        if (thethread != null) {
            thethread.stop();
            thethread = null;
        }
    }
}
