package test1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadOnline {
	static String OnlinePath="C:\\Users\\Eric\\Desktop\\jssec\\fingerprint\\fingerprint\\1.5meters.online.trace.txt";
	static BufferedReader br;

	static ArrayList <String> aplist=ReadOffline.aplist;
	static ArrayList<Map<String, Double>>eachPosRssList=new ArrayList<Map<String, Double>>();/***一个map存放一个点的信号向量***/
	static ArrayList <String> positionlist=new ArrayList<>();
	static ArrayList <Double> posXlist=new ArrayList<>();
	static ArrayList <Double> posYlist=new ArrayList<>();
	static int collecting_times=110;//之后设为一个参数
	static double default_rss=-100.0;
	
	static Pattern rss_pattern=Pattern.compile(Constant.rss_regex);
	static Pattern pos_pattern=Pattern.compile(Constant.pos_regex);
	static final double probability=0.3118;

	//static ArrayList<Map<String, Double>>eachPosRandomRssList=new ArrayList<>();
	
	public static void read() {		
		File file=new File(OnlinePath);
	
		Matcher pos_matcher;
		
		String each_pos = null;
		String fixedid="id=00:02:2D:21:0F:33;";
		
		if(file.exists()){
			try {
				FileInputStream fis;
				fis = new FileInputStream(file);
				InputStreamReader isr=new InputStreamReader(fis);
				BufferedReader brr=new BufferedReader(isr);
				
				String line;
				int i=0;//行号
				int []count=new int [30];
				double []sum=new double[30];
				while((line=brr.readLine())!=null){// && i<140

					if(i%116>=5&&i%116<=114){//0-4 5-114 115 || 116-120 121...
						//进入具体数据
						Map<String,Double> map=new HashMap<>();
						
						if(i%116==5){//每110行一开头 先得到当前位置 如（-23.5，-10.75）
							pos_matcher=pos_pattern.matcher(line);
							if(pos_matcher.find()){
								each_pos=pos_matcher.group(1);
								positionlist.add(each_pos);//online的aplist多一个
							}
							Pattern p=Pattern.compile("(.*?),(.*?),0.0");
							Matcher m=p.matcher(each_pos);
							if(m.matches()){
								posXlist.add(Double.valueOf(m.group(1)));
								posYlist.add(Double.valueOf(m.group(2)));
							}
							//System.out.println("position: "+each_pos+" ************************");
						}											
						
						String target=line.replace(fixedid, "");
						Matcher m=rss_pattern.matcher(target);//m1 mac地址  m2信号强度
						while(m.find()){/***开始读取某一行的有效信息***/
							String each_ap="00:"+m.group(1);
							if(!aplist.contains(each_ap)){
								aplist.add(each_ap);
								//System.out.println(each_ap+" not exists");
							}
							int k=aplist.indexOf(each_ap);
							sum[k]+=Double.valueOf(m.group(2));
							count[k]++;									
						}
						
						if((i+1)%116==115){//读完了一组数据
							for(int k=0;k<aplist.size();k++){
								double avgrss=(count[k]==0?-100.0:sum[k]/count[k]);
								if(count[k]<20)/****认为收到20次以下 则所收到的信号无效****/
									avgrss=-100.0;
								map.put(aplist.get(k), avgrss);
								if(avgrss!=0);
									//System.out.println(aplist.get(k)+" "+avgrss+" "+count[k]);
							}
							eachPosRssList.add(map);
							for(int p=0;p<30;p++){
								count[p]=0;
								sum[p]=0;
							}
						}
					}
					/***读完了一行***/
					i++;
				}

				System.out.println("Online pos:"+positionlist.size()+" ap:"+aplist.size()+" eachposrsslist:"+eachPosRssList.size());
				System.out.println("Online reading completed.");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}

	public static void initFile(){
		File file=new File(OnlinePath);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public static void initPosAndAP(){
		int i=0;//行号
		String each_pos = null, line;
		Matcher pos_matcher;
		try {
			while((line=br.readLine())!=null){
				if(i%116==5){//得到位置
					pos_matcher=pos_pattern.matcher(line);
					if(pos_matcher.find()){
						each_pos=pos_matcher.group(1);
						positionlist.add(each_pos);
					}
					Pattern p=Pattern.compile("(.*?),(.*?),0.0");							
					Matcher m=p.matcher(each_pos);
					if(m.matches()){
						posXlist.add(Double.valueOf(m.group(1)));
						posYlist.add(Double.valueOf(m.group(2)));
					}
				}
				
				String target=line.replace(Constant.fixedid, "");
				Matcher m=rss_pattern.matcher(target);//m1 mac地址 
				while(m.find()){/***开始读取某一行的有效信息***/
					String each_ap="00:"+m.group(1);
					if(!aplist.contains(each_ap))
						aplist.add(each_ap);										
				}
				
				i++;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initFile();
	}
	
	public static void randomRead(){			
		try{
			Random rand=new Random();
			String line;
			int i=0;//行号
			int []count=new int [30];
			double []sum=new double[30];
			int start=0;
			while((line=br.readLine())!=null){//&& i<140

				if( i%116>=5 && i%116<=114 ){//0-4 5-114 115 || 116-120 121...
												
					if(i%116==5){//开始读有效内容	
						start=rand.nextInt(105-collecting_times)+5;//随机生成开始取的位置  每个位置的起始不一样 上限可能是+10	
						//System.out.println("start:"+start+" **********************");
					}											
						
					Map<String,Double> map=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher m=rss_pattern.matcher(target);//m1:mac地址  
					
					if(i%116>=start && i%116<start+collecting_times){
						while(m.find()){/***开始读取某一行的有效信息***/
							String each_ap="00:"+m.group(1);
															
							int k=aplist.indexOf(each_ap);
							sum[k]+=Double.valueOf(m.group(2));
							count[k]++;
								
//							System.out.println(each_ap+" "+m.group(2));
						}
					}
					if((i+1)%116==115){//读完了一组数据
						for(int k=0;k<aplist.size();k++){
							double avgrss=(count[k]==0?-100.0:sum[k]/count[k]);							
							map.put(aplist.get(k), avgrss);							
							if(avgrss!=-100.0);
//								System.out.println(aplist.get(k)+" "+avgrss+" "+count[k]);
						}
						eachPosRssList.add(map);
						
						for(int p=0;p<30;p++){//重新初始化数组
							count[p]=0;
							sum[p]=0;
						}
					}
				}
				i++;/***读完了一行***/
			}  
		}catch (IOException e) {
			e.printStackTrace();
		}	

		System.out.println("Online pos:"+positionlist.size()+" ap:"+aplist.size()+" eachposrsslist:"+eachPosRssList.size());
		System.out.println("Random online reading completed.");			
	}
	
	public static void readHandlingMissedAP(){
		initFile();
		initPosAndAP();		
		try{
			Random rand=new Random();
			String line;
			int i=0;//行号
			int start=0;
			while((line=br.readLine())!=null){//&& i<140

				if( i%116>=5 && i%116<=114 ){//0-4 5-114 115 || 116-120 121...
												
					if(i%116==5){//开始读有效内容	
						start=rand.nextInt(105)+5;//随机生成开始取的位置  每个位置的起始不一样 上限可能是+10	
//						System.out.println("start:"+start+" **********************");
					}											
								
					if(i%116==start){	
						String target=line.replace(Constant.fixedid, "");
						Matcher m=rss_pattern.matcher(target);//m1:mac地址  				
						Map<String,Double> map=new HashMap<>();
						double total=0,avg=0,cnt=0;
						
						while(m.find()){/***开始读取某一行的有效信息***/
							String each_ap="00:"+m.group(1);
							total+=Double.valueOf(m.group(2));
							cnt++;
							map.put(each_ap, Double.valueOf(m.group(2)));	
//							System.out.println(each_ap+" "+m.group(2));
						}
						avg=total/cnt;
						for(int k=0;k<aplist.size();k++){
							if(map.get(aplist.get(k))==null){
								if(isRandomMissed())//如果在31.2%的missed概率中
									map.put(aplist.get(k), avg);
								else map.put(aplist.get(k), -100.0);
							}
						}
						eachPosRssList.add(map);
					}	
				}
				i++;/***读完了一行***/
			}  
		}catch (IOException e) {
			e.printStackTrace();
		}	

		System.out.println("Online pos:"+positionlist.size()+" ap:"+aplist.size()+" eachposrsslist:"+eachPosRssList.size());
		System.out.println("Online reading handling missed ap completed.");
	}
	
	static boolean isRandomMissed(){
		Random rand=new Random();
		int a=rand.nextInt(1000);
		if(a>312)
			return false;
		else return true;
	}
	
	public static void main(String[] args) {
		ReadOffline.initFile();//先读offline的 初始化aplist
		ReadOffline.initPosAndAP();
//		read();
//		ReadOffline.readOff();
		initFile();
		initPosAndAP();
		
//		readHandlingMissedAP();
		
		setTimes(1);//只取一个点只要把times设为1
		randomRead();
		
		displayRSS();

		
//		for(int i=0;i<eachPosRandomRssList.size();i++){
//			System.out.println(eachPosRandomRssList.get(i));
//		}
	}
	
	public static void displayRSS(){
		for(int i=0;i<positionlist.size();i++){
			System.out.println("position: "+positionlist.get(i)+"*******************");
			Map<String,Double>m=eachPosRssList.get(i);
			for(int j=0;j<aplist.size();j++){
				System.out.println(aplist.get(j)+":"+m.get(aplist.get(j)));
			}	
		}
	}
	
	static void setTimes(int times){
		collecting_times=times;
	}
	
	static void setDefaultRss(double d){
		default_rss=d;
	}
}
