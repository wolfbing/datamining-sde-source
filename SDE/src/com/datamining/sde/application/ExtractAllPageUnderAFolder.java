package com.datamining.sde.application;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地批量抽取类
 * <br>
 * 用于本地的批量抽取,能够一次将一个文件夹下的所有页面抽取
 * <br>
 * 
 * 
 * @author wolf
 * 
 * @version 1.0
 * 
 * create date: 2013-7-21
 * <br>
 * <br>
 * <b>注意：</b>抽取出的文件默认命名为  originalName+"-extract.html"
 *
 */
public class ExtractAllPageUnderAFolder {

	/**
	 * 默认构造函数
	 */
	public ExtractAllPageUnderAFolder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 抽取一个目录下的所有文件
	 * 
	 * @param inputDir - 文件所在路径，格式 "C:/Users/Admin/Desktop/Mockup1/Cecile/"
	 * @param outputDir - 结果输出路径，格式同上
	 * @throws UnsupportedEncodingException
	 */
	public static void ExtractAllPage(String inputDir, String outputDir) throws UnsupportedEncodingException
	{
		String inputUrl = "file:///"+inputDir;
		String arr_fileName[] = getAllFileName(inputDir);
		for(int i=0;i<arr_fileName.length;++i)
		{
			String fileName = arr_fileName[i];
			System.out.println("extracting   "+fileName+"... ...");
//			String params[] = {
//					inputUrl+URLEncoder.encode(fileName, "utf8").replace("+", "%20"),
//					outputDir+fileName+"-extract.html"
//			};
			Map<String,String> params = new HashMap<String,String>();
			params.put("url", inputUrl+URLEncoder.encode(fileName, "utf8").replace("+", "%20"));
			params.put("-o", outputDir+fileName+"-extract.html");
			new ExtractSinglePage().setOutEncoding("gbk").ExtractPage(params);
			System.out.println("finish  "+(i+1)+"/"+arr_fileName.length);
		}
		
	}
	
	/**
	 * 获取一个文件夹下的所有文件名
	 * 
	 * @param dir - 文件夹路径
	 * @return
	 */
	private static String [] getAllFileName(String dir)
	{
		File file = new File(dir);
		return file.list();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			String inputDir="C:/Users/Admin/Desktop/Mockup1/Cecile/";
			String outputDir="C:/Users/Admin/Desktop/Mockup1/TSUKUMO/";
			ExtractAllPageUnderAFolder.ExtractAllPage(inputDir, outputDir);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
