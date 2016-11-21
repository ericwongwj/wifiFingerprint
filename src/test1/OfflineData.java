package test1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineData {

	private File file;
	private BufferedReader br;

	ArrayList<ArrayList<Map<String, Double>>> offRssList=new ArrayList<>(130);//每一个点每一次采集每一个ap
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<Map<String, Integer>>();
	List<String> aplist= Arrays.asList(Constant.AP_ARR);//27
	
	//应当在读取文件之后再填充
	public double[] XArr=Constant.OFF_X_ARR;
	public double[] YArr=Constant.OFF_Y_ARR;
	
	public int number=130;
	
	private int neglect_frequency=25;//KNN设成25偏差最小
	private int collecting_times=8;//每个采集点的采集次数
	private static double defaultRSS=-100.0;
	private static double availableRSS=-80.0;

	public void setNeglectFrequency(int f){
		neglect_frequency=f;
	}
	
	public OfflineData(String path) {
		initBufferReader(path);
		initRSSData();
//		Tools.displayAllRSS(offRssList, aplist);
//		calculateOffAvgRss();
	}
	
	public void initRSSData(){
		try {
			String line;
			int cnt=0,times=0;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;//大小110（次）
			while((line=br.readLine())!=null ){
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					while(rm.find()){//只存储出现的ap
						String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
						eachTimeRss.put(each_ap, Double.valueOf(rm.group(2)));		
					}
					eachPosRss.add(eachTimeRss);	
				}
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(110);
				}
				Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					offRssList.add(eachPosRss);
					cnt++;times=0;
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
	
	public void randomRead(){}
	
	public void buildCenterPointsInfo(){	}
	
	public void buildBetterRSS(double quality){	}
	
	public void buildPenaltyList(){}
	
	public void buildWeightRSSList(){}
	
	public double caculateProbility(){return 0;}
	
	public static void main(String[] args) {
		new OfflineData(Constant.OFF_PATH);
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
