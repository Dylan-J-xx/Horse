package com.dyh.horse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Horse extends Frame{
    private static final long serialVersionUID = 1L;
    /*
     * 每个格子的边长
     */
    private static int size=80;//方格的长和宽
    /*
     * 这里更改屏幕的尺寸，宽和高为size（方框的边长）的整数倍
     */
    private static int WIDTH=20*size;
    private static int HEIGHT=12*size;
    /*
     * 定义棋盘的大小，必须小于平屏幕大小
     */
    private static int squareWidth=WIDTH-size;//为了让棋盘在界面中间，减去一格
    private static int squareHeight=HEIGHT-size;
    /*
     * 这里更改起点和终点，坐标x,y必须为size（方框的边长）的整数倍
     */
    private static Point starPoint=new Point(80,400);
    private static Point endPoint=new Point(500,400);
    /**
     * 设置界面大小和起点，终点,根据电脑尺寸大小合理设置，否则屏幕可能放不下
     * @author DYH
     * @param length_of_side 设置每个方格的边长
     * @param row_num 设置每行的方格数量
     * @param column_num 设置没列的方格数量
     * @param start_x 设置起点坐标x,x为边长（length_of_side）的整数倍,否则点不会落在线条相交处
     * @param start_y 设置起点坐标y，y为边长（length_of_side）的整数倍,否则点不会落在线条相交处
     * @param end_x 设置终点坐标x，x为边长（length_of_side）的整数倍,否则点不会落在线条相交处
     * @param end_y 设置终点坐标y，y为边长（length_of_side）的整数倍,否则点不会落在线条相交处
     */
    public Horse(int length_of_side,int row_num,int column_num,int start_x,int start_y,int end_x,int end_y) {
        size=length_of_side;
        squareWidth=length_of_side*(row_num+1);
        squareHeight=length_of_side*(column_num+1);
        WIDTH=length_of_side*(row_num+2);
        HEIGHT=length_of_side*(column_num+2);
        starPoint.x=start_x;
        starPoint.y=start_y;
        endPoint.x=end_x;
        endPoint.y=end_y;

        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setTitle("跳马问题");
        this.addWindowListener(new WindowClose());
        tryPoints.add(starPoint);
        MyThread myThread=new MyThread();
        myThread.setName("绘画");
        myThread.start();

        TryPathThread tryPathThread =new TryPathThread();
        tryPathThread.setName("找路径");
        tryPathThread.start();
        try {
            out =new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * 关闭窗口
     */
    class WindowClose extends WindowAdapter{
        @Override
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    /**
     * 绘画线程，画出正确路径
     */
    private class MyThread extends Thread{
        @Override
        public void run() {
            while (true) {
                try {
                    repaint();
                    sleep(20);
                } catch (Exception e) {

                }
            }
        }
    }
    /**
     * 找路径的线程
     */
    private class TryPathThread extends Thread{
        @Override
        public void run() {
            while (tryPoints.size()>0) {
                jump();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    Image bgImage;
    @Override
    public void update(Graphics g) {
        bgImage=this.createImage(this.getWidth(),this.getHeight());
        Graphics bg=bgImage.getGraphics();
        paint(bg);
        bg.dispose();
        g.drawImage(bgImage, 0, 0,null);
    }

    Path temp;
    Point tempP;
    Point n;//下一个点
    Point l;//上一个点
    @Override
    public void paint(Graphics g) {
        drawSquare(g);
//		System.out.println("正确路径有："+rightPaths.size()+"条");

        g.drawString("找到路径："+rightPaths.size()+"条", WIDTH/2-100, 50);
        for (int i = 0; i < rightPaths.size(); i++) {
            temp=rightPaths.get(i);
            for (int j = 0; j <temp.path.size(); j++) {
                tempP=temp.path.get(j);
                l=tempP;
                if(j+1<temp.path.size()){
                    n=temp.path.get(j+1);
                }
                g.drawLine(l.x, l.y, n.x, n.y);
            }
        }

    }
    /**
     * 画出方格
     */
    private void drawSquare(Graphics g){
        //画出所有的方格
        for (int i = size; i <= squareWidth; i+=size) {
            for (int j = size; j <= squareHeight; j+=size) {
                g.drawLine(i, j, squareWidth, j);//画横线
                g.drawLine(i, j, i, squareHeight);//画竖线
                g.drawString("("+i+","+j+")", i, j);
            }
        }
        //画出起点和终点
        g.setColor(Color.RED);
        g.setFont(new Font("", Font.BOLD, 20));
        g.drawString("起点("+starPoint.x+","+starPoint.y+")", starPoint.x-10, starPoint.y+5-30);//起点
        g.drawString("●", starPoint.x-10, starPoint.y+5);//起点
        g.drawString("终点("+endPoint.x+","+endPoint.y+")", endPoint.x-10, endPoint.y+5-30);//终点
        g.drawString("●", endPoint.x-10, endPoint.y+5);//终点
    }
    private Point nowPoint=starPoint;
    private Point nextPoint=new Point();
    private ArrayList<Point> tryPoints =new ArrayList<Point>();//当前路线走过的点
    private ArrayList<Path> rightPaths =new ArrayList<Path>();//正确路线
    private Path rightPath;

    /**
     * 开始跳，找路径
     */
    private void jump(){
        //1
        nowPoint =tryPoints.get(tryPoints.size()-1);
        nextPoint=new Point(nowPoint.x+size,nowPoint.y-2*size);
        tryWay();

        //2
        nowPoint =tryPoints.get(tryPoints.size()-1);
        nextPoint=new Point(nowPoint.x+2*size,nowPoint.y-size);
        tryWay();

        //3
        nowPoint =tryPoints.get(tryPoints.size()-1);
        nextPoint=new Point(nowPoint.x+2*size,nowPoint.y+size);
        tryWay();

        //4
        nowPoint =tryPoints.get(tryPoints.size()-1);
        nextPoint=new Point(nowPoint.x+size,nowPoint.y+2*size);
        tryWay();

        //当运行到这时，已经将此点的四个方向全都走完，返回到上一个点继续执行上一个点的步骤
//		System.out.println("退出循环tryPints长度为："+tryPoints.size());
        tryPoints.remove(tryPoints.size()-1);
        return;
    }
    /**
     * 尝试到下一个点
     */
    String path;
    private void tryWay(){
        if(nextPoint.y>=80&&nextPoint.x>=80&&nextPoint.y<=squareHeight&&nextPoint.x<=endPoint.x){//下一个点nexPoint未越界,下一个点不能到达终点的右侧
//			System.out.println("1");
            tryPoints.add(nextPoint);//将下一个点加入到尝试的路线
//			g.drawLine(nowPoint.x, nowPoint.y, nextPoint.x, nextPoint.y);
//			System.out.println("nowPoint:("+nextPoint.x+","+nextPoint.y+")");
            //判断是否到了终点
            if(nextPoint.x==endPoint.x&&nextPoint.y==endPoint.y){//如果是终点
                rightPath=new Path();
                path="";
                System.out.print("第"+(rightPaths.size()+1)+"条：");
                path+="第"+(rightPaths.size()+1)+"条：";
                for(int i=0;i<tryPoints.size();i++){
                    rightPath.path.add(tryPoints.get(i));
                    System.out.print("("+tryPoints.get(i).x+","+tryPoints.get(i).y+"),");
                    path+="("+tryPoints.get(i).x+","+tryPoints.get(i).y+"),";
                }

                System.out.println();//换行
                writeToPath(path);
                rightPaths.add(rightPath);
                jump();//是不是终点都要跳！！！重要！！！
            }else {
//				System.out.println("jump");
                jump();
            }
        }
    }
    /**
     * 将正确路径写入paths.txt文件中
     */
    FileOutputStream out;
    File outFile=new File("./paths.txt");
    private void writeToPath(String path){
        try {
            if(!outFile.exists()){
                outFile.createNewFile();
            }

            out.write((path+"\n").getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 正确路径类
     */
    private class Path {
        public ArrayList<Point> path=new ArrayList<Point>();
    }

    /**
     * 入口main方法
     */
    public static void main(String[] args) {

        new Horse(80,8, 4, 80, 80, 320,320);
    }
}
