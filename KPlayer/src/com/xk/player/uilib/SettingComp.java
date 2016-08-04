package com.xk.player.uilib;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.player.tools.SWTTools;

public class SettingComp extends Composite implements ICallable{

	private MyList left;
	private ICallback callback;
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SettingComp(Composite parent, int style) {
		super(parent, style);
		setBackgroundMode(SWT.INHERIT_FORCE);
		setBackgroundImage(parent.getParent().getBackgroundImage());
		
		Label textLabel = new Label(this, SWT.NONE);
		textLabel.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		textLabel.setBackground(SWTResourceManager.getColor(0, 153, 255));
		textLabel.setBounds(0, 0, 450, 24);
		textLabel.setText("   基本设置");
		SWTTools.enableTrag(textLabel);
		
		left=new MyList(this, 100, 400);
		left.setBounds(10, 33, 100, 247);
		left.setSimpleSelect(true);
		left.setMask(55);
		
		
		Image img=SWTResourceManager.getImage(getClass(), "/images/lrcsetting.png");
		SettingItem song=new SettingItem(img, "歌词设置");
		left.addItem(song);
		
		Image down=SWTResourceManager.getImage(getClass(), "/images/download.png");
		SettingItem download=new SettingItem(down, "下载设置");
		left.addItem(download);
		
		Composite content = new Composite(this, SWT.NONE);
		content.setBounds(116, 33, 324, 247);
		
		Label okBtn = new Label(this, SWT.NONE);
		okBtn.setBackground(SWTResourceManager.getColor(0, 153, 255));
		okBtn.setAlignment(SWT.CENTER);
		okBtn.setBounds(280, 297, 61, 17);
		okBtn.setText("确定");
		okBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if(null!=callback){
					callback.callback(null);
				}
			}
		});
		
		Label noBtn = new Label(this, SWT.NONE);
		noBtn.setBackground(SWTResourceManager.getColor(0, 153, 255));
		noBtn.setAlignment(SWT.CENTER);
		noBtn.setBounds(365, 297, 61, 17);
		noBtn.setText("取消");
		noBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if(null!=callback){
					callback.callback(null);
				}
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void setCallBack(ICallback callBack) {
		this.callback=callBack;
		
	}
}
