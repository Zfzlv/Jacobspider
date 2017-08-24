package com.wenba.resolve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wenba.util.LingoesLd2Reader;
import com.wenba.util.Util;
import com.wenba.util.WhitelistUtil;
import com.wenba.util.FullCharConverter;

public class Dictcn {
	
	public static Map<String,Object> resolvefile(Document doc) throws IOException{
		Map<String, Object> dicts = new HashMap<String, Object>();
		//File file = new File(arg0);
		//Document doc = Jsoup.parse(file, "utf-8");
        String outerHtml = doc.outerHtml();
        if(outerHtml.contains("ifufind"))return null;
        outerHtml = FullCharConverter.full2HalfChange(outerHtml);//System.out.println(outerHtml);
        for (String x : zhuanmabiao.keySet()) {
            outerHtml = outerHtml.replace(Jsoup.parse(x).text(), zhuanmabiao.get(x));
        }
        //String clean = Jsoup.clean(outerHtml, WhitelistUtil.relaxed);//.replace("&nbsp;", " ");
        //System.out.println(outerHtml);
        doc = Jsoup.parse(outerHtml);    
        String title = doc.getElementsByClass("keyword").first().text();
        dicts.put("title", title);
        if(doc.getElementsByClass("word-cont").first()!=null){
        String a = doc.getElementsByClass("word-cont").first().getAllElements().last().toString();
        String b = a.replaceAll("[\\s\\S]*?level_(\\d+).*", "$1");
        int level = 0;
        if(a!=b)level = Integer.parseInt(b);
        dicts.put("level", level);
        }else dicts.put("level", "");
        if(doc.getElementsByClass("dict-basic-ul").first()!=null){
        String meaning = doc.getElementsByClass("dict-basic-ul").first().toString().replaceAll("<li\\s*style[^>]*>[\\s\\S]*?<\\s*/li>", "");
        dicts.put("meaning", meaning);
        }else if(doc.getElementsByClass("clearfix").first()!=null){
        	String meaning = doc.getElementsByClass("clearfix").first().toString().replaceAll("^[\\s\\S]*?(<ul>[\\s\\S]*?</ul>)[\\s\\S]*?$", "$1").replaceAll("<li\\s*style[^>]*>[\\s\\S]*?<\\s*/li>","");;
            //System.out.println(meaning);
        	dicts.put("meaning", meaning);
        }else dicts.put("meaning", "");
        if(doc.getElementsByClass("dict-chart").first()!=null){
        String abc = doc.getElementsByClass("dict-chart").first().toString().replaceAll("[\\s\\S]*?data\\s*=\\s*\"([^\"]*?)\"[\\s\\S]*","$1"); 
        String chart = Util.decodeUnicode(java.net.URLDecoder.decode(abc,"utf-8"));
        dicts.put("chart", chart);
        }else{
        	dicts.put("chart", "");
        }
        //System.out.println(chart);
        //String xxx = Util.chinaToUnicode(hint);
        Elements sections  = doc.getElementsByClass("section");
        Map<String, String> waittojson = new HashMap<String, String>();
        String tabtext = "";
        for(Element x : sections){
        	/*Matcher m1 = Pattern.compile("<h3>([\\s\\S](?!<h3>))*").matcher(x.toString());
        	if(m1.find()){
        		String h = m1.group();
        	}*/
        	//System.out.println(x.toString());
        	Elements y = x.getElementsByTag("h3");
        	Elements z = x.getElementsByClass("layout");
        	for(int i = 0;i<y.size();i++){
        		String xyz = y.get(i).text();
        		tabtext += xyz+",";
        		String hjk = z.get(i).toString();
        		if(!"提问补充".equals(xyz))
        		waittojson.put(xyz,hjk);
        		//System.out.println(xyz);System.out.println(hjk);
        		
        	}
        }
        tabtext = tabtext.replaceAll(",$", "");
        tabtext = tabtext.replaceAll("提问补充,?", "");
        dicts.put("tabtext", tabtext);
        //System.out.println(tabtext);
        JSONObject layouts = JSONObject.fromObject(waittojson); 
        //String layouts = JSONArray.fromObject(waittojson).toString();      
        //String layouts = Util.simpleMapToJsonStr(waittojson);
        //System.out.println(layouts);
        dicts.put("layouts", layouts);
        return dicts;
        
	}
	
	 /**
     * 转码表，将一些特殊的字符转换成为可见的字符。
     *
     * 将一些中文的符号转换为英文符号
     */
    static Map<String, String> zhuanmabiao = new LinkedHashMap<String, String>() {
        {
            put("", "<=");//小于等于
            put("", "!=");//不等于
            put("", ">=");//大于等于
            put("", "<");//小于
            put("", ">");//大于
            put("", "=");//等于
            put("&nbsp;", " ");//空格
        }
    };
    
    /**
     * 拼接sql
     * 
     */
    public static String mark_sql(String sql,Map<String,Object> dict,String point){
    	sql += "(\""+Util.addcslashes(dict.get("title"))+"\","+dict.get("level")+",\""+Util.addcslashes(dict.get("meaning"))
    			+"\",\""+Util.addcslashes(dict.get("chart"))+"\",\""+Util.addcslashes(dict.get("layouts"))+"\",\""+Util.addcslashes(dict.get("tabtext"))+"\")"+point;
    	return sql;
    }
	
	/**
	 * 主执行方法
	 * @author jie.li
	 * */
    public static void main(String[] args) {
        try {
            new Dictcn().execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void execute() throws InterruptedException {
    	System.out.println("-start--");
    	/*
        Thread[] threads = new Thread[3];
        for (int x = 0; x < threads.length; x++) {
            threads[x] = new Thread(new Splidedict());
            threads[x].start();
        }
 
        for (int x = 0; x < threads.length; x++) {
            threads[x].join();
        }*/
    	Thread threads = new Thread(new Splidedict());
    	threads.start();       
        System.out.println("end");
    }
    
    static class Splidedict implements Runnable {
		// TODO Auto-generated method stub
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int sql_length = 1000; //sql拼接的数据数
			String origin_sql = "insert into dict(title,level,meaning,chart,layouts,tabtext) values ";
			String insertsql = "";
			int i = 0; //sql拼接计数器
			int max = 1;
			String url = "";
			List<String> front = new ArrayList<String>();
			front.add("");
			List<String> newfront = new ArrayList<String>();
			try{
			Connection con = null; // 定义一个MYSQL链接对象
			PreparedStatement ps = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance(); // MYSQL驱动
			con = DriverManager.getConnection(
							"jdbc:mysql://10.88.0.235/lijie?characterEncoding=utf-8",
							"root", "tikuserverdb");
			Statement stmt; // 创建声明
			stmt = con.createStatement();
			BufferedReader reader = null;
			
			//System.setProperty("http.proxyHost", "127.0.0.1");
			//System.setProperty("http.proxyPort", "8182");
			try {
				//while(max <= 182){
				FileInputStream fileInputStream = new FileInputStream(new LingoesLd2Reader().getClass().getResource("ViconEnglishDictionary.ld2.words").getPath());
				InputStreamReader inputStreamReader = new InputStreamReader(
						fileInputStream, "UTF-8");
				reader = new BufferedReader(inputStreamReader);
				String tempString = null;
				//for(Iterator old = front.iterator();old.hasNext();){
				while ((tempString = reader.readLine()) != null) {
				   //String xueba = (String) old.next();
						//for(int c = 1;c<=26;c++){
							//char n = (char)(c+96);
							//String now = xueba + n;
					 String now = tempString;
							System.out.println(now);
							/*String sql = "insert into tmp values(\""+now+"\","+max+");";
							ps = con.prepareStatement(sql);
							ps.executeUpdate();*/
							//newfront.add(now);
							now = now.replaceAll("/", "_2F");
							now = now.replaceAll("\\.", "_2E");
							now = URLEncoder.encode(now);
							now = now.replaceAll("\\+", "%20");
							url = "http://dict.cn/"+now;
							//System.out.println(url);
							long start = System.currentTimeMillis(); 
							try{
							if(Jsoup.connect(url)
								.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36")
								.timeout(10000)
								.execute().statusCode()==200){
							Document doc = 
							    Jsoup.connect(url)
							.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36")
							.timeout(10000)
							.get();
							System.out.println("Time is:"+(System.currentTimeMillis()-start) + "ms"); 
							Map<String,Object> dicts = resolvefile(doc);
							if(dicts!=null){
							 if(i == 0)insertsql = origin_sql;
								 if(i < sql_length){
								 insertsql = mark_sql(insertsql,dicts,",");
								     i  = i + 1;//System.out.println(insertsql);
								  }else if(i >= sql_length){
								   insertsql = mark_sql(insertsql,dicts,";");
								   Util.writetoFile("D:\\devAll\\newdict.sql",insertsql);
								    i = 0;
								  }
								}
							/*try{
								Thread.sleep(3000);
							}catch(InterruptedException e){
								System.out.println("Thread interrupted...");
							}*/
						//}
					}
					}catch(IOException e){
						e.printStackTrace();
						continue;
					}
				}
				if(insertsql != origin_sql){
					insertsql = insertsql.replaceAll(",$", ";");
					Util.writetoFile("D:\\devAll\\newdict.sql",insertsql);
				}
				reader.close();
				/*
					String sql = "delete from tmp where num <"+max;
					ps = con.prepareStatement(sql);
					ps.executeUpdate();
					sql = "select min(id) as start,max(id) as end from tmp";
					ResultSet selectRes = stmt.executeQuery(sql);
					while (selectRes.next()) {
						
					}*/
					//front = new ArrayList<String>();
					//front =  newfront;
					//newfront = new ArrayList<String>();
					//max++;
					//System.out.println("-----------------------"+max);
				//}
			    //ps = con.prepareStatement(insertsql);
			    //ps.setString(1, "呼呼");
				//ps.executeUpdate();
					/*例子
					 * Document doc = Jsoup.connect("http://www.oschina.net/") 
						.data("query", "Java")   // 请求参数
	  					.userAgent("I ’ m jsoup") // 设置 User-Agent 
	  					.cookie("auth", "token") // 设置 cookie 
	  					.timeout(3000)           // 设置连接超时时间
	  					.post();                 // 使用 POST 方法访问 URL 
					 * */
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			} catch (Exception e) {
				System.out.print("MYSQL ERROR:" + e.getMessage());
				e.printStackTrace();
			  }
		}
	}
	
}
