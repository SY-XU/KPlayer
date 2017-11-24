package com.xk.player.tools;

import java.util.List;
import java.util.Map;

import com.xk.player.lrc.XRCLine;
import com.xk.player.tools.SongSeacher.SearchInfo;

public interface IDownloadSource {

	public List<SearchInfo> getLrc(String name);
	
	public List<SearchInfo> getMV(String name);
	
	public List<SearchInfo> getSong(String name);
	
	public List<SearchInfo> getSong(String name, String type);
	
	public Map<String, String> fastSearch(String name);
	
	public String getArtist(String name);
	
	public SongLocation getInputStream(String url);
	
	public List<XRCLine> parse(String content);
	
	
	
}
