package com.xk.player.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.xk.player.tools.Config;
import com.xk.player.tools.HTTPUtil;
import com.xk.player.tools.SongLocation;
import com.xk.player.tools.SongSeacher.SearchInfo;

public class SongSearchItem extends LTableItem {

	public SongSearchItem(SearchInfo info) {
		super(info);
	}

	@Override
	protected void download() {
		if(downloading){
			return;
		}
		downloading=true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Config conf=Config.getInstance();
				String url=info.url;
				String realUrl=HTTPUtil.getInstance("player").getHtml(url);
				SongLocation loc=HTTPUtil.getInstance("player").getInputStream(realUrl);
				String parent=conf.downloadPath;
				if(null==parent||parent.trim().isEmpty()){
					parent="e:/download";
					conf.downloadPath=parent;
					conf.lrcPath=parent;
				}
				File file=new File(conf.downloadPath,info.singer+" - "+info.name+"."+info.type);
				if(!file.exists()){
					FileOutputStream out=null;
					try {
						file.createNewFile();
						out=new FileOutputStream(file);
						long all=0;
						byte[]buf=new byte[20480];
						int len=0;
						while((len=loc.input.read(buf, 0, buf.length))>=0){
							all+=len;
							double per=(double)all/loc.length*100;
							if(per-persent>1||per>=100){
								persent=(int) (per);
								flush();
							}
							out.write(buf, 0, len);
							out.flush();
						}
					} catch (Exception e) {
						System.out.println("download failed!"+e.getMessage());
					}finally{
						if(null!=out){
							try {
								out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				try {
					loc.input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PlayUI.getInstance().addFile(file.getAbsolutePath(),false);
				persent=0;
				downloading=false;
				flush();
				
			}
		}).start();

	}

}
