package com.company;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class GameTetris {

    final String TITLE_OF_PROGRAM = "Tetris";       // задаём константы
    final int BLOC_SIZE = 25;       // размер блока
    final int ARC_RADIUS = 6;       // радиус загиба
    final int FIELD_WIDTH = 10;      // ширина поля в блоках
    final int FIELD_HIGHT = 18;      // высота поля в блоках
    final int START_LOCATIONX = 1500;   //стартовая координата
    final int START_LOCATIONY = 200;   //стартовая координата
    final int FIELD_DX = 7;
    final int FIELD_DY = 26;
    final int LEFT = 37;             // коды клавиш
    final int UP = 38;
    final int RIGHT = 39;
    final int DOWN = 40;
    final int SHOW_DELAY = 400;         // задержка анимации
    final int[][][] SHAPES = {          // фигуры
            {{0,0,0,0}, {1,1,1,1}, {0,0,0,0}, {0,0,0,0}, {4, 0x00f0f0}}, // I
            {{0,0,0,0}, {0,1,1,0}, {0,1,1,0}, {0,0,0,0}, {4, 0xf0f000}}, // O
            {{1,0,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0x0000f0}}, // J
            {{0,0,1,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xf0a000}}, // L
            {{0,1,1,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0}, {3, 0x00f000}}, // S
            {{1,1,1,0}, {0,1,0,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xa000f0}}, // T
            {{1,1,0,0}, {0,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xf00000}}  // Z
    };
    final int[] SCORES = {100, 300, 700, 1500};                  // начисляемые очки за уничтожение 1, 2, 3, 4 строк
    int gameScore = 0;                                          // изначальные очки 0
    int [][] mine = new int[FIELD_HIGHT + 1][FIELD_WIDTH];      // двумерный массив для поля
//    int [][] mine = new int[FIELD_HIGHT - 1][FIELD_WIDTH];      // двумерный массив для поля
    JFrame frame;
    Canvas canvasPanel = new Canvas();
    Random random = new Random();
    Figure figure = new Figure();
    boolean gameOver = false;
    final int[][] GAME_OVER_MSG = {     // надпись GAME OVER
            {0,1,1,0,0,0,1,1,0,0,0,1,0,1,0,0,0,1,1,0},
            {1,0,0,0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,1},
            {1,0,1,1,0,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1},
            {1,0,0,1,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,0},
            {0,1,1,0,0,1,0,0,1,0,1,0,1,0,1,0,0,1,1,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,1,1,0,0,1,0,0,1,0,0,1,1,0,0,1,1,1,0,0},
            {1,0,0,1,0,1,0,0,1,0,1,0,0,1,0,1,0,0,1,0},
            {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,1,0,0},
            {1,0,0,1,0,1,1,0,0,0,1,0,0,0,0,1,0,0,1,0},
            {0,1,1,0,0,1,0,0,0,0,0,1,1,0,0,1,0,0,1,0}};

    public static void main(String[] args) {
        new GameTetris().go();                                  // запуск игры
    }

    void go() {
        frame = new JFrame(TITLE_OF_PROGRAM);                   // окно с названием
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // закрытие крестиком
        frame.setSize(FIELD_WIDTH * BLOC_SIZE + FIELD_DX + 10, FIELD_HIGHT * BLOC_SIZE + FIELD_DY + 10);  // размер окна
        frame.setLocation(START_LOCATIONX, START_LOCATIONY);    // начальная точка отображения окна
        frame.setResizable(false);                              // неизменный размер окна
        canvasPanel.setBackground(Color.BLACK);                 // черный фон

        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed (KeyEvent e) {
                if (!gameOver) {
                    if (e.getKeyCode() == DOWN) figure.drop();
                    if (e.getKeyCode() == UP) figure.rotate();
                    if (e.getKeyCode() == LEFT || e.getKeyCode() == RIGHT) figure.move(e.getKeyCode());
                }
                canvasPanel.repaint();
            }
        });
        frame.getContentPane().add(BorderLayout.CENTER, canvasPanel);
        frame.setVisible(true);

        Arrays.fill(mine[FIELD_HIGHT], 1);     // определяется дно колодца

        // главный цикл игры
        while (!gameOver) {
            try{
                Thread.sleep(SHOW_DELAY);       // вызываем задержку
            } catch (Exception e) { e.printStackTrace(); }
            canvasPanel.repaint();              // перерисовка окна

            if (figure.isTouchGround()) {
                figure.leaveOnTheGround();
                checkFilling();
                figure = new Figure();
                gameOver = figure.isCrossGround();
            } else {
                figure.stepDown();
            }
        }
    }

    void checkFilling() {            // проверка заполнения строк
        int row = FIELD_HIGHT - 1;
        int countFillRows = 0;
        while (row > 0 ) {
            int filled = 1;
            for (int col = 0; col < FIELD_WIDTH; col++)
                filled *= Integer.signum(mine[row][col]);
            if (filled > 0) {
                countFillRows++;
                for (int i = row; i > 0; i--) System.arraycopy(mine[i-1], 0, mine[i], 0, FIELD_WIDTH);
            } else {
                row--;
            }
        }
        if (countFillRows > 0) {
            gameScore += SCORES[countFillRows - 1];
            frame.setTitle(TITLE_OF_PROGRAM + " : " + gameScore);     // вывод счета
        }
    }

    class Figure {
        private ArrayList<Block> figure = new ArrayList<>();
        int [][] shape = new int [4][4];
        private int type, size, color;
        private int x = 3;
        private int y = 0;

        // 03.10.2021

        Figure() {
            type = random.nextInt(SHAPES.length);       // берем рандомную фигуру из таблицы заранее заготовленных
            size = SHAPES[type][4][0];
            color = SHAPES[type][4][1];
            if (size == 4) y = -1;               // было y = -1;
            for (int i = 0; i < size; i++) {
                System.arraycopy(SHAPES[type][i], 0, shape[i], 0, SHAPES[type][i].length);
            }
            createFromShape();
        }

        void createFromShape() {                       // добавление фигуры прорисовка формы
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    // 05.10.2021
                    if (shape[y][x] == 1) figure.add(new Block(x + this.x, y + this.y));    // тут была ошибка перепутаны х у
                }
            }
        }

        public void drop() {
            while (!isTouchGround()) {
                stepDown();
            }
        }

        boolean isTouchWall(int direction) {     // проверка на соприкосновение со стеной
            for (Block block : figure) {
                if (direction == LEFT && (block.getX() == 0 || mine [block.getY()][block.getX() - 1] > 0)) return true;
                if (direction == RIGHT && (block.getX() == FIELD_WIDTH - 1 || mine [block.getY()][block.getX() + 1] > 0)) return true;
            }
            return false;
        }

        void move(int direction) {              // перемещение блока влево/вправо
            if (!isTouchWall(direction) ) {     // проверяем соприкосновение со стеной
                int dx = direction - 38;
                for (Block block : figure)  block.setX(block.getX() + dx);
                x += dx;
            }
        }

        boolean isWrongPosition() {             // проверка - не вылазит ли фигура при вращении за границы поля
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    if (shape[y][x] == 1) {
                        if (y + this.y < 0) return true;
                        if (x + this.x < 0 || x + this.x > FIELD_WIDTH - 1) return true;
                        if (mine[y + this.y][x + this.x] > 0) return true;
                    }
            return false;
        }

        void rotate() {                         // метод поворота фигуры
            for (int i = 0; i < size/2; i++)
                for (int j = i; j < size -1 - i; j++) {
                    int tmp = shape[size - 1 - j][i];
                    shape[size - 1 - j][i] = shape[size - 1 - i][size - 1 - j];
                    shape[size - 1 - i][size - 1 - j] = shape[j][size - 1 - i];
                    shape[j][size - 1 - i] = shape[i][j];
                    shape[i][j] = tmp;
                }
            if (!isWrongPosition()) {       // проверка на возможность поворота
                figure.clear();
                createFromShape();
            }
        }

        boolean isTouchGround() {            // проверка на соприкосновение с землей / др. фигурами
            for (Block block : figure)
                if (mine[1 + block.getY()][block.getX()] > 0)  return true;
                return false;                // иначе возвращает false
        }

        void leaveOnTheGround() {           // метод оставления на месте
            for (Block block : figure) mine[block.getY()][block.getX()] = color;    // поле окрашивается в цвет фигурки
        }

        boolean isCrossGround() {           // проверка пересечения земли
            for (Block block : figure) {
                System.out.println("block.getY(): " + block.getY() + " block.getX(): " + block.getX());
                if (mine[block.getY()][block.getX()] > 0) return true;
            }
            return false;                  // иначе возвращает false
        }

        void stepDown() {                  // падение фигурки
            for (Block block : figure) block.setY(1 + block.getY());
            ++y;
        }

        void paint(Graphics g) {            // рисует фигуру
            for (Block block : figure) block.paint(g, color);
        }
    }

    class Block {
        private int x, y;

        public Block(int x, int y) {
            setX(x);
            setY(y);
        }

        void  setX(int x) { this.x = x; }
        void  setY(int y) { this.y = y; }

        int getX() { return x; }
        int getY() { return y; }

        void  paint(Graphics g, int color) {        // прорисовка блока
            g.setColor(new Color(color));           // цвет
            g.drawRoundRect(x * BLOC_SIZE + 1, y * BLOC_SIZE + 1, BLOC_SIZE - 2, BLOC_SIZE - 2, ARC_RADIUS, ARC_RADIUS);
        }
    }

    public class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            // отрисовка фигур лежащих на земле
            for (int x = 0; x < FIELD_WIDTH; x++)
                for (int y = 0; y < FIELD_HIGHT; y++)
                    if (mine[y][x] > 0) {
                        g.setColor(new Color(mine[y][x]));      // чтобы фигурки оставались на дне
                        g.fill3DRect(x * BLOC_SIZE + 1, y * BLOC_SIZE + 1, BLOC_SIZE - 1, BLOC_SIZE - 1, true);
                    }
            if (gameOver) {
                g.setColor(Color.WHITE);
                for (int y = 0; y < GAME_OVER_MSG.length; y++)
                    for (int x = 0; x < GAME_OVER_MSG[y].length; x++)
                        if (GAME_OVER_MSG[y][x] == 1) g.fill3DRect(x * 11 + 18, y * 11 + 160, 10, 10, true);
            } else
                figure.paint(g);
        }
    }
}
