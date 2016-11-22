package test2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineData {

	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27

	public static List<String> poslist= Arrays.asList(Constant.OFF_POS_ARR);
	ArrayList<ArrayList<Map<String, Double>>> offRssList=new ArrayList<>(130);/**每一个点每一次采集每一个ap*/
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	ArrayList <Integer[]> apVectorlist=new ArrayList<>();/**0-n向量组成的数组记录ap是否出现若干次*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>(130);/**每个点每个ap出现的rss以及对应的次数 130-26-n*/
	
	//应当在读取文件之后再填充
	public double[] XArr=Constant.OFF_X_ARR;
	public double[] YArr=Constant.OFF_Y_ARR;
	
	public int number=130;
	
	private int neglect_frequency=22;//KNN设成?偏差最小
	private double posDensity=1.0;//采集点密度
	private double timeDensity=1.0;//采集次数密度
	private static double defaultRSS=-100.0;//默认missed AP为-100
	private static double availableRSS=-80.0;//仅使用大于该值的RSS
	
	public static void main(String[] args) {
//		OfflineData offline=new OfflineData(Constant.OFF_PATH);
		OfflineData offline=new OfflineData(Constant.OFF_PATH,0.2,1.0);
//		Tools.displayAllRSS(offline.offRssList, offline.aplist);
		showMapList(offline.avgRssList);
//		Tools.showList(aplist);
		Tools.showList(poslist);
//		showApVectorList(offline.apVectorlist);
//		showRssVectorList(offline.rssVectorlist);
	}

	public OfflineData(String path) {
		initBufferReader(path);
		initRSSData();
		calculateOffAvgRss();
		buildRssVectorList();
	}
	
	public OfflineData(String path, double posD, double timeD) {
		initBufferReader(path);
		posDensity=posD;
		timeDensity=timeD;
		initRandPosRss(posD,Constant.OFF_POS_ARR.length);
//		initRandTimeRss(timeD, 110);
		calculateOffAvgRss();
//		buildRssVectorList();
	}
	
	public void initRSSData(){
		try {
			String line;
			int cnt=0,times=0;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[XArr.length];//0-1向量
			while((line=br.readLine())!=null ){
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					while(rm.find()){//只存储出现的ap
						String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
						double rss=Double.valueOf(rm.group(2));
						eachTimeRss.put(each_ap, rss);
						
						apVector[aplist.indexOf(each_ap)]++;
					}
					eachPosRss.add(eachTimeRss);	
				}
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(110);
					apVector=new Integer[XArr.length];
					Tools.cleanArr(apVector);;
				}
				Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					offRssList.add(eachPosRss);
					apVectorlist.add(apVector);
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateOffAvgRss(){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(ArrayList<Map<String,Double>> eachpos : offRssList){
//			System.out.println(Constant.OFF_POS_ARR[t++]);
			Map<String,Double> eachavgrss=new HashMap<>();
			avgRssList.add(eachavgrss);
			for(Map<String,Double> eachtime : eachpos){
				for(int i=0;i<aplist.size();i++){
					String ap=aplist.get(i);
					if(eachtime.get(ap)!=null){
						sum[i]+=eachtime.get(ap);
						count[i]++;
					}
				}
			}
			for(int j=0;j<aplist.size();j++){
				if(count[j]!=0){
					double avg=(count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);//检测到的次数太少
					eachavgrss.put(aplist.get(j), avg);
//					System.out.println(aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(ArrayList<Map<String,Double>> onePosRss:offRssList){//130次
			List<Map<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27次
				Map<Double, Integer> oneApRss=new HashMap<>();
				for(Map<String,Double> oneTimeRss:onePosRss){
					
					if(oneTimeRss.get(ap)!=null){
						double rss=oneTimeRss.get(ap);
						if(oneApRss.containsKey(rss)){
							int times=oneApRss.get(rss);
							oneApRss.put(rss, ++times);
						}else
							oneApRss.put(rss, 1);
					}
					
				}
				apRssList.add(oneApRss);
			}
			rssVectorlist.add(apRssList);
		}
	}
	
	public void initRandPosRss(double d,int bound){
		posDensity=d;
		TreeSet<Integer> set=Tools.generateRandArr(d, bound);
		poslist=new ArrayList<>();
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[XArr.length];//0-1向量
			boolean isInSet=false;
			int cnt=0;
			while((line=br.readLine())!=null ){
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(110);
					apVector=new Integer[XArr.length];
					Tools.cleanArr(apVector);
					cnt++;
					if(set.contains(cnt)){
						isInSet=true;
						poslist.add(Constant.OFF_POS_ARR[cnt-1]);
					}
					else isInSet=false;
				}
				if(isInSet){
					Matcher newline_matcher=Constant.newline_pattern.matcher(line);
					if(newline_matcher.find()&&line.contains(Constant.fixedid)){
						eachTimeRss=new HashMap<>();
						String target=line.replace(Constant.fixedid, "");
						Matcher rm=Constant.rss_pattern.matcher(target);
						while(rm.find()){//只存储出现的ap
							String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
							double rss=Double.valueOf(rm.group(2));
							eachTimeRss.put(each_ap, rss);
							
							apVector[aplist.indexOf(each_ap)]++;
						}
						eachPosRss.add(eachTimeRss);	
					}
					
					Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
					if(endtime_matcher.find()){
						offRssList.add(eachPosRss);
						apVectorlist.add(apVector);
					}
				}
				
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initRandTimeRss(double d,int bound){
		timeDensity=d;
		TreeSet<Integer> set=Tools.generateRandArr(d, bound);
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[XArr.length];//0-1向量
			boolean isInSet=false;
			int cnt=0;
			while((line=br.readLine())!=null ){
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					set=Tools.generateRandArr(d, bound);
					cnt=0;
					
					eachPosRss=new ArrayList<>(110);
					apVector=new Integer[XArr.length];
					Tools.cleanArr(apVector);	
				}
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					
					cnt++;
					if(set.contains(cnt)){
						isInSet=true;
						poslist.add(Constant.OFF_POS_ARR[cnt-1]);
					}
					
					else isInSet=false;
					while(rm.find()){//只存储出现的ap
						String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
						double rss=Double.valueOf(rm.group(2));
						eachTimeRss.put(each_ap, rss);
						
						apVector[aplist.indexOf(each_ap)]++;
					}
					eachPosRss.add(eachTimeRss);	
				}
				
				Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					offRssList.add(eachPosRss);
					apVectorlist.add(apVector);
				}	
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void buildCenterPointsInfo(){	}
	
	public void buildBetterRSS(double quality){	}
	
	public void buildPenaltyList(){}
	
	public void buildWeightRSSList(){}
	
	public double caculateProbility(){return 0;}
	
	public static void showMapList(List<Map<String,Double>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(Map<String,Double> map:list){
			System.out.println(poslist.get(i++));
			for(String ap:map.keySet())
				System.out.println(ap+" "+map.get(ap));
		}
	}
	
	public static void showApVectorList(List<Integer[]> list){
		int i=0;
		for(Integer[] arr:list){
			System.out.println(Constant.OFF_POS_ARR[i++]);
			int k=0;
			for(int j=0;j<arr.length;j++){
				if(arr[j]!=0){
					System.out.println(aplist.get(j)+" "+arr[j]);
					k++;
				}
			}
			System.out.println("ap number at this point is "+k);
		}
	}
	
	public static void showRssVectorList(List <List<Map<Double, Integer>>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(List<Map<Double, Integer>> apRssList:list){
			System.out.println(Constant.OFF_POS_ARR[i++]+"  ****************************************");
			int j=0;
			for(Map<Double, Integer> map:apRssList){
				if(!map.isEmpty()){
					System.out.println(aplist.get(j));
					int total=0;
					for(double rss:map.keySet()){
						total+=map.get(rss);
						System.out.println("rss="+rss+" times="+map.get(rss));
					}
					System.out.println("total times="+total);
				}				
				j++;
			}
		}
	}
	
	private void initBufferReader(String path){
		file=new File(path);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
