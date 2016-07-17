package com.xk.player.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

import com.xk.player.tools.Config;
import com.xk.player.tools.FileUtils;
import com.xk.player.tools.SWTResourceManager;
import com.xk.player.tools.SWTTools;
import com.xk.player.tools.SongSeacher;
import com.xk.player.tools.SongSeacher.SearchInfo;
import com.xk.player.uilib.BaseBox;
import com.xk.player.uilib.ListItem;
import com.xk.player.uilib.MyList;
import com.xk.player.uilib.SearchComp;
import com.xk.player.uilib.SearchResultComp;

public class SongItem extends ListItem {

	private Map<String,String> property;
	private int height=30;
	private int selectedHeight=50;
	private Image headDefault=SWTResourceManager.getImage(SongItem.class, "/images/head.png");
	private Image head;
	
	public SongItem(Map<String,String> property){
		this.property=property;
	}
	
	public void put(String key,String prop){
		property.put(key, prop);
	}
	
	@Override
	public int getHeight() {
		return selected?selectedHeight:height;
	}

	
	@Override
	public void unSelect() {
		if(null!=head){
			head.dispose();
			head=null;
		}
		super.unSelect();
	}

	@Override
	public void draw(GC gc, int start,int width,int index) {
		String name=FileUtils.getLimitString(property.get("name"), 18);
		Font font=SWTResourceManager.getFont("黑体", 10, SWT.NORMAL);
		boolean hq=property.get("path").endsWith(".ape");
		if(selected){
			int alf=gc.getAlpha();
			gc.setAlpha(55);
			gc.fillRectangle(0, start, width-MyList.BAR_WIDTH, getHeight());
			gc.setAlpha(alf);
			gc.drawImage((null==head||head.isDisposed())?headDefault:head, 15, start);
			Path path=new Path(null);
			path.addString(name, 15+58f, start+8, font);
			String all=property.get("all");
			if(null!=all){
				path.addString(all, width-MyList.BAR_WIDTH-40, start+30, font);
			}
			String now=property.get("now");
			if(null!=now){
				path.addString(now, 15+58f, start+30, font);
			}
			gc.drawPath(path);
			path.dispose();
		}else if(focused){
			int alf=gc.getAlpha();
			gc.setAlpha(55);
			gc.fillRectangle(0, start, width-MyList.BAR_WIDTH, getHeight());
			gc.setAlpha(alf);
			Path path=new Path(null);
			path.addString(index+"", 30f, start+8, font);
			path.addString(name, 50f, start+8, font);
			gc.drawPath(path);
			path.dispose();
		}else{
			Path path=new Path(null);
			path.addString(index+"", 30f, start+8, font);
			path.addString(name, 50f, start+8, font);
			gc.drawPath(path);
			path.dispose();
			
		}
		if(hq){
			Font hqf=SWTResourceManager.getFont("宋体", 10, SWT.NORMAL);
			Color bk=gc.getBackground();
			Color fo=gc.getForeground();
			Color inner=SWTResourceManager.getColor(0X66,0XCD,0XAA);
			Color outer=SWTResourceManager.getColor(0XAF,0XEE,0XEE);
			gc.setBackground(inner);
			gc.setForeground(outer);
			Path hqp=new Path(null);
			hqp.addString("HQ", width-MyList.BAR_WIDTH-40, start+8, hqf);
			gc.fillPath(hqp);
			gc.drawPath(hqp);
			hqp.dispose();
			gc.setBackground(bk);
			gc.setForeground(fo);
		}
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight,int index) {
		if(e.button==3){
			Menu m=new Menu(getParent());
			Menu menu=getParent().getMenu();
			if (menu != null) {
				menu.dispose();
			}
			MenuItem miPlay=new MenuItem(m, SWT.NONE);
			miPlay.setText("播放");
			miPlay.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					getParent().select(SongItem.this,false);
				}
				
			});
			
			MenuItem miSearch=new MenuItem(m, SWT.NONE);
			miSearch.setText("搜索歌词");
			miSearch.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					BaseBox bb=new BaseBox(getParent().getShell(), SWT.NO_TRIM);
					bb.getShell().setSize(300, 130);
					SWTTools.centerWindow(bb.getShell());
					SearchComp sc=new SearchComp(bb.getShell(), SWT.NONE,property.get("name"));
					bb.add(sc);
					Object result=bb.open(0, 0);
					if(null!=result){
						String name=result.toString();
						List<SearchInfo>lrcs=SongSeacher.getLrcFromKuwo(name);
						BaseBox bbox=new BaseBox(getParent().getShell(), SWT.NO_TRIM);
						bbox.getShell().setSize(475,330);
						SWTTools.centerWindow(bbox.getShell());
						SearchResultComp src=new SearchResultComp(bbox.getShell(), SWT.NONE);
						src.setData(lrcs);
						src.setPath(property.get("path"));
						bbox.add(src);
						bbox.open(0, 0);
					}
				}
			});
			
			MenuItem mOpen = new MenuItem(m, SWT.NONE);
			mOpen.setText("打开文件所在文件夹");
			mOpen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e1) {
					try {
						Runtime.getRuntime().exec(
								"rundll32 SHELL32.DLL,ShellExec_RunDLL "
										+ "Explorer.exe /select,"
										+ getProperty().get("path")
												);
					} catch (IOException e) {
						MessageBox mb = new MessageBox(getParent().getShell(), SWT.NONE);
						mb.setText("错误");
						mb.setMessage("文件不存在，建议删除歌曲。");
						mb.open();
					}
					
				}
			});
			
			MenuItem miDel = new MenuItem(m, SWT.NONE);
			miDel.setText("删除");
			miDel.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					getParent().removeItem(SongItem.this);
				}
				
			});
			getParent().setMenu(m);
			m.setVisible(true);
		}
		return true;
	}


	public Map<String,String> getProperty() {
		return Collections.unmodifiableMap(property);
	}

	public Image getHead() {
		return head;
	}

	public void setHead(Image head) {
		this.head = head;
	}

}
