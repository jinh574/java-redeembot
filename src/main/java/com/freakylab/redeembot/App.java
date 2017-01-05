package com.freakylab.redeembot;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
	public static int lastPostId = 0;
	
	public static ArrayList<String> getRedeemCode() {
		ArrayList<Integer> redeemPosts = new ArrayList<Integer>();
		ArrayList<String> redeems = new ArrayList<String>();
//		String findBoardUrl = "http://www.clien.net/cs2/bbs/board.php?bo_table=cm_iphonien&sca=&sfl=wr_subject&stx=%EB%A6%AC%EB%94%A4&x=0&y=0";
		String findBoardUrl = "http://www.clien.net/cs2/bbs/board.php?bo_table=cm_iphonien&page=19&page=20";
		String findRedeemUrl = "http://www.clien.net/cs2/bbs/board.php?bo_table=cm_iphonien&wr_id=";
		final String redeemRegx = "[0-9A-Z]{12}";
		
		Pattern pattern = Pattern.compile("[0-9A-Z]{12}");
		
		try {
			Document doc = Jsoup.parse(new URL(findBoardUrl).openStream(), "UTF8", findBoardUrl);
			Elements subjects = doc.select(".mytr .post_subject a");
			for(Element subject : subjects) {
				if(subject.text().contains("리딤")) {
					redeemPosts.add(Integer.parseInt(((Element) subject.parentNode().siblingNodes().get(1)).text()));
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int id : redeemPosts) {
			if(id > lastPostId) {
				try {
					String requestUrl = findRedeemUrl + id;
					Document doc = Jsoup.parse(new URL(requestUrl).openStream(), "UTF8", requestUrl);
					Elements tmpContents = doc.select("#writeContents p");
					for(Element itm : tmpContents) {
						if(itm.text().matches(redeemRegx)) {
							redeems.add(itm.text());
						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		return redeems;
	}
	
	public static void main( String[] args ) {
		ArrayList<String> redeems;
		String urlSchema = "macappstores://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/redeemLandingPage?code=";
		final String token = "o.hzS5GgPx3krqYQN3dCL65086V4YleR4u";
		final String sendUrl = "https://api.pushbullet.com/v2/pushes"; 
		final String deviceToken = "ujD7x1zs2oKsjAewzdwpCC";
				
		redeems = getRedeemCode();
		if(redeems.size() > 0) {
			int select = redeems.size() - (int)(Math.random() * 10 + 1);
			String url = urlSchema + redeems.get(select);	
			try {
				HttpURLConnection con = (HttpURLConnection) new URL(sendUrl).openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Access-Token", token);
				con.setRequestProperty("Content-Type", "application/json");
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes("{ \"title\":\"Redeem Detected.\", \"body\":\"Registry Redeem code.\", \"type\":\"link\", \"url\":\"" + url + "\", \"device_iden\":\"" + deviceToken + "\"}");
				wr.flush();
				wr.close();
				System.out.println(con.getContent());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
