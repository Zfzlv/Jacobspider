package com.wenba.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.parser.JSONParser;

public class Util {
	public static String ReadFile(String Path) {
		BufferedReader reader = null;
		String laststr = "";
		try {
			FileInputStream fileInputStream = new FileInputStream(Path);
			InputStreamReader inputStreamReader = new InputStreamReader(
					fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				laststr += tempString;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return laststr;
	}

	public static void writeFile(String filePath, String sets) {
		try {
			FileWriter fw = new FileWriter(filePath);
			PrintWriter out = new PrintWriter(fw);
			out.write(sets);
			out.println();
			fw.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void writetoFile(String FileName, String sets){
		try {
			BufferedWriter writer = null;
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(FileName, true)));
			writer.write(sets);
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * JAVA对象转换成JSON字符串
	 * 
	 * @param obj
	 * @return
	 * @throws MapperException
	 */
	public static String objectToJsonStr(Object obj) throws MapperException {
		JSONValue jsonValue = JSONMapper.toJSON(obj);
		String jsonStr = jsonValue.render(false);
		return jsonStr;
	}

	/**
	 * 重载objectToJsonStr方法
	 * 
	 * @param obj
	 *            需要转换的JAVA对象
	 * @param format
	 *            是否格式化
	 * @return
	 * @throws MapperException
	 */
	public static String objectToJsonStr(Object obj, boolean format)
			throws MapperException {
		JSONValue jsonValue = JSONMapper.toJSON(obj);
		String jsonStr = jsonValue.render(format);
		return jsonStr;
	}

	/**
	 * JSON字符串转换成JAVA对象
	 * 
	 * @param jsonStr
	 * @param cla
	 * @return
	 * @throws MapperException
	 * @throws TokenStreamException
	 * @throws RecognitionException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object jsonStrToObject(String jsonStr, Class<?> cla)
			throws MapperException, TokenStreamException, RecognitionException {
		Object obj = null;
		try {

			JSONParser parser = new JSONParser(new StringReader(jsonStr));
			JSONValue jsonValue = parser.nextValue();
			if (jsonValue instanceof com.sdicons.json.model.JSONArray) {
				List list = new ArrayList();
				JSONArray jsonArray = (JSONArray) jsonValue;
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONValue jsonObj = jsonArray.get(i);
					Object javaObj = JSONMapper.toJava(jsonObj, cla);
					list.add(javaObj);
				}
				obj = list;
			} else if (jsonValue instanceof com.sdicons.json.model.JSONObject) {
				obj = JSONMapper.toJava(jsonValue, cla);
			} else {
				obj = jsonValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 将JAVA对象转换成JSON字符串
	 * 
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleObjectToJsonStr(Object obj, List<Class> claList)
			throws IllegalArgumentException, IllegalAccessException {
		if (obj == null) {
			return "null";
		}
		String jsonStr = "{";
		Class<?> cla = obj.getClass();
		Field fields[] = cla.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getType() == long.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getLong(obj)
						+ ",";
			} else if (field.getType() == double.class) {
				jsonStr += "\"" + field.getName() + "\":"
						+ field.getDouble(obj) + ",";
			} else if (field.getType() == float.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getFloat(obj)
						+ ",";
			} else if (field.getType() == int.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.getInt(obj)
						+ ",";
			} else if (field.getType() == boolean.class) {
				jsonStr += "\"" + field.getName() + "\":"
						+ field.getBoolean(obj) + ",";
			} else if (field.getType() == Integer.class
					|| field.getType() == Boolean.class
					|| field.getType() == Double.class
					|| field.getType() == Float.class
					|| field.getType() == Long.class) {
				jsonStr += "\"" + field.getName() + "\":" + field.get(obj)
						+ ",";
			} else if (field.getType() == String.class) {
				jsonStr += "\"" + field.getName() + "\":\"" + field.get(obj)
						+ "\",";
			} else if (field.getType() == List.class) {
				String value = simpleListToJsonStr((List<?>) field.get(obj),
						claList);
				jsonStr += "\"" + field.getName() + "\":" + value + ",";
			} else {
				if (claList != null && claList.size() != 0
						&& claList.contains(field.getType())) {
					String value = simpleObjectToJsonStr(field.get(obj),
							claList);
					jsonStr += "\"" + field.getName() + "\":" + value + ",";
				} else {
					jsonStr += "\"" + field.getName() + "\":null,";
				}
			}
		}
		jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
		jsonStr += "}";
		return jsonStr;
	}

	/**
	 * 将JAVA的LIST转换成JSON字符串
	 * 
	 * @param list
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleListToJsonStr(List<?> list, List<Class> claList)
			throws IllegalArgumentException, IllegalAccessException {
		if (list == null || list.size() == 0) {
			return "[]";
		}
		String jsonStr = "[";
		for (Object object : list) {
			jsonStr += simpleObjectToJsonStr(object, claList) + ",";
		}
		jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
		jsonStr += "]";
		return jsonStr;
	}

	/**
	 * 将JAVA的MAP转换成JSON字符串， 只转换第一层数据
	 * 
	 * @param map
	 * @return
	 */
	public static String simpleMapToJsonStr(Map<?, ?> map) {
		if (map == null || map.isEmpty()) {
			return "null";
		}
		String jsonStr = "{";
		Set<?> keySet = map.keySet();
		for (Object key : keySet) {
			jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
		}
		jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
		jsonStr += "}";
		return jsonStr;
	}

	/**
	 * unicode转中文
	 * 
	 * @param theString
	 * @return
	 */
	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}

	/**
	 * 字符串转unicode
	 * 
	 * @param theString
	 * @return
	 */
	public static String string2Unicode(String s) {
		try {
			StringBuffer out = new StringBuffer("");
			byte[] bytes = s.getBytes("unicode");
			for (int i = 2; i < bytes.length - 1; i += 2) {
				out.append("u");
				String str = Integer.toHexString(bytes[i + 1] & 0xff);
				for (int j = str.length(); j < 2; j++) {
					out.append("0");
				}
				String str1 = Integer.toHexString(bytes[i] & 0xff);

				out.append(str);
				out.append(str1);
				out.append(" ");
			}
			return out.toString().toUpperCase();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * unicode转字符串
	 * 
	 * @param theString
	 * @return
	 */
	public static String unicode2String(String unicodeStr) {
		StringBuffer sb = new StringBuffer();
		String str[] = unicodeStr.toUpperCase().split("U");
		for (int i = 0; i < str.length; i++) {
			if (str[i].equals(""))
				continue;
			char c = (char) Integer.parseInt(str[i].trim(), 16);
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * 把中文转成Unicode码
	 * 
	 * @param str
	 * @return
	 */
	public static String chinaToUnicode(String str) {
		String result = "";
		for (int i = 0; i < str.length(); i++) {
			int chr1 = (char) str.charAt(i);
			if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
				result += "\\u" + Integer.toHexString(chr1);
			} else {
				result += str.charAt(i);
			}
		}
		return result;
	}

	/**
	 * 判断是否为中文字符
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * sql插入
	 * 字符串转义
	 * 
	 */
	public static String addcslashes(Object obj){
		String a = String.valueOf(obj);
        a = a.replaceAll("\\\\", "\\\\\\\\");
        a = a.replaceAll("\\'", "\\\\'");
        a = a.replaceAll("\\000", "\\\\000");
        a = a.replaceAll("\\n", "\\\\n");
        a = a.replaceAll("\\r", "\\\\r");
        a = a.replaceAll("\"", "\\\\\"");
        a = a.replaceAll("\\032", "\\\\032");
        return a;		
	}
	
	/**
	 * 抓取图片
	 * 
	 */
	public static void makeImg(String imgUrl,String fileURL) {
			try {
				// 创建流
				BufferedInputStream in = new BufferedInputStream(new URL(imgUrl)
						.openStream());

				// 生成图片名
				int index = imgUrl.lastIndexOf("/");
				String sName = imgUrl.substring(index+1, imgUrl.length());
				System.out.println(sName);
				// 存放地址
				File img = new File(fileURL+sName);
				// 生成图片
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(img));
				byte[] buf = new byte[2048];
				int length = in.read(buf);
				while (length != -1) {
					out.write(buf, 0, length);
					length = in.read(buf);
				}
				in.close();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
