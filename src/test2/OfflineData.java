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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineData {

	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27
	public static List<String> poslist= Arrays.asList(Constant.OFF_POS_ARR);

	/**
	 * 每一个点每一次采集每一个ap 存储所有数据 改变算法时 在这上面读取 产生新的avgRssList
	*/
	List<List<Map<String, Double>>> allRssList=new ArrayList<>(130);//二者只初始化一次
	List <Integer[]> apVectorlist=new ArrayList<>();/**0-n向量组成的数组记录ap是否出现若干次*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**和平均值相对于 记录每个点每个ap的可信度(1-P(miss))*/
	List<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>();/**每个点每个ap出现的rss以及对应的次数 130*density-26-n*/
	
	//应当在读取文件之后再填充
	
	
	public Point[] points=new Point[Constant.OFF_POS_ARR.length];
	
	public int number=130;
	
	private int neglect_frequency=0;//KNN设成?偏差最小  22
	private double posDensity;//采集点密度
	private double timeDensity;//采集次数密度
	private static double defaultRSS;//默认missed AP为-100
	private static double availableRSS=-1000;//是每次的可用值还是平均值可用？
	
	public static class Options{
		double pDensity=1.0;
		double tDensity=1.0;//采集次数密度
		
		int neglect_frequency=0;//将此次数下的ap rss置为default
		double defaultRSS=-95.0;//默认missed AP为-100
		double availableRSS=-1000.0;//仅使用大于该值的RSS 默设为极小认为rss都可用 该值和以上两个互斥
		
		public Options() {}
		public Options(String what, double value) {
			switch(what){
			case "pos":
				pDensity=value;
				break;
			case "time":
				tDensity=value;
				break;
			case "neglect":
				neglect_frequency=(int)value;
				break;
			case "default":
				defaultRSS=value;
				break;
			case "available":
				availableRSS=value;
				break;
			default:
				System.out.println("wrong");
			}
		}
		public Options(double p, double t, int nf,double deRss, double avRss) {
			pDensity=p;
			tDensity=t;
			neglect_frequency=nf;
			defaultRSS=deRss;
			availableRSS=avRss;
		}
	} 
	
	public static void main(String[] args) {
		Options ops=new Options();
//		ops.pDensity=0.1;
//		ops.tDensity=0.1;
		ops.availableRSS=-70;
		OfflineData offline=new OfflineData(Constant.OFF_PATH,ops);
		
//		Tools.displayAllRSS(offline.allRssList, offline.aplist);
//		showMapList(offline.avgRssList);
//		showMapList(offline.penaltyList);
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
		neglect_frequency=options.neglect_frequency;
		initPoints();
		initBufferReader(path);
		initRSSData();
		buildRssVectorList();//build的是全部的情况
		buildPenaltyList();

		if(posDensity==1 && timeDensity==1)
			generateAvgRss(allRssList);
		else if(posDensity<1 || timeDensity<1){
			List<List<Map<String, Double>>> tempRss=initRandPosAndTimeRss(posDensity,timeDensity);
			generateAvgRss(tempRss);
		}else System.out.println("Wrong density!");
	}
	
	public void initPoints(){
		for(int i=0;i<Constant.OFF_X_ARR.length;i++){
			points[i]=new Point(Constant.OFF_X_ARR[i], Constant.OFF_Y_ARR[i]);
//			System.out.println(points[i]);
		}
	}
	
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[aplist.size()];//0-n向量
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
					eachPosRss.add(eachTimeRss);//先添加	
				}
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(110);
					apVector=new Integer[aplist.size()];
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
	
	
	
	public List<List<Map<String, Double>>> initRandPosAndTimeRss(double pd,double td){
		List<List<Map<String, Double>>> randRssList=new ArrayList<>();
		TreeSet<Integer> pset=Tools.generateRandArr(pd, 130);
		TreeSet<Integer> tset=Tools.generateRandArr(td, 110);

		poslist=new ArrayList<>();
		List<Map<String, Double>> eachPosRss = null;
		Map<String,Double> eachTimeRss = null;
		for(List<Map<String, Double>> maplist:allRssList){
			if(pset.contains(allRssList.indexOf(maplist))){//130次中的一些随机
				eachPosRss=new ArrayList<>();//大小为110*td
				
				poslist.add(Constant.OFF_POS_ARR[allRssList.indexOf(maplist)]);
				
				for(Map<String,Double> map:maplist){
					if(tset.contains(maplist.indexOf(map))){//110次中的一些随机 次数减少 apVector/rssVector啥的也在变
						eachTimeRss=new HashMap<>();
						eachPosRss.add(eachTimeRss);//先添加	
						
						for(String ap:map.keySet())
							eachTimeRss.put(ap, map.get(ap));
					}
				}
				randRssList.add(eachPosRss);
			}
		}
		return randRssList;
	}
	
	private void generateAvgRss(List<List<Map<String, Double>>> rssList){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];
		for(List<Map<String, Double>> eachpos : rssList){
			Map<String,Double> eachavgrss=new HashMap<>();
			avgRssList.add(eachavgrss);
			for(Map<String,Double> eachTimeRss : eachpos){
				for(int i=0;i<aplist.size();i++){
					String ap=aplist.get(i);
					Double rss=eachTimeRss.get(ap);
					if(rss!=null){//一定要加
						if(rss>availableRSS){
						sum[i]+=eachTimeRss.get(ap);
						count[i]++;
						}
					}
				}
			}	
			for(int j=0;j<aplist.size();j++){
				if(count[j]!=0){
					double avg = sum[j]/count[j];
//					if(availableRSS<=-100){//使用了availableRSS 频率一般不会太少
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);
//					} 
//					else {//使用availableRSSb 如果平均值-81 实际79 误差就会很大
//						if(avg<-availableRSS) continue;
//						else eachavgrss.put(aplist.get(j), avg);
//					}
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRssList){//130次
			List<Map<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27次
				Map<Double, Integer> oneApRss=new TreeMap<>();
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
	
	/**
	 * 直接对apVector处理 通过计算missed AP的概率计算每一个点每一个ap的可信度
	 */
	public void buildPenaltyList(){
		for(Integer[] apVector:apVectorlist){
			Map<String,Double> map=new HashMap<>();
			for(int i=0;i<apVector.length;i++){
				double prob=apVector[i]/110.0;
				map.put(aplist.get(i), prob);
			}
			penaltyList.add(map);
		}
	}

	/**
	 * 首先可以构建一个四元组，每个四元组对应一个centerpoint 然后再计算
	 *  (-23.5,-10.75) (-23.5,-9.25)
		(-22.0,-10.75) (-22.0,-9.25)
		(-20.5,-10.75) (-20.5,-9.25)
		(-19.0,-10.75) (-19.0,-9.25)
	 */
	public void buildCenterPointsInfo(){
		Set<Point> centers=new HashSet<>();
		for(Point point:points){
			//遍历每一个点 计算离点最近的3个点 看是否构成矩形 是则看计算出来的中心点是否已经在set中
			Point[] nearby=new Point[3];
			double x=(nearby[0].x+nearby[1].x+nearby[2].x+point.x)/4;
			double y=(nearby[0].y+nearby[1].y+nearby[2].y+point.y)/4;
			boolean isContains=false;
			for(Point center:centers){
				if(center.x==x&&center.y==y){
					isContains=true;
					break;
				}
			}
			if(!isContains)
				centers.add(new Point(x,y));
		}
	}
	
	public void buildBetterRSS(double quality){	}
	
	
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
