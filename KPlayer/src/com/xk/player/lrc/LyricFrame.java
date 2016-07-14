/**
 * @author xiaokui
 *
 *
 *
 *  时间：2014-11-4下午1:42:21
 */
package com.xk.player.lrc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JWindow;

import com.sun.awt.AWTUtilities;
import com.xk.player.core.BasicController;
import com.xk.player.core.BasicPlayerEvent;
import com.xk.player.core.BasicPlayerListener;
import com.xk.player.tools.FileUtils;
import com.xk.player.tools.JSONUtil;
import com.xk.player.tools.KrcText;
import com.xk.player.tools.LrcParser;
import com.xk.player.ui.PlayUI;




/**
 * @项目名称：PGS
 * @类名称：TestSwing.java
 * @类描述：
 * @创建人：xiaokui
 * 时间：2014-11-4下午1:42:21
 */
public class LyricFrame extends JWindow {

	private Integer startX=null;
	private Integer startY=null;
	private MyLyricPanel ly;
	
	public void hide(boolean bool) {
		setVisible(bool);
	}
	
	public LyricFrame(PlayUI ui){
		setVisible(false);
		AWTUtilities.setWindowOpaque(this, false);
		Dimension dis= Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(0, (int)(dis.height*0.65));
		this.setSize(dis.width, 150);
		ly=new MyLyricPanel(ui);
		ly.setBounds(0, 0, dis.width/2, 150);
		add(ly);
		setAlwaysOnTop(true);
		enableDrag();
	}
	
	private void enableDrag(){
		MouseListener ml=new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startX = e.getX();
	            startY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				startX = null;
	            startY = null;
			}
			
		};
		
		this.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if(null!=startX&&null!=startY){
					Point point=LyricFrame.this.getLocation();
					Point to=new Point();
					to.x=point.x+e.getX()-startX;
					to.y=point.y+e.getY()-startY;
					LyricFrame.this.setLocation(to);
				}
			}
			
		});
		addMouseListener(ml);
	}

	public void setLines(List<XRCLine> lines) {
		if(null!=ly){
			ly.setLines(lines);
		}
		
	}

	public MyLyricPanel getLy() {
		return ly;
	}

}
