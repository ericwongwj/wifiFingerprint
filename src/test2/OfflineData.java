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

	/**
	 * ÿһ����ÿһ�βɼ�ÿһ��ap �洢�������� �ı��㷨ʱ ���������ȡ �����µ�avgRssList
	*/
	List<List<Map<String, Double>>> allRssList=new ArrayList<>(130);//����ֻ��ʼ��һ��
	List <Integer[]> apVectorlist=new ArrayList<>();/**0-n������ɵ������¼ap�Ƿ�������ɴ�*/

	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();/**ƽ��ֵ*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>();/**ÿ����ÿ��ap���ֵ�rss�Լ���Ӧ�Ĵ��� 130*density-26-n*/
	
	//Ӧ���ڶ�ȡ�ļ�֮�������
	public double[] XArr=Constant.OFF_X_ARR;
	public double[] YArr=Constant.OFF_Y_ARR;
	
	public int number=130;
	
	private int neglect_frequency=22;//KNN���?ƫ����С
	private double posDensity;//�ɼ����ܶ�
	private double timeDensity;//�ɼ������ܶ�
	private static double defaultRSS;//Ĭ��missed APΪ-100
	private static double availableRSS;//��ʹ�ô��ڸ�ֵ��RSS
	
	public static class Options{
		double pDensity=1.0;
		double tDensity=1.0;//�ɼ������ܶ�
		double defaultRSS=-100.0;//Ĭ��missed APΪ-100
		double availableRSS=-80.0;//��ʹ�ô��ڸ�ֵ��RSS
		
		public Options() {}
		public Options(int what, double value) {
			switch(what){
			case 0:
				pDensity=value;
				break;
			case 1:
				tDensity=value;
				break;
			case 2:
				defaultRSS=value;
				break;
			case 3:
				availableRSS=value;
				break;
			default:
				System.out.println("wrong");
			}
		}
		public Options(double p, double t, double deRss, double avRss) {
			pDensity=p;
			tDensity=t;
			defaultRSS=deRss;
			availableRSS=avRss;
		}
	} 
	
	public static void main(String[] args) {
		Options ops=new Options(0,0.2);
		OfflineData offline=new OfflineData(Constant.OFF_PATH,ops);
		
//		Tools.displayAllRSS(offline.offRssList, offline.aplist);
//		showMapList(offline.avgRssList);
//		Tools.showList(aplist);
//		Tools.showList(poslist);
//		showApVectorList(offline.apVectorlist);
//		showRssVectorList(offline.rssVectorlist);
	}

	public OfflineData(String path, Options options) {
		posDensity=options.pDensity;
		timeDensity=options.tDensity;
		defaultRSS=options.defaultRSS;
		availableRSS=options.availableRSS;
		initBufferReader(path);
		initRSSData();
		buildRssVectorList();//build����ȫ�������

//		if(options.po)
		initRandPosAndTimeRss(0.2,1.0);
//		initRandTimeRss(timeD, 110);
//		generateAvgRss();

	}
	
	
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[XArr.length];//0-1����
			while((line=br.readLine())!=null ){
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					while(rm.find()){//ֻ�洢���ֵ�ap
						String each_ap="00:"+rm.group(1);//m1 mac��ַ m2��rss
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
					allRssList.add(eachPosRss);
					apVectorlist.add(apVector);
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//TODO:
	public List<List<Map<String, Double>>> initRandPosAndTimeRss(double pd,double td){
		List<List<Map<String, Double>>> randRssList=new ArrayList<>();
		posDensity=pd;
		timeDensity=td;
		TreeSet<Integer> pset=Tools.generateRandArr(pd, 130);
		TreeSet<Integer> tset=Tools.generateRandArr(td, 110);
		List<Double> tlist=new ArrayList<>();
		poslist=new ArrayList<>();
		for(List<Map<String, Double>> maplist:allRssList){
			if(pset.contains(allRssList.indexOf(maplist))){//130���е�һЩ���
				poslist.add(Constant.OFF_POS_ARR[allRssList.indexOf(maplist)]);
				for(Map<String,Double> map:maplist){
					if(tset.contains(maplist.indexOf(map))){//110���е�һЩ���
						for(String ap:map.keySet())
							tlist.add(map.get(ap));
						System.out.println(tlist.size());
						tlist=new ArrayList<>();
					}
				}
			}
		}
		return randRssList;
	}
	
	private void generateAvgRss(List<List<Map<String, Double>>> rssList){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(List<Map<String, Double>> eachpos : rssList){
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
					double avg=(count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);//��⵽�Ĵ���̫��
					eachavgrss.put(aplist.get(j), avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRssList){//130��
			List<Map<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27��
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
