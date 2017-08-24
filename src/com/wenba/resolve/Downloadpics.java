package com.wenba.resolve;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.wenba.resolve.JyeooKnowLeadge.Splidedict;
import com.wenba.util.Util;

public class Downloadpics {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
            new Downloadpics().execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
	}
    public void execute() throws InterruptedException {
    	System.out.println("-start--");
    	/*
        Thread[] threads = new Thread[3];
        for (int x = 0; x < threads.length; x++) {
            threads[x] = new Thread(new Download());
            threads[x].start();
        }
 
        for (int x = 0; x < threads.length; x++) {
            threads[x].join();
        }*/
    	Thread threads = new Thread(new Download());
    	threads.start();       
        System.out.println("end");
    }
    
    static class Download implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader reader = null;
			try{
				FileInputStream fileInputStream = new FileInputStream("D:\\pics\\feeds_info_2015-05-18.txt");
				InputStreamReader inputStreamReader = new InputStreamReader(
						fileInputStream, "UTF-8");
				reader = new BufferedReader(inputStreamReader);
				String tempString = null;
				//for(Iterator old = front.iterator();old.hasNext();){
				while ((tempString = reader.readLine()) != null) {
					tempString = "http://wb-img.u.qiniudn.com/"+tempString;
					Util.makeImg(tempString,"D:\\pics\\");
					/*try{
						Thread.sleep(3000);
					}catch(InterruptedException e){
						System.out.println("Thread interrupted...");
					}*/
				}
				reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
    	
    }
}
