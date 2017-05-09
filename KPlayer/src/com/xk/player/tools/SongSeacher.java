/**
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                 佛祖保佑                                      永无BUG
 * @author xiaokui
 * @版本 ：v1.0
 * @时间：2016-5-2上午10:44:33
 */
package com.xk.player.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @项目名称：MusicParser
 * @类名称：SongSeacher.java
 * @类描述：
 * @创建人：xiaokui
 * 时间：2016-5-2上午10:44:33
 */
public class SongSeacher {

	
	public static String fromCharCodes(String[]codes){
		if(null==codes){
			return "";
		}
		StringBuilder builder=new StringBuilder();
		for(String code:codes){
			int intValue=Integer.parseInt(code);
			char chr=(char) intValue;
			builder.append(chr);
		}
		return builder.toString();
	}
	
	public static String getArtistFromKuwo(String name){
		String searchUrl=null;
		try {
			searchUrl="http://sou.kuwo.cn/ws/NSearch?type=artist&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements texts=doc.getElementsByAttribute("lazy_src");
			for(Element ele:texts){
				String alt=ele.attr("alt");
				if(null!=alt&&alt.contains(name.replace(" ", "&nbsp;"))){
					return ele.attr("lazy_src");
				}
			}
		}
		return null;
	}
	
	public static List<SearchInfo> getLrcFromKuwo(String name){
		List<SearchInfo> lrcs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return lrcs;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info=new SearchInfo();
				lrcs.add(info);
				Elements names=ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP:names){
					Elements as=nameP.getElementsByTag("a");
					for(Element a:as){
						info.name=a.attr("title");
						info.url=a.attr("href");
						break;
					}
					break;
				}
				Elements singers=ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as=singer.getElementsByTag("a");
					for(Element a:as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
			}
		}
		return lrcs;
		
	}
	
	public static LrcInfo perseFromHTML(String html){
		Document doc=Jsoup.parse(html);
		Elements lrcs=doc.select("p[class=lrcItem]");
		LrcInfo lrc=new LrcInfo();
		Map<Long,String> infos=new HashMap<Long, String>();
		for(Element ele:lrcs){
			String time=ele.attr("data-time");
			double dtime=Double.parseDouble(time);
			long ltime=(long) (dtime*1000);
			String text=ele.text();
			infos.put(ltime, text);
		}
		lrc.setInfos(infos);
		return lrc;
	}
	
	/**
	 * 用途：快速搜索
	 * @date 2016年11月18日
	 * @param name
	 * @return
	 */
	public static Map<String, String> fastSearch(String name) {
		if(StringUtil.isBlank(name)) {
			return Collections.emptyMap();
		}
		try {
			name = URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = "http://search.kuwo.cn/r.s?SONGNAME=" + name + "&ft=music&rformat=json&encoding=utf8&rn=8&callback=song&vipver=MUSIC_8.0.3.1&_=" + System.currentTimeMillis();
		String html=HTTPUtil.getInstance("search").getHtml(url);
		if(StringUtil.isBlank(html)) {
			return Collections.emptyMap();
		}
		html = html.replace(";song(jsondata);}catch(e){jsonError(e)}", "").replace("try {var jsondata =", "");
		Map<String, Object> rst = JSONUtil.fromJson(html);
		if(null == rst || rst.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Map<String, String>> list = (List<Map<String, String>>) rst.get("abslist");
		Map<String, String> result = new HashMap<String, String>();
		for(Map<String, String> map : list) {
			result.put(map.get("NAME"), map.get("SONGNAME"));
		}
		return result;
	}
	
	
	/**
	 * 用途：默认歌曲搜索
	 * @date 2016年11月18日
	 * @param name
	 * @return
	 */
	public static List<SearchInfo> getSongFromKuwo(String name){
		return getSongFromKuwo(name, "mp3");
	}
	
	
	/**
	 * 用途：指定类型搜索音乐
	 * @date 2016年11月18日
	 * @param name
	 * @param type
	 * @return
	 */
	public static List<SearchInfo> getSongFromKuwo(String name, String type){
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info=new SearchInfo();
				info.type=type;
				songs.add(info);
				Elements names=ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP:names){
					Elements as=nameP.getElementsByTag("a");
					for(Element a:as){
						info.name=a.attr("title");
						break;
					}
					break;
				}
				Elements albums=ele.getElementsByAttributeValue("class", "a_name");
				for(Element albumP:albums){
					Elements as=albumP.getElementsByTag("a");
					for(Element a:as){
						info.album=a.attr("title");
						break;
					}
					break;
				}
				Elements singers=ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as=singer.getElementsByTag("a");
					for(Element a:as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
				Elements numbers=ele.getElementsByAttributeValue("class", "number");
				String download="response=url&type=convert%5Furl&rid=MUSIC%5F{mid}&format="+type;
				String baseHost="http://antiserver.kuwo.cn/anti.s?";
				for(Element number:numbers){
					Elements as=number.getElementsByTag("input");
					for(Element a:as){
						String mid=a.attr("mid");
						String url = baseHost+download.replace("{mid}", mid);
						info.url=url;
						break;
					}
					break;
				}
			}
		}
		
		return songs;
	}
	
	/**
	 * 酷我搜索mv
	 * @param name
	 * @return
	 */
	public static List<SearchInfo> getMVFromKuwo(String name) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=mv&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("div[class=mvalbum]");
			for(Element ele : eles) {
				Elements uls = ele.select("ul[class=clearfix]");
				for(Element ul : uls) {
					Elements lis = ul.getElementsByTag("li");
					for(Element li : lis) {
						SearchInfo info = new SearchInfo();
						songs.add(info);
						info.type= "mv";
						info.album = "";
						Elements as = li.select("a[class=img]");
						for(Element a : as) {
							info.name = a.attr("title");
							String href = a.attr("href");
							String songId = href.replace("http://www.kuwo.cn/mv/", "").replace("/", "");
							String mp4Url = "http://www.kuwo.cn/yy/st/mvurl?rid=MUSIC_" + songId;
							info.url = HTTPUtil.getInstance("search").getHtml(mp4Url);
							break;
						}
						Elements ps = li.select("p[class=singerName]");
						for(Element p : ps) {
							Elements pas = p.getElementsByTag("a");
							for(Element pa : pas) {
								info.singer = pa.attr("title");
							}
							break;
						}
						
					}
				}
			}
		}
		return songs;
		
	}
	
	
	public static class SearchInfo{
		public String url="";
		public String name="";
		public String singer="";
		public String album="";
		public String type = "mp3";
	}
	
}
