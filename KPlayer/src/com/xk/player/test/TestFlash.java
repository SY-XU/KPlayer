package com.xk.player.test;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.xk.player.ole.flash.Flash;
import com.xk.player.ole.flash.listener.FlashEventListener;
import com.xk.player.tools.ByteUtil;
import com.xk.player.tools.HTTPUtil;
import com.xk.player.tools.JSONUtil;

public class TestFlash {
	
	
	protected Shell shell;
	
	public static void main(String[] args) {
		TestFlash test = new TestFlash();
		test.open();
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(966, 575);
		shell.setText("Flash");
		
		StackLayout layout = new StackLayout();
//		shell.setLayout(new FillLayout());
		shell.setLayout(layout);
		StackLayout layout1 = new StackLayout();
		
		Composite comp = new Composite(shell, SWT.EMBEDDED|SWT.BORDER);
		comp.setLayout(layout1);
		layout.topControl = comp;
		Flash flash = new Flash(comp, SWT.NO_BACKGROUND, new FlashEventListener() {
			
			@Override
			public void onReadyStateChange(int newState) {
//				System.out.println("newState" + newState);
				
			}
			
			@Override
			public void onProgress(int percentDone) {
				System.out.println("percentDone = " + percentDone);
				
			}
			
			@Override
			public void onFSCommand(String command, String args) {
				System.out.println(command + "  " + args);
				
			}
		});
		layout1.topControl = flash.getOleFrame();
		String hash = "869270221B2FEDCDF5BB75016C692AF3";
		String md5 = ByteUtil.MD5(hash + "kugoumvcloud");
		String url = "http://trackermv.kugou.com/interface/index/cmd=100&hash=" + hash + "&key=" + md5 + "&pid=6&ext=mp4&ismp3=0";
		String rst = HTTPUtil.getInstance("test").getHtml(url);
		Map<String, Object> map = JSONUtil.fromJson(rst);
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("skinurl", "http://static.kgimg.com/common/swf/video/skin.swf");
		vars.put("aspect", "true");
		vars.put("autoplay", "true");
		vars.put("fullscreen", "true");
		vars.put("initfun", "flashinit");
		vars.put("url", (String)((Map<String , Map<String, Object>>)map.get("mvdata")).get("sd").get("downurl"));
		String varsStr = "";
		for(String key : vars.keySet()) {
			varsStr += key + "=" + vars.get(key) + "&";
		}
		flash.setFlashVars(varsStr);
		flash.setQuality2("high");
		flash.setBGColor("#666666");
		flash.setWMode("Transparent");
		flash.loadMovie(0, "http://static.kgimg.com/common/swf/video/videoPlayer.swf?20141014061415");
	}
}
