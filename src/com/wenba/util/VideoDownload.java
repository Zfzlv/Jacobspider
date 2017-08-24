package com.wenba.util;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
public class VideoDownload {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    	Statement stmt; // 创建声明
    	 try {
    			Connection con = null; // 定义一个MYSQL链接对象
    			PreparedStatement ps = null;
    			Class.forName("com.mysql.jdbc.Driver").newInstance(); // MYSQL驱动
    			con = DriverManager
    					.getConnection(
    							"jdbc:mysql://10.88.0.234/weike?characterEncoding=utf-8",
    							"root", "tikuserversee");
    					
    					stmt = con.createStatement();
    	
    	String selectSql = "SELECT videoname FROM W_Video";
    					ResultSet selectRes = stmt.executeQuery(selectSql);
    					while (selectRes.next()) {
    						String url = selectRes.getString("videoname");
    						String path = url.replace("http://", "D:/");
    						path = path.replace("/", "//");
    						String destDirName = path.replaceAll("//[^/]*$", "");
    						File dir = new File(destDirName);  
    						if (!dir.exists()) { 
    							if (dir.mkdirs()) {  
    					            System.out.println("创建目录" + destDirName + "成功！");   
    					        } else {  
    					            System.out.println("创建目录" + destDirName + "失败！");  
    					        }  
    						}
        /*final String url = "http://video.dearedu.com/gz/sx/chenhongwei/sx_d1_jhyjdlj.mp4";					
        final String path = "d://sx_d1_jhyjdlj.mp4";*/
        final long filesize;
        RemoteFile rf=getRemoteFile(url);
        long remoteSize=rf.size;
        final String realUrl=rf.realUrl;
        //得到重定向后的真实URL，经测试，优酷视频，如果不用真实URL下载，会报403错误
        System.out.println("真实URL"+realUrl);
        System.out.println("远程文件大小" + remoteSize);
        File f = new File(path);
        if (f.exists()) {
            filesize = f.length();
        } else {
            filesize = 0;
        }
        System.out.println("本地文件长度" + filesize);
        if (filesize < remoteSize) {
            /*new Thread() {
                public void run() {*/
                    try {
                        System.out.println("开始下载--"+url);
                        URL u = new URL(realUrl);
                        HttpURLConnection connection = (HttpURLConnection) u
                                .openConnection();
                        connection.setRequestProperty("RANGE", "bytes="+ filesize+"-");
                        //根据已下载的大小，设置下载的起点
                        connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.1.4322)"); 
                        //设置UA
                        InputStream input = connection.getInputStream();
                        //读入InputStream
                        RandomAccessFile SavedFile = new RandomAccessFile(path, "rw");
                        //建立随机访问对象
                        SavedFile.seek(filesize);
                        //文件指针移动到文件末尾
                        byte[] b = new byte[1024];
                        //新建byte对象
                        int nRead;
                        long readed=filesize;
                        //readed用于存储已下载的字节数,所以初始值为文件大小
                        while ((nRead = input.read(b, 0, 1024)) > 0) {
                            //从InputStream循环读入byte对象，nRead为实际读入的byte对象长度
                            readed+=nRead;
                            //已下载长度计数
                            //System.out.println(readed);
                            SavedFile.write(b, 0, nRead);
                            //把byte对象写入文件
                        }
                        connection.disconnect();
                        //断开连接
                        SavedFile.close();
                        //关闭文件
                        System.out.println("结束下载--"+url);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            /*}.start();
        }*/
        }
    	 } catch (Exception e) {
 			System.out.print("MYSQL ERROR:" + e.getMessage());
 			e.printStackTrace();
 		  }
    }

    public static RemoteFile getRemoteFile(String url) {
        long size = 0;
        String realUrl="";
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url))
                    .openConnection();
            size = conn.getContentLength();
            //远程文件体积
            realUrl=conn.getURL().toString();
            //真实URL
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RemoteFile rf=new RemoteFile(size,realUrl);
        //把取得的文件体积及真实URL返回
        return rf;
    }

}
class RemoteFile{
    //RemoteFile类，用于存储远程文件的大小及重定向后的地址
    long size;
    String realUrl;
    RemoteFile(long size,String realUrl){
        this.size=size;
        this.realUrl=realUrl;
    }
}