package demo1;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tetris extends JPanel {
    //正在下落的方块
    private Tetromino currrntOne = Tetromino.randomOne();
    //将要下落的方块
    private Tetromino nextOne = Tetromino.randomOne();

    private Cell[][] wall = new Cell[18][9];

    private static final int CELL_SIZE = 48;
    //游戏主区域
    int[] scores_pool = {0, 1, 2, 5, 10};

    private int totalScore = 0;
    private int totalLine = 0;
    //游戏三种状态
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int GAMEOVER = 2;

    private int game_state;
    //显示游戏状态的数组
    String[] show_state = {"P[pause]", "C[continue]", "S[replay]"};

    public static BufferedImage I;
    public static BufferedImage J;
    public static BufferedImage L;
    public static BufferedImage O;
    public static BufferedImage S;
    public static BufferedImage T;
    public static BufferedImage Z;
    public static BufferedImage backImage;

    static
    {
        try {
            I = ImageIO.read(new File("D:\\javacode\\images\\I.png"));
            J = ImageIO.read(new File("D:\\javacode\\images\\J.png"));
            L = ImageIO.read(new File("D:\\javacode\\images\\L.png"));
            O = ImageIO.read(new File("D:\\javacode\\images\\O.png"));
            S = ImageIO.read(new File("D:\\javacode\\images\\S.png"));
            T = ImageIO.read(new File("D:\\javacode\\images\\T.png"));
            Z = ImageIO.read(new File("D:\\javacode\\images\\Z.png"));
            backImage = ImageIO.read(new File("D:\\javacode\\images\\background.png"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(backImage, 0, 0, null);
        //坐标轴
        g.translate(22, 15);
        //游戏主区域
        paintWall(g);
        //正在下落四方格
        paintCurrentone(g);
        //下一个要下落四方格
        paintNextOne(g);
        //游戏得分
        paintScore(g);
       //当前状态
        paintState(g);
    }
    public void start()
    {
        game_state = PLAYING;
        KeyListener l = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
               int code = e.getKeyCode();
               switch (code)
               {
                   case KeyEvent.VK_DOWN:
                       sortDropAction();
                       break;
                   case KeyEvent.VK_RIGHT:
                       moveRightAction();
                       break;
                   case KeyEvent.VK_LEFT:
                       moveLeftAction();
                       break;
                   case KeyEvent.VK_UP:
                       rotateRightAction();
                       break;
                   case KeyEvent.VK_SPACE:
                       handDropAction();
                       break;
                   case KeyEvent.VK_P:
                       if(game_state == PLAYING)
                           game_state = PAUSE;
                       break;
                   case KeyEvent.VK_C:
                       if(game_state == PAUSE)
                           game_state = PLAYING;
                       break;
                   case KeyEvent.VK_S:
                       game_state = PLAYING;
                       wall = new Cell[18][9];
                       currrntOne = Tetromino.randomOne();
                       nextOne = Tetromino.randomOne();
                       totalLine = 0;
                       totalScore = 0;
                       break;
               }
            }
        };

        this.addKeyListener(l);
        this.requestFocus();

        while(true)
        {
            if(game_state == PLAYING)
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(canDrop())
                {
                    currrntOne.softDrop();
                }
                else
                {
                    landToWall();
                    destroyLine();
                    if(isGameOver())
                    {
                        game_state = GAMEOVER;
                    }
                    else
                    {
                        currrntOne = nextOne;
                        nextOne = Tetromino.randomOne();
                    }
                }
            }
            repaint();
        }
    }
    public void rotateRightAction()
    {
        currrntOne.rotateRight();
        if(outOfBound() || coincide())
        {
            currrntOne.rotateLeft();
        }
    }

    public void handDropAction()
    {
        while(true)
        {
            if(canDrop())
            {
                currrntOne.softDrop();
            }
            else
            {
                break;
            }
        }

        landToWall();
        destroyLine();

        if(isGameOver())
        {
           game_state = GAMEOVER;
        }
        else
        {
            currrntOne = nextOne;
            nextOne = Tetromino.randomOne();
        }
    }
    public void sortDropAction()
    {
        if(canDrop())
        {
            currrntOne.softDrop();
        }
        else
        {
            landToWall();
            destroyLine();
            if(isGameOver())
            {
                game_state = GAMEOVER;
            }
            else
            {
                currrntOne = nextOne;
                nextOne = Tetromino.randomOne();
            }

        }
    }
    public void landToWall()
    {
        Cell[] cells = currrntOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            wall[row][col] = cell;
        }
    }
    public boolean canDrop()
    {
        Cell[] cells = currrntOne.cells;
        for (Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            if(row == wall.length - 1)
            {
                return false;
            }
            else if(wall[row + 1][col] != null)
            {
                return false;
            }
        }
        return true;
    }
    public void destroyLine()
    {
        int line = 0;
        Cell[] cells = currrntOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            if(isFullLine(row))
            {
                line ++;
                for(int i = row; i > 0; i --)
                {
                    System.arraycopy(wall[i - 1], 0, wall[i], 0, wall[0].length);
                }
                wall[0] = new Cell[9];
            }
        }
        totalScore += scores_pool[line];
        totalLine += line;
    }
    public boolean isFullLine(int row)
    {
        Cell[] cells = wall[row];

        for(Cell cell : cells)
        {
            if(cell == null)
                return false;
        }
        return true;
    }
    public boolean isGameOver()
    {
        Cell[] cells = nextOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            if(wall[row][col] != null)
            {
                return true;
            }
        }
        return false;
    }

    private void paintState(Graphics g)
    {
        if(game_state == PLAYING)
        {
            g.drawString(show_state[game_state], 500, 660);
        }
        else if(game_state == PAUSE)
        {
            g.drawString(show_state[game_state], 500, 660);
        }
        else if(game_state == GAMEOVER)
        {
            g.drawString(show_state[game_state], 500, 660);
            g.setColor(Color.RED);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            g.drawString("GAMEOVER!", 30, 400);
        }
    }
    private void paintScore(Graphics g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.drawString("SCORES:" + totalScore, 500, 248);
        g.drawString("LINES:" + totalLine, 500, 430);
    }

    private void paintWall(Graphics g)
    {
        for(int i = 0; i < wall.length; i ++)
            for(int j = 0; j < wall[i].length; j ++)
            {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                Cell cell = wall[i][j];
                if(cell == null)
                {
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                else
                {
                    g.drawImage(cell.getImage(), x, y, null);
                }
            }
    }

    private void paintCurrentone(Graphics g)
    {
        Cell [] cells = currrntOne.cells;
        for(Cell cell : cells)
        {
            int x = cell.getCol() * CELL_SIZE;
            int y = cell.getRow() * CELL_SIZE;
            g.drawImage(cell.getImage(), x ,y, null);
        }
    }
    private void paintNextOne(Graphics g)
    {
        Cell [] cells = nextOne.cells;
        for(Cell cell : cells)
        {
            int x = cell.getCol() * CELL_SIZE + 370;
            int y = cell.getRow() * CELL_SIZE + 25;
            g.drawImage(cell.getImage(), x ,y, null);
        }
    }
    public boolean outOfBound()
    {
        Cell[] cells = currrntOne.cells;
        for(Cell cell : cells) {
            int col = cell.getCol();
            int row = cell.getRow();
            if (row < 0 || row > wall.length - 1 || col < 0 || col > wall[0].length - 1) {
                return true;
            }
        }
        return false;
    }
    public boolean coincide()
    {
        Cell[] cells = currrntOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            if(wall[row][col] != null)
            {
                return true;
            }
        }
        return false;
    }
    public void moveLeftAction()
    {
        currrntOne.moveLeft();
        if(outOfBound() || coincide())
        {
            currrntOne.moveRight();
        }
    }
    public void moveRightAction()
    {
        currrntOne.moveRight();
        if(outOfBound() || coincide())
        {
            currrntOne.moveLeft();
        }
    }

    public static void main(String[] args) {
        //创建窗口
        JFrame frame = new JFrame("俄罗斯方块");
        //创建游戏界面，面板
        Tetris panel = new Tetris();

        frame.add(panel);
        //设置可见
        frame.setVisible(true);

        frame.setSize(810, 940);

        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.start();

    }

}
