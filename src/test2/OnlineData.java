package test2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;

import test2.OfflineData.Options;

public class OnlineData {
	
	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27

	/**
	 * ÿһ����ÿһ�βɼ�ÿһ��ap �洢�������� �ı��㷨ʱ ���������ȡ �����µ�avgRssList
	*/
	List<List<Map<String, Double>>> allRssList=new ArrayList<>(46);//����ֻ��ʼ��һ��
	List <Integer[]> apVectorlist=new ArrayList<>(46);/**0-n������ɵ������¼ap�Ƿ�������ɴ�*/

	List<Map<String, Double>> avgRssList=new ArrayList<>();/**ƽ��ֵ*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>();/**ÿ����ÿ��ap���ֵ�rss�Լ���Ӧ�Ĵ��� 130*density-26-n*/
	
	//Ӧ���ڶ�ȡ�ļ�֮�������
//	public double[] XArr=Constant.ON_X_ARR;
//	public double[] YArr=Constant.ON_Y_ARR;
	
	public Point[] points=new Point[Constant.ON_POS_ARR.length];
		
	public int number=46;
	private double availableRSS=-80.0;	
	private double timeDensity=1.0;//�ɼ������ܶ�
	
	public static void main(String[] args) {
		OnlineData on=new OnlineData(Constant.ON_PATH,1.0);			
		showApVectorList(on.apVectorlist);
//		showRssVectorList(on.rssVectorlist);		
//		Tools.displayAllRSS(on.allRssList, aplist);
//		showMapList(on.avgRssList);
	}
	
	
	public OnlineData(String path, double td) {
		timeDensity=td;
		initPoints();
		initBr(path);
		initRSSData();
		buildRssVectorList();//build����ȫ�������

		if(timeDensity==1)
			generateAvgRss(allRssList);
		else if(timeDensity<1){
			List<List<Map<String, Double>>> tempRss=initRandTimeRss(timeDensity,timeDensity);
			generateAvgRss(tempRss);
		}
	}
	
	public void initPoints(){
		for(int i=0;i<Constant.ON_X_ARR.length;i++){
			points[i]=new Point(Constant.ON_X_ARR[i], Constant.ON_Y_ARR[i]);
		}
	}
	
	/**
	 * eachTimeRss��ֻput������⵽��ap�Լ���Ӧ��rss
	 */
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[aplist.size()];//0-1����
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
						
						if(aplist.contains(each_ap))//��Ϊ�����²����ڵ�ap
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
	
	public List<List<Map<String, Double>>> initRandTimeRss(double pd,double td){
		List<List<Map<String, Double>>> randRssList=new ArrayList<>();
		TreeSet<Integer> tset=Tools.generateRandArr(td, 110);

		List<Map<String, Double>> eachPosRss = null;
		Map<String,Double> eachTimeRss = null;
		for(List<Map<String, Double>> maplist:allRssList){
			eachPosRss=new ArrayList<>();//��СΪ110*td
			
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
					double avg=sum[j]/count[j];//��⵽�Ĵ���̫�� (count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);
					eachavgrss.put(aplist.get(j), avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	/**
	 * ��һЩap��ͬһ�����ظ����� �������ݻ���
	 */
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
	

	public static void showMapList(List<Map<String,Double>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(Map<String,Double> map:list){
			System.out.println(Constant.ON_POS_ARR[i++]);
			for(String ap:map.keySet())
				System.out.println(ap+" "+map.get(ap));
		}
	}
	
	public static void showApVectorList(List<Integer[]> list){
		int i=0;
		for(Integer[] arr:list){
			System.out.println(Constant.ON_POS_ARR[i++]);
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
			System.out.println(Constant.ON_POS_ARR[i++]+"  ****************************************");
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
	
	private void initBr(String path){
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
