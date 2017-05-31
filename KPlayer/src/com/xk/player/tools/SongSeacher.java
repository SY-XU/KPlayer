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
	
	
	public static List<SearchInfo> getMVFromKugou(String name) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		String searchUrl = null;
		String html = null;
		String callBack = "jQuery19108035724928824395_" + System.currentTimeMillis();
		try {
			searchUrl = "http://mvsearch.kugou.com/mv_search";
			Map<String, String> params = new HashMap<String, String>();
			params.put("callback", callBack);
			params.put("keyword", name);
			params.put("page", "1");
			params.put("pagesize", "30");
			params.put("userid", "-1");
			params.put("clientver", "");
			params.put("platform", "WebFilter");
			params.put("tag", "em");
			params.put("filter", "2");
			params.put("iscorrection", "1");
			params.put("privilege_filter", "0");
			params.put("_", String.valueOf(System.currentTimeMillis()));
			html = HTTPUtil.getInstance("search").getHtml(searchUrl, params);
		} catch (Exception e) {
			return songs;
		}
		if(!StringUtil.isBlank(html)){
			String json = html.substring(callBack.length() + 1, html.length() - 1 );
			Map<String, Object> rst = JSONUtil.fromJson(json);
			Map<String, Object> data = (Map<String, Object>) rst.get("data");
			List<Map<String, Object>> lists = (List<Map<String, Object>>) data.get("lists");
			for(Map<String, Object> minfo : lists) {
				SearchInfo info = new SearchInfo(){

					@Override
					public String getUrl() {
						if(this.urlFound) {
							return url;
						}
						String md5 = ByteUtil.MD5(this.url + "kugoumvcloud");
						String url = "http://trackermv.kugou.com/interface/index/cmd=100&hash=" + this.url + "&key=" + md5 + "&pid=6&ext=mp4&ismp3=0";
						String rst = HTTPUtil.getInstance("test").getHtml(url);
						Map<String, Object> map = JSONUtil.fromJson(rst);
						this.urlFound = true;
						this.url = (String)((Map<String , Map<String, Object>>)map.get("mvdata")).get("sd").get("downurl");
						return this.url;
					}
					
				};
				songs.add(info);
				info.type= "mv";
				info.album = "";
				info.singer = (String) minfo.get("SingerName");
				info.url = (String) minfo.get("MvHash");
				info.name = ((String) minfo.get("MvName")).replace("<em>", "").replace("</em>", "");
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
						SearchInfo info = new SearchInfo(){

							@Override
							public String getUrl() {
								if(this.urlFound) {
									return this.url;
								}
								String mp4Url = "http://www.kuwo.cn/yy/st/mvurl?rid=MUSIC_" + this.url;
								this.urlFound = true;
								this.url = HTTPUtil.getInstance("search").getHtml(mp4Url);
								return this.url;
							}
							
						};
						
						songs.add(info);
						info.type= "mv";
						info.album = "";
						Elements as = li.select("a[class=img]");
						for(Element a : as) {
							info.name = a.attr("title");
							String href = a.attr("href");
							String songId = href.replace("http://www.kuwo.cn/mv/", "").replace("/", "");
							info.url = songId;
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
	public static void main(String[] args) {
		getMVFromKugou("凉凉");
	}
	
	public static class SearchInfo{
		boolean urlFound = false;
		String url="";
		public String name="";
		public String singer="";
		public String album="";
		public String type = "mp3";
		public String getUrl() {
			return url;
		}
	}
	
}
