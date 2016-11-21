package test1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Calculate {
	public static void main(String[] arg) {
		
		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:myfingerprint.db");
//			System.out.println("Opened database successfully");
//			stmt = c.createStatement();
			String sql="DROP TABLE RSS1";// IF EXISTS RSS1
			//stmt.executeQuery(sql);
			
			sql = "CREATE TABLE RSS2"+
	                   " (ID INTEGER PRIMARY KEY   AUTOINCREMENT," + 
	                   " ONELINE           TEXT   NOT NULL ) ";
//			  System.out.println("begin to create table");
			  //stmt.executeUpdate(sql);
			  
			  
			File file = new File(Path);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int i = 0;
				while (i<100) {//((lineTxt = bufferedReader.readLine()) != null)
					i++;
//					if(i/10000*10000 == i) System.out.println(i);
					/*sql="INSERT INTO RSS1 (ONELINE) " +
				    		  "VALUES ('"+lineTxt+"');"; 
		      stmt.executeUpdate(sql);*/
					System.out.println(lineTxt);
				}
				stmt.close();
			  c.close();
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}

	}
	
	static String myPath = "C:\\Users\\Eric\\Desktop\\jssec\\offline\\myoffline.txt";
	static String Path = "C:\\Users\\Eric\\Desktop\\jssec\\offline\\offline.final.trace.txt";
	static String OfflinePath="C:\\Users\\Eric\\Desktop\\jssec\\fingerprint\\fingerprint\\1.5meters.offline.trace.txt";
	static private Connection c;
	static private Statement stmt;

}
