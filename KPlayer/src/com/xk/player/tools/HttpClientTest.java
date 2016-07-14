package com.xk.player.tools;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpClientTest {
	
	
	public static Map<String,String> getHtml(String content) {
		if(null==content){
			return null;
		}
		Document doc=Jsoup.parse(content);
		Elements links=doc.select("ul[class=searchResult]");
		if(null!=links&&links.size()==1){
			Map<String,String>songs=new HashMap<String,String>();
			links=links.get(0).getElementsByAttributeValue("target", "_blank");
			for(int i=0;i<links.size();i++){
				Element ele=links.get(i);
				String href=ele.attr("href");
				if(href!=null&&href.startsWith("/play/")){
					if(!songs.containsKey(href)){
						songs.put(href, ele.text());
					}
				}
			}
			return songs;
		}
		
		return null;
	
	}

	public static Elements getScripts(String content){
		if(null==content){
			return null;
		}
		Document doc=Jsoup.parse(content);
		Elements scripts=doc.select("script:not([src])");
		return scripts;
	}
	
	private static String GetCharset(Elements charsets) {
		String charset="";
		for(Element sets:charsets){
			if(sets.attr("content").indexOf("charset=")>=0){
				String[] chars=sets.attr("content").split(";");
				if(chars!=null&&chars.length==2){
					charset=chars[1].trim();
					charset=charset.substring(charset.indexOf("=")+1, charset.length()).trim();
					return charset;
				}
			}
			if(sets.attr("charset")!=null){
				return sets.attr("charset");
			}
		}
		return charset;
	}

}
