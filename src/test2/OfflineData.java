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
	 * ÿһ����ÿһ�βɼ�ÿһ��ap �洢�������� �ı��㷨ʱ ���������ȡ �����µ�avgRssList
	*/
	List<List<Map<String, Double>>> allRssList=new ArrayList<>(130);//����ֻ��ʼ��һ��
	List <Integer[]> apVectorlist=new ArrayList<>();/**0-n������ɵ������¼ap�Ƿ�������ɴ�*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**��ƽ��ֵ����� ��¼ÿ����ÿ��ap�Ŀ��Ŷ�(1-P(miss))*/
	List<Map<String, Double>> avgRssList=new ArrayList<>();/**ƽ��ֵ*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>();/**ÿ����ÿ��ap���ֵ�rss�Լ���Ӧ�Ĵ��� 130*density-26-n*/
	
	//Ӧ���ڶ�ȡ�ļ�֮�������
	
	
	public Point[] points=new Point[Constant.OFF_POS_ARR.length];
	
	public int number=130;
	
	private int neglect_frequency=0;//KNN���?ƫ����С  22
	private double posDensity;//�ɼ����ܶ�
	private double timeDensity;//�ɼ������ܶ�
	private static double defaultRSS;//Ĭ��missed APΪ-100
	private static double availableRSS=-1000;//��ÿ�εĿ���ֵ����ƽ��ֵ���ã�
	
	public static class Options{
		double pDensity=1.0;
		double tDensity=1.0;//�ɼ������ܶ�
		
		int neglect_frequency=0;//���˴����µ�ap rss��Ϊdefault
		double defaultRSS=-95.0;//Ĭ��missed APΪ-100
		double availableRSS=-1000.0;//��ʹ�ô��ڸ�ֵ��RSS Ĭ��Ϊ��С��Ϊrss������ ��ֵ��������������
		
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
		buildRssVectorList();//build����ȫ�������
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
			List<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[aplist.size()];//0-n����
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
					eachPosRss.add(eachTimeRss);//�����	
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
			if(pset.contains(allRssList.indexOf(maplist))){//130���е�һЩ���
				eachPosRss=new ArrayList<>();//��СΪ110*td
				
				poslist.add(Constant.OFF_POS_ARR[allRssList.indexOf(maplist)]);
				
				for(Map<String,Double> map:maplist){
					if(tset.contains(maplist.indexOf(map))){//110���е�һЩ��� �������� apVector/rssVectorɶ��Ҳ�ڱ�
						eachTimeRss=new HashMap<>();
						eachPosRss.add(eachTimeRss);//�����	
						
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
					if(rss!=null){//һ��Ҫ��
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
//					if(availableRSS<=-100){//ʹ����availableRSS Ƶ��һ�㲻��̫��
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);
//					} 
//					else {//ʹ��availableRSSb ���ƽ��ֵ-81 ʵ��79 ���ͻ�ܴ�
//						if(avg<-availableRSS) continue;
//						else eachavgrss.put(aplist.get(j), avg);
//					}
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRssList){//130��
			List<Map<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27��
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
	 * ֱ�Ӷ�apVector���� ͨ������missed AP�ĸ��ʼ���ÿһ����ÿһ��ap�Ŀ��Ŷ�
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
	 * ���ȿ��Թ���һ����Ԫ�飬ÿ����Ԫ���Ӧһ��centerpoint Ȼ���ټ���
	 *  (-23.5,-10.75) (-23.5,-9.25)
		(-22.0,-10.75) (-22.0,-9.25)
		(-20.5,-10.75) (-20.5,-9.25)
		(-19.0,-10.75) (-19.0,-9.25)
	 */
	public void buildCenterPointsInfo(){
		Set<Point> centers=new HashSet<>();
		for(Point point:points){
			//����ÿһ���� ������������3���� ���Ƿ񹹳ɾ��� ���򿴼�����������ĵ��Ƿ��Ѿ���set��
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
