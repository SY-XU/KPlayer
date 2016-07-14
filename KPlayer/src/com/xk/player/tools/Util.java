/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xk.player.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * 一个工具类，主要负责分析歌词
 * 并找到歌词下载下来，然后保存成标准格式的文件
 * 还有一些常用的方法
 * @author hadeslee
 */
public final class Util {

    public static String VERSION = "1.2";//版本号,用于对比更新
    private static Logger log = Logger.getLogger(Util.class.getName());
    private static final JPanel panel = new JPanel();
    private static final JFileChooser jfc = new JFileChooser();

    private Util() {
    }


    /**
     * 根据远程取到的版本和现在的版本对比
     * 看能不能更新
     * @param version 远程的版本
     * @return 能不能更新
     */
    private static boolean canUpdate(String version) {
        if (version == null) {
            return false;
        }
        return VERSION.compareTo(version) < 0;
    }



    /**
     * 一个简便的生成一系列渐变颜色的方法,一般是生成128
     * 个颜个,供可视化窗口用
     * @param c1 第一种颜色
     * @param c2 第二种颜色
     * @param c3 第三种颜色
     * @param count 生成几种颜色
     * @return 渐变色
     */
    public static Color[] getColors(Color c1, Color c2, Color c3, int count) {
        if (count < 3) {
            throw new IllegalArgumentException("总颜色数不能少于3!");
        }
        Color[] cs = new Color[count];
        int half = count / 2;
        float addR = (c2.getRed() - c1.getRed()) * 1.0f / half;
        float addG = (c2.getGreen() - c1.getGreen()) * 1.0f / half;
        float addB = (c2.getBlue() - c1.getBlue()) * 1.0f / half;
//        log.log(Level.INFO, "addR="+addR+",addG="+addG+",addB="+addB);
        int r = c1.getRed();
        int g = c1.getGreen();
        int b = c1.getBlue();
        for (int i = 0; i < half; i++) {
            cs[i] = new Color((int) (r + i * addR), (int) (g + i * addG), (int) (b + i * addB));
//            log.log(Level.INFO, "cs["+i+"]="+cs[i]);
        }
        addR = (c3.getRed() - c2.getRed()) * 1.0f / half;
        addG = (c3.getGreen() - c2.getGreen()) * 1.0f / half;
        addB = (c3.getBlue() - c2.getBlue()) * 1.0f / half;
        r = c2.getRed();
        g = c2.getGreen();
        b = c2.getBlue();
        for (int i = half; i < count; i++) {
            cs[i] = new Color((int) (r + (i - half) * addR), (int) (g + (i - half) * addG), (int) (b + (i - half) * addB));
//            log.log(Level.INFO, "cs["+i+"]="+cs[i]);
        }
        return cs;
    }

    /**
     * 根据特定的颜色生成一个图标的方法
     * @param c 颜色
     * @param width 宽度
     * @param height 高度
     * @return 图标
     */
    public static ImageIcon createColorIcon(Color c, int width, int height) {
        BufferedImage bi = createImage(c, width, height);
        return new ImageIcon(bi);
    }

    /**
     * 根据特定的颜色,生成这个颜色的一张图片
     * 一般用于显示在图片按钮上做为ICON的
     * @param c 颜色
     * @param width 图片的宽度
     * @param height 图片的高度
     * @return 生成的图片
     */
    public static BufferedImage createImage(Color c, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(c);
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(128, 128, 128));
        g.drawRect(0, 0, width - 1, height - 1);
        g.setColor(new Color(236, 233, 216));
        g.drawRect(1, 1, width - 3, height - 3);
        return bi;
    }




	/**
     * 从一个int值得到它所代表的字节数组
     * @param i 值 
     * @return 字节数组
     */
    public static byte[] getBytesFromInt(int i) {
        byte[] data = new byte[4];
        data[0] = (byte) (i & 0xff);
        data[1] = (byte) ((i >> 8) & 0xff);
        data[2] = (byte) ((i >> 16) & 0xff);
        data[3] = (byte) ((i >> 24) & 0xff);
        return data;
    }


    /**
     * 一个简便的方法，把一个字符串的转成另一种字符串
     * @param source 源字符串
     * @param encoding 编码
     * @return 新的字符串
     */
    public static String convertString(String source, String encoding) {
        try {
            byte[] data = source.getBytes("ISO8859-1");
            return new String(data, encoding);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }

    /**
     * 转码的一个方便的方法
     * @param source 要转的字符串
     * @param sourceEnc 字符串原来的编码
     * @param distEnc 要转成的编码
     * @return 转后的字符串
     */
    public static String convertString(String source, String sourceEnc, String distEnc) {
        try {
            byte[] data = source.getBytes(sourceEnc);
            return new String(data, distEnc);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }

    /**
     * 从传进来的数得到这个数组
     * 组成的整型的大小
     * @param data 数组
     * @return 整型
     */
    public static int getInt(byte[] data) {
        if (data.length != 4) {
            throw new IllegalArgumentException("数组长度非法,要长度为4!");
        }
        return (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
    }

    /**
     * 从传进来的字节数组得到
     * 这个字节数组能组成的长整型的结果
     * @param data 字节数组
     * @return 长整型
     */
    public static long getLong(byte[] data) {
        if (data.length != 8) {
            throw new IllegalArgumentException("数组长度非法,要长度为4!");
        }
        return (data[0] & 0xff) |
                ((data[1] & 0xff) << 8) |
                ((data[2] & 0xff) << 16) |
                ((data[3] & 0xff) << 24) |
                ((data[4] & 0xff) << 32) |
                ((data[5] & 0xff) << 40) |
                ((data[6] & 0xff) << 48) |
                ((data[7] & 0xff) << 56);
    }


    /**
     * 根据一个文件的全路径得到它的扩展名
     * @param path 全路径
     * @return  扩展名
     */
    public static String getExtName(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    /**
     * 得到两个矩形的距离
     * @param rec1 矩形1
     * @param rec2 矩形2
     * @return 距离
     */
    public static int getDistance(Rectangle rec1, Rectangle rec2) {
        if (rec1.intersects(rec2)) {
            return Integer.MAX_VALUE;
        }
        int x1 = (int) rec1.getCenterX();
        int y1 = (int) rec1.getCenterY();
        int x2 = (int) rec2.getCenterX();
        int y2 = (int) rec2.getCenterY();
        int dis1 = Math.abs(x1 - x2) - rec1.width / 2 - rec2.width / 2;
        int dis2 = Math.abs(y1 - y2) - rec1.height / 2 - rec2.height / 2;
        return Math.max(dis1, dis2) - 1;
    }


    /**
     * 根据一些参数快速地构造出按钮来
     * 这些按钮从外观上看都是一些特殊的按钮
     * @param name 按钮图片的相对地址
     * @param cmd 命令
     * @param listener 监听器
     * @return 按钮
     */
    public static JButton createJButton(String name, String cmd, ActionListener listener) {
        Image[] icons = Util.getImages(name, 3);
        JButton jb = new JButton();
        jb.setBorderPainted(false);
        jb.setFocusPainted(false);
        jb.setContentAreaFilled(false);
        jb.setDoubleBuffered(true);
        jb.setIcon(new ImageIcon(icons[0]));
        jb.setRolloverIcon(new ImageIcon(icons[1]));
        jb.setPressedIcon(new ImageIcon(icons[2]));
        jb.setOpaque(false);
        jb.setFocusable(false);
        jb.setActionCommand(cmd);
        jb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jb.addActionListener(listener);
        return jb;
    }

    /**
     * 根据一些参数快速地构造出按钮来
     * 这些按钮从外观上看都是一些特殊的按钮
     * @param name 按钮图片的相对地址
     * @param cmd 命令
     * @param listener 监听器
     * @param selected 是否被选中了
     * @return 按钮
     */
    public static JToggleButton createJToggleButton(String name, String cmd, ActionListener listener, boolean selected) {
        Image[] icons = Util.getImages(name, 3);
        JToggleButton jt = new JToggleButton();
        jt.setBorder(null);
        jt.setContentAreaFilled(false);
        jt.setFocusPainted(false);
        jt.setDoubleBuffered(true);
        jt.setIcon(new ImageIcon(icons[0]));
        jt.setRolloverIcon(new ImageIcon(icons[1]));
        jt.setSelectedIcon(new ImageIcon(icons[2]));
        jt.setOpaque(false);
        jt.setFocusable(false);
        jt.setActionCommand(cmd);
        jt.setSelected(selected);
        jt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jt.addActionListener(listener);
        return jt;
    }

    /**
     * 得到一系列的图片，以数字递增做为序列的
     * @param who 图片的基名
     * @param count 数量
     * @return 图片数组
     */
    public static Image[] getImages(String who, int count) {
        Image[] imgs = new Image[3];
        MediaTracker mt = new MediaTracker(panel);
        Toolkit tk = Toolkit.getDefaultToolkit();
        for (int i = 1; i <= count; i++) {
            URL url = Util.class.getResource("/com/hadeslee/yoyoplayer/pic/" + who + i + ".png");
            imgs[i - 1] = tk.createImage(url);
            mt.addImage(imgs[i - 1], i);
        }
        try {
            mt.waitForAll();
        } catch (Exception exe) {
            exe.printStackTrace();
        }

        return imgs;
    }

    /**
     * 根据某个URL得到这个URL代表的图片
     * 并且把该图片导入内存
     * @param name URL
     * @return 图片
     */
    public static Image getImage(String name) {
        URL url = Util.class.getResource("/com/hadeslee/yoyoplayer/pic/" + name);
        Image im = Toolkit.getDefaultToolkit().createImage(url);
        try {
            MediaTracker mt = new MediaTracker(panel);
            mt.addImage(im, 0);
            mt.waitForAll();
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        return im;
    }

    /**
     * 根据一个比例得到两种颜色之间的渐变色
     * @param c1 第一种颜色
     * @param c2 第二种颜色
     * @param f 比例
     * @return 新的颜色
     */
    public static Color getGradientColor(Color c1, Color c2, float f) {
        int deltaR = c2.getRed() - c1.getRed();
        int deltaG = c2.getGreen() - c1.getGreen();
        int deltaB = c2.getBlue() - c1.getBlue();
        int r1 = (int) (c1.getRed() + f * deltaR);
        int g1 = (int) (c1.getGreen() + f * deltaG);
        int b1 = (int) (c1.getBlue() + f * deltaB);
        Color c = new Color(r1, g1, b1);
        return c;
    }

    /**
     * 得到两种颜色的混合色
     * @param c1 第一种颜色
     * @param c2 第二种颜色
     * @return 混合色
     */
    public static Color getColor(Color c1, Color c2) {
        int r = (c2.getRed() + c1.getRed()) / 2;
        int g = (c2.getGreen() + c1.getGreen()) / 2;
        int b = (c2.getBlue() + c1.getBlue()) / 2;
        return new Color(r, g, b);
    }

    /**
     * 一个简便地获取字符串高度的方法
     * @param s 字符串
     * @param g 画笔
     * @return 高度
     */
    public static int getStringHeight(String s, Graphics g) {
        return (int) g.getFontMetrics().getStringBounds(s, g).getHeight();
    }

    /**
     * 一个简便地获取字符串宽度的方法
     * @param s 字符串
     * @param g 画笔
     * @return 宽度
     */
    public static int getStringWidth(String s, Graphics g) {
        return (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
    }

    /**
     * 自定义的画字符串的方法，从字符串的左上角开始画
     * 不是JAVA的从左下角开始的画法
     * @param g 画笔
     * @param s 字符串
     * @param x X坐标
     * @param y Y坐标
     */
    public static void drawString(Graphics g, String s, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        g.drawString(s, x, y + asc);
    }

    /**
     * 一个简便的让字符串对于某点居中的画法
     * @param g 画笔
     * @param s 字符串
     * @param x X坐标
     * @param y Y坐标
     */
    public static void drawStringCenter(Graphics g, String s, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int width = getStringWidth(s, g);
        g.drawString(s, x - width / 2, y + asc);
    }

    /**
     * 一个便捷的方法,画字符串右对齐的方法
     * @param g 画笔
     * @param s 字符串
     * @param x 右对齐的X座标
     * @param y 右对齐的Y座标
     */
    public static void drawStringRight(Graphics g, String s, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int width = getStringWidth(s, g);
        g.drawString(s, x - width, y + asc);
    }

    /**
     * 得到文件的格式
     * @param f 文件
     * @return 格式
     */
    public static String getType(File f) {
        String name = f.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * 根据文件名得到歌曲的名字
     * @param f 文件名
     * @return 歌曲名
     */
    public static String getSongName(File f) {
        String name = f.getName();
        name = name.substring(0, name.lastIndexOf("."));
        return name;
    }

    /**
     * 根据文件名得到歌曲的名字
     * @param name 文件名
     * @return 歌曲名
     */
    public static String getSongName(String name) {
        try {
            int index = name.lastIndexOf(File.separator);
            name = name.substring(index + 1, name.lastIndexOf("."));
            return name;
        } catch (Exception exe) {
            return name;
        }

    }


    /**
     * 从一个流里面得到这个流的字符串
     * 表现形式
     * @param is 流
     * @return 字符串
     */
    private static String getString(InputStream is) {
        InputStreamReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            //TODO 这里是固定把网页内容的编码写在GBK,应该是可设置的
            r = new InputStreamReader(is, "GBK");
            char[] buffer = new char[128];
            int length = -1;
            while ((length = r.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length));
            }
            return sb.toString();
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } finally {
            try {
                r.close();
            } catch (Exception ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 秒数转成00:00之类的字符串
     * @param sec 秒数
     * @return 字符串
     */
    public static String secondToString(int sec) {
        DecimalFormat df = new DecimalFormat("00");
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(sec / 60)).append(":").append(df.format(sec % 60));
        return sb.toString();
    }



    /**
     * 去除HTML标记
     * @param str1 含有HTML标记的字符串
     * @return 去除掉相关字符串
     */
    public static String htmlTrim(String str1) {
        String str = "";
        str = str1;
        //剔出了<html>的标签
        str = str.replaceAll("</?[^>]+>", "");
        //去除空格
        str = str.replaceAll("\\s", "");
        str = str.replaceAll("&nbsp;", "");
        str = str.replaceAll("&amp;", "&");
        str = str.replace(".", "");
        str = str.replace("\"", "‘");
        str = str.replace("'", "‘");
        return str;
    }

    private static String htmlTrim2(String str1) {
        String str = "";
        str = str1;
        //剔出了<html>的标签
        str = str.replaceAll("<BR>", "\n");
        str = str.replaceAll("<br>", "\n");
        str = str.replaceAll("</?[^>]+>", "");
        return str;
    }


}

