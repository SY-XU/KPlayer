package com.xk.player.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class SWTTools {

	public static void enableTrag(final Composite composite) {
		Listener listener = new Listener() {
		    int startX, startY;
		    public void handleEvent(Event e) {
		        if (e.type == SWT.MouseDown && e.button == 1) {
		            startX = e.x;
		            startY = e.y;
		        }
		        if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) != 0) {
		            Point p = composite.toDisplay(e.x, e.y);
		            p.x -= startX;
		            p.y -= startY;
		            composite.setLocation(p);
		        }
		    }
		};
		composite.addListener(SWT.MouseDown, listener);
		composite.addListener(SWT.MouseMove, listener);
		
	}
	
	public static void centerWindow(Shell shell){
		Rectangle rect=Display.getDefault().getClientArea();
		int x=rect.width/2-shell.getSize().x/2;
		int y=rect.height/2-shell.getSize().y/2;
		shell.setLocation(x,y);
	}
}
