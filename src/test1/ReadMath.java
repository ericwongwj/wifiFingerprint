package test1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ReadMath {

	static String path="C:\\Users\\Eric\\Desktop\\mathmodel\\ptsidx3.txt";
	static String pathout="C:\\Users\\Eric\\Desktop\\mathmodel\\ptsidx4.txt";

	public static void main(String[] args) {
		extract2();
	}
	
	static void extract2(){
		File file=new File(path);
		File filew=new File(pathout);
	
		try {
			if (!filew.exists())			
				filew.createNewFile();
			FileInputStream fis = new FileInputStream(filew);
			InputStreamReader isr=new InputStreamReader(fis);
			BufferedReader br=new BufferedReader(isr);
//			FileOutputStream fos = new FileOutputStream(filew);
//			OutputStreamWriter osw=new OutputStreamWriter(fos);
//			BufferedWriter bw=new BufferedWriter(osw);
			String line;
			int j=0,size=0;
			int lastx=0;
			double sumx=0,sumy=0,temp=0;
			while((line=br.readLine())!=null){
				line=line.replaceAll(" ","");
				if(j%2==0){
					size++;
					temp++;
					Integer i=Integer.valueOf(line);//471*614 先列再行
					if(i%471==0)
						continue;
					int y=i%471;
					int x=i/471+1;
					if(y==2){
						temp--;
						sumx-=2;
						sumy-=49;
					}
					if(x-lastx>10){//一个点读完
						System.out.println(sumx/(temp-1)+" "+sumy/(temp-1));
						temp=1;
						sumx=sumy=0;
					}
					lastx=x;
					sumx+=x;
					
					int b=471-y;
					sumy+=b;
					
					//System.out.println(b+" "+y);//i+" "++" "+y
				}
				j++;
//				bw.write(line);
//				bw.newLine();
			}
//			bw.flush();
			System.out.println(sumx/temp+" "+sumy/temp);

			System.out.println("size is "+size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void extract1(){
		File file=new File(pathout);
		File filew=new File(pathout);
	
		try {
			if (!file.exists())			
				file.createNewFile();
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			BufferedReader br=new BufferedReader(isr);
//			FileOutputStream fos = new FileOutputStream(filew);
//			OutputStreamWriter osw=new OutputStreamWriter(fos);
//			BufferedWriter bw=new BufferedWriter(osw);
			String line;
			int j=0,size=0;
			int lastx=0;
			double sumx=0,sumy=0,temp=0;
			while((line=br.readLine())!=null){
				//line=line.replaceAll(" ","");
				//line+="\n";
				if(j%2==0){
					size++;
					temp++;
					Integer i=Integer.valueOf(line);//364*475 先列再行 特例 2 49
					int y=i%364;
					int x=i/364+1;
					if(y==2){
						temp--;
						sumx-=2;
						sumy-=49;
					}
					if(x-lastx>10){//一个点读完
						System.out.println(sumx/(temp-1)+" "+sumy/(temp-1));
						temp=1;
						sumx=sumy=0;
					}
					lastx=x;
					sumx+=x;
					
					int b=364-y;
					sumy+=b;
					
					System.out.println(b+" "+y);//i+" "++" "+y
				}
				j++;
				//bw.write(line);
				//bw.newLine();
			}
			//bw.flush();
			System.out.println(sumx/temp+" "+sumy/temp);

			System.out.println("size is "+size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
