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
import java.util.regex.Matcher;

public class OnlineData {
	/**
	 * ����֯��ʽֻ����pc�������ݼ��� ʵ��Ӧ��Ӧ�ø�Ϊ���ε�
	 */	
	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27

	ArrayList<ArrayList<Map<String, Double>>> onRssList=new ArrayList<>(46);//ÿһ����ÿһ�βɼ�ÿһ��ap
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList <Integer[]> apVectorlist=new ArrayList<>();/**0-n������ɵ������¼ap�Ƿ����*/
	List <List<Map<Double, Integer>>> rssVectorlist=new ArrayList<>(46);/**ÿ����ÿ��ap���ֵ�rss�Լ���Ӧ�Ĵ��� 46-26-n*/

	//Ӧ���ڶ�ȡ�ļ�֮�������
	public double[] XArr=Constant.ON_X_ARR;
	public double[] YArr=Constant.ON_Y_ARR;
	
	public int number=46;
	private double availableRSS=-80.0;	
	
	public OnlineData(String path) {
		initBr(path);
		initRSSData();
//		Tools.displayAllRSS(onRssList, aplist);
		calculateOnAvgRss();
		buildRssVectorList();
	}

	/**
	 * eachTimeRss��ֻput������⵽��ap�Լ���Ӧ��rss
	 */
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[XArr.length];//0-1����
			ArrayList<Double> rssVector=new ArrayList<>();
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
						
						if(aplist.indexOf(each_ap)>=0)//ע����offline֮���ap
							apVector[aplist.indexOf(each_ap)]++;
						
						if(!rssVector.contains(rss))
							rssVector.add(rss);
					}
					eachPosRss.add(eachTimeRss);	
				}
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(46);
					apVector=new Integer[XArr.length];
					Tools.cleanArr(apVector);;
					rssVector=new ArrayList<>();
				}
				Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					onRssList.add(eachPosRss);
					apVectorlist.add(apVector);
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void calculateOnAvgRss(){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(ArrayList<Map<String,Double>> eachpos : onRssList){
//			System.out.println(Constant.ON_POS_ARR[t++]);
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
					double avg= sum[j]/count[j];
					eachavgrss.put(aplist.get(j), avg);
//					System.out.println(aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	/**
	 * ��һЩap��ͬһ�����ظ����� �������ݻ���
	 */
	public void buildRssVectorList(){
		for(ArrayList<Map<String,Double>> onePosRss:onRssList){//46��
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
	
	public static void main(String[] args) {
		OnlineData on=new OnlineData(Constant.ON_PATH);			
//		Tools.showList(on.aplist);
//		showApVectorList(on.apVectorlist);
		showRssVectorList(on.rssVectorlist);
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
