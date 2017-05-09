package com.xk.player.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.xk.player.ole.flash.Flash;
import com.xk.player.ole.flash.listener.FlashEventListener;
import com.xk.player.ole.mediaplayer.MediaPlayer;
import com.xk.player.ole.mediaplayer.listener.MediaPlayerEventAdapter;

public class TestWmp {
	
	
	protected Shell shell;
	
	public static void main(String[] args) {
		TestWmp test = new TestWmp();
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
		shell.setText("Docment");
		shell.setLayout(new FillLayout());
		MediaPlayer mp = new MediaPlayer(shell, SWT.NO_BACKGROUND, new MediaPlayerEventAdapter(){
			
		});
		mp.play("http://win.web.ri03.sycdn.kuwo.cn/77ca3b17a7b95d038917da57d01f600d/590ca491/resource/m1/49/9/2028373139.mp4");
//		mp.setControlBarVisible(false);
	}
}
