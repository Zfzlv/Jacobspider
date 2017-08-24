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

public class BaiduTranslater {
	
	public static Map<String,Object> resolvefile(Document doc) throws IOException{
		Map<String, Object> correctword = new HashMap<String, Object>();
		//File file = new File(arg0);
		//Document doc = Jsoup.parse(file, "utf-8");
        String outerHtml = doc.outerHtml();
        if(!outerHtml.contains("correctxt")){correctword.put("correctname", "");return correctword;}
        outerHtml = FullCharConverter.full2HalfChange(outerHtml);//System.out.println(outerHtml);
        for (String x : zhuanmabiao.keySet()) {
            outerHtml = outerHtml.replace(Jsoup.parse(x).text(), zhuanmabiao.get(x));
        }
        //String clean = Jsoup.clean(outerHtml, WhitelistUtil.relaxed);//.replace("&nbsp;", " ");
        //System.out.println(outerHtml);
        doc = Jsoup.parse(outerHtml);         
        	String name = doc.text();//System.out.println(name);
        	name = name.replaceAll("^[\\s\\S]*?\"correctxt\":\"([\\s\\S]*?)\",\"[\\s\\S]*$", "$1");
        	correctword.put("correctname", name);
        	return correctword;
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
    	sql += "(\""+Util.addcslashes(dict.get("name"))+"\",\""+Util.addcslashes(dict.get("correctname"))+"\")"+point;
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
            new BaiduTranslater().execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }
    
    public void execute() throws InterruptedException {
    	System.out.println("-start--");
    	try{
    		Connection con = null; // 定义一个MYSQL链接对象
			PreparedStatement ps = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance(); // MYSQL驱动
			con = DriverManager.getConnection(
							"jdbc:mysql://10.88.0.235/lijie?characterEncoding=utf-8",
							"root", "tikuserverdb");
			Statement stmt; // 创建声明
			stmt = con.createStatement();
			int count = 0;
			String sql = "select count(*) as c from correctword where isIndexed = 0";
			ResultSet counts = stmt.executeQuery(sql);
			while (counts.next()) {
				count = Integer.parseInt(counts.getString("c"));
			}
			int tmp = count/5;int less = count%5;		
        Thread[] threads = new Thread[5];
        if(less != 0)threads = new Thread[6];
        for (int x = 0; x < threads.length; x++) {
        	String ids = "";
        	sql = "select id from correctword where isIndexed = 0 limit "+tmp;
        	ResultSet selectRes = stmt.executeQuery(sql);			
			while (selectRes.next()) {
				Integer id = selectRes.getInt("id");
				ids += id+",";
			}
			ids = ids.replaceAll(",$", "");
			sql = "update correctword set isIndexed =1 where id in("+ids+")";
			ps = con.prepareStatement(sql);
			ps.executeUpdate();
            threads[x] = new Thread(new Splidedict(ids));
            threads[x].start();
        }
        for (int x = 0; x < threads.length; x++) {
            threads[x].join();
        }
    	} catch (Exception e) {
			System.out.print("MYSQL ERROR:" + e.getMessage());
			e.printStackTrace();
		  }
    	/*Thread threads = new Thread(new Splidedict());
    	threads.start();     */  
        System.out.println("end");
    }
    
    static class Splidedict implements Runnable {
		// TODO Auto-generated method stub
    	String ids = "";
    	
    	Splidedict(String id){
    		this.ids = id;
    	}
    	
    	Splidedict(){
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int sql_length = 1000; //sql拼接的数据数
			String origin_sql = "insert into correctword(name,correctname) values ";
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
			String sql = "select id,name from correctword where id in("+ids+")";
			ResultSet selectRes = stmt.executeQuery(sql);			
			while (selectRes.next()) {
			/*FileInputStream fileInputStream = new FileInputStream("C:\\Users\\PC\\Documents\\Tencent Files\\657149104\\FileRecv\\tmp.txt");
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;*/
			//for(Iterator old = front.iterator();old.hasNext();){
			//while ((tempString = reader.readLine()) != null) {
			   //String xueba = (String) old.next();
					//for(int c = 1;c<=26;c++){
						//char n = (char)(c+96);
						//String name = tempString;
				Integer id = selectRes.getInt("id");	
				 String name = selectRes.getString("name");
						/*String sql = "insert into tmp values(\""+now+"\","+max+");";
						ps = con.prepareStatement(sql);
						ps.executeUpdate();*/
						//newfront.add(now);	
				 		url = "http://correctxt.baidu.com/correctxt?callback=jQuery19102663917208556086_1435559996458&text="+URLEncoder.encode(name)+"&ie=utf-8&version=0&from=FanyiWeb&_=1435559996459";						
						//url = "http://fanyi.baidu.com/#en/zh/"+URLEncoder.encode(now);
						//url = "http://www.jyeoo.com/"+subject_original_id+"/api/pointcard?a="+URLEncoder.encode(original_id);
						System.out.println(url);
						long start = System.currentTimeMillis(); 
						org.jsoup.Connection connect = Jsoup.connect(url);
						connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
						connect.header("Accept-Encoding", "gzip, deflate, sdch");
						connect.header("Accept-Language", "zh-CN,zh;q=0.8");
						connect.header("Cache-Control", "max-age=0");
						connect.header("Connection", "keep-alive");
						connect.header("Cookie", "BAIDUID=82626C860497342293492CB12DD102D2:FG=1; PSTM=1433483514; BIDUPSID=8061217E5BC7993BF1EEE4FA08C7FF83; MCITY=-%3A; BDSFRCVID=4AksJeCCxG3NQUoluyFCaGmpXdoa_kbfiEJF3J; H_BDCLCKID_SF=JbAjoKK5tKvbfP0kh-QJhnQH-UnLq-69X57Z0lOnMp05enuly4v65PcQXJO-0x7eLIbEXqbLBnRvOKO_e6KWDTc3Da0sK-PX-K-O_4TXHJOoDDkCM4R5y4LdjG5xtt703a6Q0ROHtp6oSUb-DxRo5fPw3-Aq54R23D3t_xO8-UQvfMJ6XnjmQfbQ0h8f2pojymPLBtnv-R7JOpkxbfnxy-Pq0aCqJ5KJfRPfV-TaajrSeJrv54P_-P4Denrj0nJZ5mAqofod5UolMJRSD4n5Xt4meHOMLhvWJCrnaIQqaMnmST6CWRjGKjjDXR5-apc43bRTLI3sL4JZs56pDxQhhP-UyN3-Wh3722OTVDIyJDtWMDtrKPA_M4_B-eTJK43tHD7XVhRC3tOkeq8CDxJayfIzyxvhBpteBCOULlcjtnvboRr2y5jHhT-y5fvN0f6jyD-tKC8-fPcpsIJMy6bS0J84Q2cbQpvIaKviaKJHBMb1jncMe6LBD6jLeaDs-bbfHDv-Lbj-K6rjDnCrb4QMKUI8LNDHt-6NQmDfWnQTQUoYDIJLW-os2R040JO7ttoyK67r-Po9JfK5eqvOqqu2jUL1Db3JW-7I-N5Rsx7zBRroepvoD-oc3MkfDn0EJ6-8tn4j_CvXbRu_HRjYbb__-P4DennjybJZ5m7mXp0bWlOofRRlLqJ2KtAlX4jx0pTgygkL0I5vLDOkbCK4jT8WDTQMKUAXt4Ft2to2WbCQbqcTqpcNLTDK0x4LjG-t2nQ8aCjuWKJuyn5vhCoq5lO1j4_eDfPq34-Lyj7da4bvBnk5bq5jDh3M54AuKbrRelvJaTTy0hvctn5cShncDp00jTQyDH_tJjnfb5kXB43_b5rjDnCrjMQOKUI8LPbO05JwLGTyWP5X3qOHsn6xW-Oo-q4f-xj00xTlt2LE3-oJqC_-hK0R3J; locale=zh; H_PS_PSSID=15975_1445_14602_14444_16035_10812_14432_12867_14669_14871_14961_15599_11501_13936_15963_10634");
						connect.header("Host", "correctxt.baidu.com");
						connect.header("RA-Sid", "74E70381-20150304-100858-3512c8-872ba6");
						connect.header("RA-Ver", "2.10.4");
						connect.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36");
						try{
						Document doc = connect.timeout(10000).ignoreContentType(true).get();
						System.out.println("Time is:"+(System.currentTimeMillis()-start) + "ms"); 
						Map<String,Object> correctword = resolvefile(doc);
						if(correctword!=null){
						/*	correctword.put("name", name);
						 if(i == 0)insertsql = origin_sql;
							 if(i < sql_length){
							 insertsql = mark_sql(insertsql,correctword,",");
							     i  = i + 1;//System.out.println(insertsql);
							  }else if(i >= sql_length){
							   insertsql = mark_sql(insertsql,correctword,";");
							   Util.writetoFile("D:\\devAll\\baidutranslater.sql",insertsql);
							    i = 0;
							  }*/
							insertsql = "update correctword set correctname = \""+Util.addcslashes(correctword.get("correctname"))+"\" where id = "+id+";";
							Util.writetoFile("D:\\devAll\\baidutranslater.sql",insertsql);
							}
						/*try{
							Thread.sleep(3000);
						}catch(InterruptedException e){
							System.out.println("Thread interrupted...");
						}*/
					}catch(IOException e){
						e.printStackTrace();
						//continue;
					}
				}
			/*if(insertsql != origin_sql){
				insertsql = insertsql.replaceAll(",$", ";");
				Util.writetoFile("D:\\devAll\\baidutranslater.sql",insertsql);
			}
			reader.close();*/
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
