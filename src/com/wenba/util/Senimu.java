package com.wenba.util;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.Reader;  
  
import javax.script.Invocable;  
import javax.script.ScriptEngine;  
import javax.script.ScriptEngineManager;  
  
public class Senimu  
{  
      
    public static void main(String[]arg)throws IOException  
    {  
        // 得到一个ScriptEngine对象  
        ScriptEngineManager maneger = new ScriptEngineManager();  
        ScriptEngine engine = maneger.getEngineByName("JavaScript");  
          
        // 读js文件  
        String jsFile = "E:\\tools\\ckplayer.js";  
        FileInputStream fileInputStream = new FileInputStream(new File(jsFile));  
        Reader scriptReader = new InputStreamReader(fileInputStream, "utf-8");  
          
        try  
        {  
            engine.eval(scriptReader);  
            if (engine instanceof Invocable)  
            {  
                // 调用JS方法  
                Invocable invocable = (Invocable)engine;  
                String result = (String)invocable.invokeFunction("guolv", new Object[]{"aHR0cDovL3ZpZGVvLmRlYXJlZHUuY29tL2d6L3N4L2NoZW5ob25nd2VpL3N4X2QxX2poeWpkbGoubXA0"});  
                System.out.println(result);  
                System.out.println(result.length());  
            }  
        }  
        catch (Exception e)  
        {  
            e.printStackTrace();  
        }  
        finally  
        {  
            scriptReader.close();  
        }  
    }  
}  