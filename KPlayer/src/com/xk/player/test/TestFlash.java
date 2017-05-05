package com.xk.player.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.xk.player.ole.flash.Flash;
import com.xk.player.ole.flash.listener.FlashEventListener;

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
		shell.setText("Docment");
		shell.setLayout(new FillLayout());
		Flash flash = new Flash(shell, SWT.NO_BACKGROUND, new FlashEventListener() {
			
			@Override
			public void onReadyStateChange(int newState) {
				System.out.println("newState" + newState);
				
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
		flash.setBackgroundColor(SWT.COLOR_BLACK);
		flash.loadMovie(0, "https://p.bokecc.com/flash/player.swf?vid=B94C8EEBA792AB1B9C33DC5901307461&siteid=2745FC107AA7B1F3&playerid=1245942C135EBBD6&playertype=1&autoStart=true");
		flash.setMenuEnable(false);
	}
}
