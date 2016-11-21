package test1;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.*;

public class ReadOffline {
	static BufferedReader br;
	
	static ArrayList <String> aplist=new ArrayList<>();
	static ArrayList<Map<String, Double>>eachPosRssList=new ArrayList<Map<String, Double>>();/***一个map存放一个点的信号向量***/
	static ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<Map<String, Integer>>();
	static ArrayList <String> positionlist=new ArrayList<>();
	static ArrayList <Double> posXlist=new ArrayList<>();
	static ArrayList <Double> posYlist=new ArrayList<>();
	
	static ArrayList<Integer> randposindexlist=new ArrayList<Integer>();
	
	static Pattern rss_pattern=Pattern.compile(Constant.rss_regex);
	static Pattern ap_pattern=Pattern.compile(Constant.ap_regex);
	static Pattern pos_pattern=Pattern.compile(Constant.pos_regex);
	static Matcher ap_matcher;
	static Matcher pos_matcher;
	
	static int neglect_frequency=22;
	static int collecting_times=110;//每个采集点的采集次数
	static int density=130;//采集点的采集密度
	static double defaultRSS=-100.0;
	static double probability=0.0;
	
	public static void initFile(){
		File file=new File(Constant.OFF_PATH);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void initPosAndAP(){
		initFile();
		int i=0,j=0;
		try {
			String line,each_pos = null;
			while((line=br.readLine())!=null){//&& i<1400
				if(i>0&&i<13&&j==0){//
					ap_matcher=ap_pattern.matcher(line);
					while(ap_matcher.find()){
						aplist.add(ap_matcher.group(0));
					}
				}else if(i==13&&j==0){
					i=j=1;
				}else if(i%116==6){//每110行一开头 先得到当前位置 如（-23.5，-10.75）
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
				else{
					if(line.contains(Constant.fixedid)){
						String target=line.replace(Constant.fixedid, "");
						Matcher m=rss_pattern.matcher(target);
						while(m.find()){
							String each_ap="00:"+m.group(1);//m1 mac地址
							if(!aplist.contains(each_ap))
								aplist.add(each_ap);		
						}
					}
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("position: "+each_pos+" ************************");
		initFile();
	}
	
	public static void initAllRSS(){
		try {	
			String line;
			int i=0,j=0;//j防止到13行之后再次重置i
			int []count=new int [30];
			double []sum=new double[30];
			while((line=br.readLine())!=null ){
				
				if(i>0&&i<13&&j==0){
					
				}else if(i==13&&j==0){
					i=j=1;					}
				else if(i%116>=0 && i%116<=5){//进入格式 跳过头尾
					//System.out.println("ignore");//1-17 18-127 128 
				}
				else{
					//进入具体数据
					Map<String,Double> map=new HashMap<>();
					Map<String,Integer> mapc=new HashMap<>();																	
					String target=line.replace(Constant.fixedid, "");
					getEachRSS(target, count, sum);
							
					if((i+1)%116==0){
						calculateRSSData(count,sum,map,mapc,false);
					}
				}/***else结束 读完了一行***/
				i++;//行数加一
			}//end while 读取结束					
			System.out.println("offline  poslist:"+positionlist.size()+"  aplist:"+aplist.size());
			System.out.println("Offline data initialized.");
		}catch (FileNotFoundException e) {
				e.printStackTrace();
		}catch (IOException e) {
				e.printStackTrace();
		}
		initFile();
	}
	
	public static void getEachRSS(String target, int[]count, double []sum){
		Matcher m=rss_pattern.matcher(target);//m1 mac地址  m2信号强度
		while(m.find()){/***读取某一行的有效信息***/
			String each_ap="00:"+m.group(1);
				
			int k=aplist.indexOf(each_ap);
			sum[k]+=Double.valueOf(m.group(2));
			count[k]++;									
		}
	}
	
	public static void calculateRSSData(int []count, double []sum, Map<String,Double>map ,Map<String,Integer>mapc, boolean isRand){
		for(int k=0;k<aplist.size();k++){
			/****认为收到20次以下 则所收到的信号无效 count[i]=0即为missedAP  简单取平均****/
			double avgrss;
			if(isRand)
				avgrss=(count[k]==0 ? defaultRSS : sum[k]/count[k]);// defaultRSS默认设置为-100
			else 				
				avgrss=(count[k]<neglect_frequency ? defaultRSS : sum[k]/count[k]);// defaultRSS默认设置为-100

			map.put(aplist.get(k), avgrss);
			mapc.put(aplist.get(k), count[k]);
			if(avgrss!=-100.0);
				System.out.println(aplist.get(k)+" "+avgrss+" "+count[k]);
		}
		eachPosRssList.add(map);
		eachPosRssCountList.add(mapc);
		for(int p=0;p<30;p++){
			count[p]=0;
			sum[p]=0;
		}
	}
	
	public static void read() {
		initPosAndAP();
		try {	
			String line;
			int i=0,j=0;//j防止到13行之后再次重置i
			int []count=new int [30];
			double []sum=new double[30];
			while((line=br.readLine())!=null ){//&& i<1400
				if(i>0&&i<13&&j==0){//
					
				}else if(i==13&&j==0){
					i=j=1;					}
				else if(i%116>=0 && i%116<=5){//进入格式 跳过头尾
						//System.out.println("ignore");//1-17 18-127 128 
				}
				else{
					Map<String,Double> map=new HashMap<>();
					Map<String,Integer> mapc=new HashMap<>();//存放count																
					String target=line.replace(Constant.fixedid, "");
					getEachRSS(target, count, sum);	
					if((i+1)%116==0){
						calculateRSSData(count,sum,map,mapc,false);
					}
				}/***else结束 读完了一行***/
				i++;//行数加一
			}//end while 读取结束				
				
			System.out.println("Offline reading completed.");
			
		}catch (FileNotFoundException e) {
				e.printStackTrace();
		}catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	public static void randomRead(){
		initPosAndAP();
		try {
			Random rand=new Random();
			int start=0;
			String line;
			int i=0,j=0;//j防止到13行之后再次重置i
			int []count=new int [30];
			double []sum=new double[30];
			while((line=br.readLine())!=null ){
				
				if(i>0&&i<13&&j==0){
					
				}else if(i==13&&j==0){
					i=j=1;					}
				else if(i%116>=0 && i%116<=5){//进入格式 跳过头尾
					//System.out.println("ignore");//1-17 18-127 128 
				}
				else{
					//进入具体数据
					if(i%116==6){//开始读有效内容	
						start=rand.nextInt(105-collecting_times)+6;//随机生成开始取的位置  每个位置的起始不一样 上限可能是+10	
//						System.out.println("start:"+start+" **********************");
					}
					if(i%116>=start && i%116<start+collecting_times){
						String target=line.replace(Constant.fixedid, "");
						getEachRSS(target, count, sum);
					}
					Map<String,Double> map=new HashMap<>();
					Map<String,Integer> mapc=new HashMap<>();																	
					
					if((i+1)%116==0){
						calculateRSSData(count,sum,map,mapc,true);
					}
				}/***else结束 读完了一行***/
				i++;//行数加一
			}//end while 读取结束				
				
		}catch (FileNotFoundException e) {
				e.printStackTrace();
		}catch (IOException e) {
				e.printStackTrace();
		}
		initFile();		
				
		System.out.println("Random offline reading completed.");
	}
	
	static ArrayList<Map<String, Double>>densityRssList=new ArrayList<Map<String, Double>>();
	public static void createPosWithDensity(){
		Random rand=new Random();
		
		int a=rand.nextInt(130);
		randposindexlist.add(a);//存放随机生成位置在总的poslist中的下标
		for(int i=0;i<density-1;i++){
			while(randposindexlist.contains(a))
				a=rand.nextInt(130);
			randposindexlist.add(a);//获取若干个互不相同的随机数
		}
		
		Collections.sort(randposindexlist);
		for(int i=0;i<randposindexlist.size();i++){
			int index=randposindexlist.get(i);
			//System.out.println(i+" "+positionlist.get(index));//输出
			densityRssList.add(eachPosRssList.get(index));
		}
		
	}
	
	static ArrayList<Map<String, Double>>centerPointsRssList=new ArrayList<Map<String, Double>>();
	static ArrayList<Double>centerXList=new ArrayList<>();
	static ArrayList<Double>centerYList=new ArrayList<>();
	public static void buildCenterPointsInfo(){	
		for(int j=0;j<positionlist.size();j++){
			double x=posXlist.get(j);
			double y=posYlist.get(j);
			ArrayList<Integer> nlist=new ArrayList<>();
			Map<String,Double>map;
			double []rsssum=new double[aplist.size()];
			int []cnt=new int[aplist.size()];
			map=eachPosRssList.get(j);
			for(int a=0;a<aplist.size();a++){
				rsssum[a]=map.get(aplist.get(a));
				if(map.get(aplist.get(a))!=-100.0){
					cnt[a]++;
				}
			}
			for(int i=0;i<posXlist.size();i++){
				double nearx=posXlist.get(i);
				double neary=posYlist.get(i);
				if((nearx-x)*(nearx-x)+(neary-y)*(neary-y)<6 && (nearx-x)*(nearx-x)+(neary-y)*(neary-y)>0
						&& nearx-x>=0 && neary-y>=0)
					nlist.add(i);
			}
			if(nlist.size()==3){
				double sumx = posXlist.get(j),sumy = posYlist.get(j);
				for(int k:nlist){
					sumx+=posXlist.get(k);
					sumy+=posYlist.get(k);
					
					Map<String,Double>m = eachPosRssList.get(k);//相邻的三个相邻在总体中是第k个向量
					for(int c=0;c<aplist.size();c++){
						rsssum[c]+=m.get(aplist.get(c));
						if(m.get(aplist.get(c))!=-100.0){
							cnt[c]++;
						}
//						System.out.println(c+" "+rsssum[c]+" "+cnt[c]);
					}
				}
				System.out.println("center point:"+sumx/4+" "+sumy/4+"******************");
				for(int d=0;d<rsssum.length;d++)
					if(cnt[d]!=0)
						System.out.println(d+" "+rsssum[d]/4);
				centerXList.add(sumx/4);
				centerYList.add(sumy/4);
				Map<String,Double>cmap=new HashMap<>();
				for(int p=0;p<aplist.size();p++)
					cmap.put(aplist.get(p),rsssum[p]/4);				
			}
			
//			System.out.println(positionlist.get(j)+" size:"+nlist.size());
		}
	}
	
	static ArrayList<Map<String, Double>>eachPosQuaRssList=new ArrayList<Map<String, Double>>();//quality:-80 -85 -90
	public static void buildBetterRSS(double quality){		
		for(Map<String,Double>m: eachPosRssList){
			Map<String, Double>map = new HashMap<String, Double>();
			for(int i=0;i<aplist.size();i++){
				if(i==28) break;
				double rss=m.get(aplist.get(i));

				if(rss>quality){/**仅使用一定质量以上的RSS 同时每个map的维数要相同
				 					其他的都是null**/
					map.put(aplist.get(i), rss);
//					System.out.println(rss);
				}
			}
			eachPosQuaRssList.add(map);
		}
		
		for(int j=0;j<positionlist.size();j++){
			Map<String,Double>m=eachPosQuaRssList.get(j);
			for(int k=0;k<aplist.size();k++){
//				System.out.println(aplist.get(k)+":"+m.get(aplist.get(k)));
			}
		}
	}
	
	
	/**
	 * penalty 是rss记录的每个ap维度的不同权重
		比如ap_i结果很不稳定，那么定位结果就要少考虑ap_i的值。 penalty如果大，换言之ap维度的权重小，表示计算欧式距离时，该维度的距离要更小
		distance_0 = w_0*distance(ap_0,ap_i) distance^2 = sigma(distance_0^2,...,distance_m^2)	*/
	static ArrayList<Map<String, Double>>penaltyList=new ArrayList<Map<String, Double>>();
	public static void buildPenaltyList(){//只需要建立penaltylist
		for(int j=0;j<positionlist.size();j++){
//			System.out.println("position: "+positionlist.get(j)+"*******************");
			Map<String,Integer>m=eachPosRssCountList.get(j);
			Map<String,Double>map=new HashMap<>();
			
			for(int k=0;k<aplist.size()-1;k++){
				double t=m.get(aplist.get(k));
				if(t>=100)
					map.put(aplist.get(k), 1.0);
				else{
					double p=t/100;
					map.put(aplist.get(k), p);
//					System.out.println(1-p);
				}//定位的时候用
			}
			penaltyList.add(map);
		}
		System.out.println("penaltylist:"+penaltyList.size()+" is built");
	}
	
	public static void buildWeightRSSList(){
		initPosAndAP();
		try {	
			String line;
			int i=0,j=0;//j防止到13行之后再次重置i
			int []count=new int [30];
			double []sum=new double[30];
			
			int n=0;
			double []aptimes=new double[110];//每个向量中ap的个数
			ArrayList<Map<String, Double>>Rss110List=new ArrayList<Map<String, Double>>();
			
			while((line=br.readLine())!=null ){//&&i<140
				if(i>0&&i<13&&j==0);
				else if(i==13&&j==0) i=j=1;
				else if(i%116>=0 && i%116<=5);
				else{
					//每一次读取一个位置
					Map<String,Double> map=new HashMap<>();
					Map<String,Integer> mapc=new HashMap<>();
					
					String target=line.replace(Constant.fixedid, "");
					Matcher m=rss_pattern.matcher(target);//m1 mac地址  m2信号强度
					
					while(m.find()){/***开始读一个RSS向量 每循环一次读一个ap***/
						aptimes[n]++;//每一行的ap出现个数
						
						String each_ap="00:"+m.group(1);
						int k=aplist.indexOf(each_ap);
						count[k]++;
						map.put(each_ap, Double.valueOf(m.group(2)));
//						mapc.put(each_ap, count[k]);
					}
					n++;
					Rss110List.add(map);
					
					if((i+1)%116==0){//读完了一组数据
						double wtotal=0;
						for(double a:aptimes)
							wtotal+=a;
						double []rss=new double[aplist.size()];//分别记录每一个ap的rss
						
						Map<String,Double> wmap=new HashMap<>();//要加入weightlist的信号向量

						double []weight=new double[110];
						for(int c=0;c<110;c++){
							weight[c]=aptimes[c]/wtotal;
							//System.out.println("weight "+c+" "+weight[c]);
						}
						
						for(int c=0;c<110;c++){
							Map<String,Double> mapr=Rss110List.get(c);
							for(int d=0;d<aplist.size();d++){
								if(mapr.get(aplist.get(d))!=null){
									double aprss=mapr.get(aplist.get(d));
									rss[d]+=weight[c]*aprss;
								}else{
									rss[d]+=weight[c]*(-100.0);
								}
								
							}
						}
						
						for(int d=0;d<aplist.size();d++){
							wmap.put(aplist.get(d), rss[d]);
						}
						eachPosRssList.add(wmap);
						
						n=0;
						for(int p=0;p<110;p++){
							aptimes[p]=0;
							if(p<aplist.size())
								rss[p]=0;
						}				
					}
				}
				i++;
			}			
				
			System.out.println("Weight RSS building completed.");
			
		}catch (FileNotFoundException e) {
				e.printStackTrace();
		}catch (IOException e) {
				e.printStackTrace();
		}		
		for(int i=0;i<positionlist.size();i++){
			System.out.println("position: "+positionlist.get(i)+"*******************");
			Map<String,Double>m=eachPosRssList.get(i);
			for(int j=0;j<aplist.size();j++){
				if(m.get(aplist.get(j))+100<0.1);
				else
					System.out.println(aplist.get(j)+":"+m.get(aplist.get(j)));
			}	
		}
		initFile();
	}
	
	public static double caculateProbility(){
		double total=0;//130个点 28个ap 每个点110次*110
		double invalid=0;
		for(int i=0;i<eachPosRssCountList.size();i++){
			Map<String,Integer>mc=eachPosRssCountList.get(i);
			for(int j=0;j<aplist.size();j++){
				int cnt=mc.get(aplist.get(j));		
				if(cnt!=0){
					invalid+=110-cnt;
					total+=110;
				}
			}
		}
		probability=invalid/total;
		System.out.println("valid:"+invalid+" missed probability:"+probability);
		return probability;
	}
	
	public static void main(String[] args) {
		initFile();
		initPosAndAP();
//		randomRead();
		//readOff();
//		Double p=caculateProbility();
//		buildBetterRSS(-80.0);
//		buildPenaltyList(); 
//		buildCenterPointsInfo();
//		buildWeightRSSList();
				
//		displayRSS();

		//setCollectTimes(20);
		//setNeglectFrequency(20);
		//setDefaultRSS(-100.0);
		//setDensity(30);//50-130
		//createPosWithDensity();
		//read();
	}
	
	public static void displayRSS(){//只输出eachPosRssList的内容
		for(int i=0;i<positionlist.size();i++){
			System.out.println("position: "+positionlist.get(i)+"*******************");
			Map<String,Double>m=eachPosRssList.get(i);
			Map<String,Integer>mc=eachPosRssCountList.get(i);
			for(int j=0;j<aplist.size();j++){
				System.out.println(aplist.get(j)+":"+m.get(aplist.get(j))+" "+mc.get(aplist.get(j)));
			}	
		}
	}
	
	public static void setNeglectFrequency(int f){
		neglect_frequency=f;
	}
	
	public static void setDensity(int dens){
		density=dens;
	}
	
	public static void setCollectTimes(int times){
		collecting_times=times;
	}
	
	public static void setDefaultRSS(double rss){
		defaultRSS=rss;
	}
	
	public static void readOff(){//给其他类调用的接口
		initFile();
		initPosAndAP();
		initAllRSS();
	}
	
}
