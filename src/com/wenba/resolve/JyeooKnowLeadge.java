package com.wenba.resolve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class JyeooKnowLeadge {
	
	public static Map<String,Object> resolvefile(Document doc) throws IOException{
		Map<String, Object> knowledgepoint = new HashMap<String, Object>();
		//File file = new File(arg0);
		//Document doc = Jsoup.parse(file, "utf-8");
        String outerHtml = doc.outerHtml();
        if(outerHtml.contains("erros"))return null;
        outerHtml = FullCharConverter.full2HalfChange(outerHtml);//System.out.println(outerHtml);
        for (String x : zhuanmabiao.keySet()) {
            outerHtml = outerHtml.replace(Jsoup.parse(x).text(), zhuanmabiao.get(x));
        }
        //String clean = Jsoup.clean(outerHtml, WhitelistUtil.relaxed);//.replace("&nbsp;", " ");
        //System.out.println(outerHtml);
        doc = Jsoup.parse(outerHtml);    
        String points = doc.getElementsByTag("b").first().text();
        knowledgepoint.put("title", points);
        String knowleadge = doc.getElementsByClass("point-card-body").first().toString();
        knowledgepoint.put("detail", knowleadge);
        
        //knowleadge = knowleadge.replaceAll("<\\s*div[^>]*point-card-body[^>]*>\\s*", "");
        //knowleadge = knowleadge.replaceAll("\\s*<\\s*/\\s*div[^>]*>$", "");
        /*Map<String, String> meaning = new HashMap<String, String>();    
        if(knowleadge.contains("onmouseover")){
        	Matcher m1 = Pattern.compile("<[^>]*onmouseover\\s*=[^>]*>[\\s\\S]*?</[^>]*>").matcher(knowleadge);*/
        	//knowleadge = knowleadge.replaceAll("<\\s*div[^>]*>[\\s\\S]*?<[^>]*onmouseover\\s*=[^>]*>[\\s\\S]*?<\\s*/\\s*div[^>]*>\\s*", "");
        	//knowleadge = knowleadge.replaceAll("^<\\s*div[^>]*>\\s*","");
        	//knowleadge = knowleadge.replaceAll("\\s*<\\s*/\\s*div[^>]*>$","");
        	/*List<String> abc = new ArrayList<String>();
        	while (m1.find()) {
        		String text = m1.group();
        		text = text.replaceAll("\\s*<[^>]*>\\s*", "");
        		abc.add(text);
        	}
        	Document tmp = Jsoup.parse(knowleadge);
        	Elements xyz = tmp.getElementsByTag("div");
        	for(int i = 0;i<xyz.size();i++){
        		meaning.put(abc.get(i).toString(), xyz.get(i).toString());
        	}       	
        }else{
        	Matcher m1 = Pattern.compile("<[^>]*>\\s*【[\\s\\S]*?】").matcher(knowleadge);
        	if(m1.find()){
        		knowleadge = knowleadge.replaceAll("^[\\s\\S]*?(<[^>]*>\\s*【[\\s\\S]*?】)","$1");*/
        		//knowleadge = knowleadge.replaceAll("\\s*<\\s*/\\s*div[^>]*>$","");
        		//System.out.println(knowleadge);
        		/*Matcher m2 = Pattern.compile("<[^>]*>\\s*【[\\s\\S]*?】[\\s\\S]*?").matcher(knowleadge);
        		while(m2.find()){
        			String abc = m2.group();
        			String tmp = knowleadge.replaceAll("^(<[^>]*>\\s*【[\\s\\S]*?】[\\s\\S]*?)<[^>]*>\\s*【[\\s\\S]*?】[\\s\\S]*$","$1");
        			tmp = tmp.replaceAll("(<\\s*br[^>]*>)?\\s*【[\\s\\S]*?】","");
        			knowleadge = knowleadge.replaceAll("^<[^>]*>\\s*【[\\s\\S]*?】[\\s\\S]*?(<[^>]*>\\s*【[\\s\\S]*?】[\\s\\S]*$)","$1");
        			String text = abc.replaceAll("<[^>]*>\\s*","");
        			meaning.put(text, tmp);
        			System.out.println("++"+text);
        			System.out.println("--"+tmp);
        		}
        	}else{
        		meaning.put(points, knowleadge);
        	}
        }
        JSONObject layouts = JSONObject.fromObject(meaning); 
        knowledgepoint.put("detail", layouts);*/
        return knowledgepoint;
        
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
    	/*try {
			resolvefile("E:\\test1\\pointcard_1.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
       try {
            new JyeooKnowLeadge().execute();
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
			String origin_sql = "insert into points(id,detail) values ";
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
			//BufferedReader reader = null;
			String sql = "select id,subject_original_id,original_id from points where original_id is not null and original_id <> '' and (title is null or title = '') and  length(original_id)>1 order by id desc";
			ResultSet selectRes = stmt.executeQuery(sql);			
			while (selectRes.next()) {
			/*FileInputStream fileInputStream = new FileInputStream(new LingoesLd2Reader().getClass().getResource("ViconEnglishDictionary.ld2.words").getPath());
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			//for(Iterator old = front.iterator();old.hasNext();){
			while ((tempString = reader.readLine()) != null) {*/
			   //String xueba = (String) old.next();
					//for(int c = 1;c<=26;c++){
						//char n = (char)(c+96);
						//String now = xueba + n;
				Integer id = selectRes.getInt("id");	
				 String original_id = selectRes.getString("original_id");
				 String subject_original_id = selectRes.getString("subject_original_id");
						/*String sql = "insert into tmp values(\""+now+"\","+max+");";
						ps = con.prepareStatement(sql);
						ps.executeUpdate();*/
						//newfront.add(now);			
						url = "http://www.jyeoo.com/"+subject_original_id+"/api/pointcard?a="+URLEncoder.encode(original_id);
						System.out.println(url);
						long start = System.currentTimeMillis(); 
						org.jsoup.Connection connect = Jsoup.connect(url);
						connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
						connect.header("Accept-Encoding", "gzip, deflate, sdch");
						connect.header("Accept-Language", "zh-CN,zh;q=0.8");
						connect.header("Connection", "keep-alive");
						connect.header("Cookie", "jye_geography_ques_0_q=0e1d9698-9a7e-4595-be06-3ccebf49e78a~aeb19716-887f-4afd-8896-1cf4f164329d~; jye_math3_ques_s=0; jye_math3_ques_d=0; jye_math3_ques_t=0; jye_math3_ques_0_q=bc2d00e7-3e53-4464-9dd2-3a151c4827d4~5ad53fe4-7322-4808-a390-a3828f42fffd~; jyean=0xL4gJavlQOQ9_YaHCV4eOM8G1NMQjtTWaOfIERGqs3TMPcqQcxsKm61aM1bkW4m7vu29blF-oLLdcBeaywmhPUYwmF9QoBh1-c1PbRFBar3PJMAggKEF-j527gvKxf80; __RequestVerificationToken=xfJu5e1Jl7ktgkvY1MJY8dzDOKSzv3rMUWuzfT2oEVwBozJGn40HE5Yrsxaf3F8tREft2agPcCzWLxFqiSxAEJXVhsZS6RDt32xT3pGc67c1; jye_notice_show=2|2015/6/23 15:02:06|1|false; LF_Email=jiangdongcaiwei@163.com; jy=69BE7C64203DAC47ADFB83F6045088C92D6230C01E41BEEC64CDE79DD7554E525AC96BA7CF882462AA12B42A751E5DDD8822EA90498864A6A509E9EC14486BE78BBC1B0DEC375A7551324FBA31EC92A292C9A691AC213AA6FE63FD86BC0470676D4D64C3D361A59F298C40743416CD2744EC6CD3A419E57DA1980C75111B56F0BAB19717A87D782A1D1744C9DA029869E40CBA784AF7207D6E667A5783235FEB0E9CD414CFA445F6FC4B6676FBA3A972B7B13B322E722E8B7D3562C090B16CF65A9C79DFC5BA54C1853FF6ED03354CD6BCD254D83344D8313D78CF09BCB162FDCAC4A822ECCB4A7612AF367042A412358EBA43F14A0C113E752A34DA5ADE098A9225D9F8C035650F50C7EE8583AD60797D1A6A074B302CCF33A847F66BF3B7FC; jye_search_q=0; JYERN=0.5034323348663747; jye_math_ques_s=0; jye_math_ques_d=0; jye_math_ques_t=0; jye_math_ques_0_q=75a08844-6562-4bf5-a182-034cf7929588~2af82177-bb3a-4ae0-9557-d507ac8867b4~19; jye_physics_ques_s=0; jye_physics_ques_d=0; jye_physics_ques_t=0; jye_physics_ques_0_q=f01deeb1-7d50-4553-9ace-37f89dd1bd3b~44df696b-f2a9-4e30-a5ca-39b36e722aa8~62; jye_chemistry_ques_s=0; jye_chemistry_ques_d=0; jye_chemistry_ques_t=0; jye_chemistry_ques_0_q=4e8b555e-9c56-4daf-9049-b7a438b2f249~bab4650f-c373-4235-9c0c-438f39c8d468~E1; CNZZDATA2018550=cnzz_eid%3D1496876744-1428467412-%26ntime%3D1435043302");
						connect.header("Host", "www.jyeoo.com");
						connect.header("RA-Sid", "74E70381-20150304-100858-3512c8-872ba6");
						connect.header("RA-Ver", "2.9.0");
						connect.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36");
						try{
						Document doc = connect.timeout(10000).get();
						System.out.println("Time is:"+(System.currentTimeMillis()-start) + "ms"); 
						Map<String,Object> knowledgepoint = resolvefile(doc);
						if(knowledgepoint!=null){
						 /*if(i == 0)insertsql = origin_sql;
							 if(i < sql_length){
							 insertsql = mark_sql(insertsql,knowledgepoint,",");
							     i  = i + 1;//System.out.println(insertsql);
							  }else if(i >= sql_length){
							   insertsql = mark_sql(insertsql,knowledgepoint,";");
							   Util.writetoFile("D:\\devAll\\jyeoopoints.sql",insertsql);
							    i = 0;
							  }*/
							insertsql = "update points set title = \""+Util.addcslashes(knowledgepoint.get("title"))+"\",detail = \""+Util.addcslashes(knowledgepoint.get("detail"))+"\" where id = "+id+";";
							Util.writetoFile("D:\\devAll\\jyeoopoints.sql",insertsql);
							}
						try{
							Thread.sleep(10000);
						}catch(InterruptedException e){
							System.out.println("Thread interrupted...");
						}
					}catch(IOException e){
						e.printStackTrace();
						continue;
					}
				}
			//reader.close();
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
			} catch (Exception e) {
				System.out.print("MYSQL ERROR:" + e.getMessage());
				e.printStackTrace();
			  }
		}
	}
	
}
