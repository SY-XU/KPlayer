package com.xk.player.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Config {

	public Integer BGTYPE=0;//背景图片类型，默认
	public String BGPATH="/images/bg1.jpg";
	
	public int PLAY_MODEL=0;//0,顺序播放，1，随机播放，2，单曲循环
	
	public long maxVolume=100;//最大音量
	public long defaultVolume=15;//当前音量
	
	//歌词背景色
	public int br=253;
	public int bg=254;
	public int bb=255;
	//歌词进度色
	public int cr=22;
	public int cg=136;
	public int cb=227;
	
	//歌词字体
	public String fontName="楷体";
	public int fontStyle=SWT.NORMAL;
	
	//下载路径
	public String downloadPath="e:/download";
	public String lrcPath="e:/download";
	
	//播放列表
	public List<String> songList=new ArrayList<>();
	public List<String> favoriteList=new ArrayList<>();

	@JsonIgnore
	public Map<String,Map<String,String>> maps=new HashMap<String,Map<String,String>>();
	
	private static Config instance;
	public static Config getInstance(){
		if(null==instance){
			File file=new File("config.jc");
			if(file.exists()){
				if(file.isFile()){
					String result=FileUtils.readString(file.getAbsolutePath());
					instance=JSONUtil.toBean(result, Config.class);
					return instance;
				}else{
					file.delete();
				}
			}
			instance=new Config();
		}
		return instance;
	}
	
	private Config(){
	}
	
	public void save(){
		File file=new File("config.jc");
		if(file.exists()){
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result=JSONUtil.toJson(this);
		FileUtils.writeString(result, file);
	}
}
