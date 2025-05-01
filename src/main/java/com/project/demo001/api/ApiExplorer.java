package com.project.demo001.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ApiExplorer {

	public static void main(String[] args) throws IOException {
		
		StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/eventInfo"); // Url

		urlBuilder.append("?"+URLEncoder.encode("apiKey", "UTF-8")+"="+URLEncoder.encode("	09cb01591e1b488a8e089445738b423f", "UTF-8")); // 공개키
		urlBuilder.append("&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode("all","UTF-8")); // 도로유형
		urlBuilder.append("&"+URLEncoder.encode("eventType","UTF-8")+"="+URLEncoder.encode("1","UTF-8")); // 이벤트유형
		urlBuilder.append("&"+URLEncoder.encode("minX","UTF-8")+"="+URLEncoder.encode("126.600000","UTF-8")); // 최소경도유형
		urlBuilder.append("&"+URLEncoder.encode("maxX","UTF-8")+"="+URLEncoder.encode("127.900000","UTF-8")); // 최대경도유형
		urlBuilder.append("&"+URLEncoder.encode("minY","UTF-8")+"="+URLEncoder.encode("36.800000","UTF-8")); // 최소위도유형
		urlBuilder.append("&"+URLEncoder.encode("maxY","UTF-8")+"="+URLEncoder.encode("38.300000","UTF-8")); // 최대위도유형
		urlBuilder.append("&"+URLEncoder.encode("getType","UTF-8")+"="+URLEncoder.encode("xml","UTF-8")); // 출력유형
		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
		System.out.println("Response code: "+conn.getResponseCode());
		BufferedReader rd;
		if(conn.getResponseCode()>=200&&conn.getResponseCode()<=300) {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}
		StringBuilder sb = new StringBuilder();
		String line;
		while((line=rd.readLine())!=null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
		System.out.println(sb.toString());
	}
	
}
