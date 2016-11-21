package test1;

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
	 * 此组织形式只是在pc上跑数据集用 实际应用应该改为单次的
	 */	
	private File file;
	private BufferedReader br;

	ArrayList<ArrayList<Map<String, Double>>> onRssList=new ArrayList<>(130);//每一个点每一次采集每一个ap
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<Map<String, Integer>>();
	List<String> aplist= Arrays.asList(Constant.AP_ARR);//27
	
	//应当在读取文件之后再填充
	public double[] XArr=Constant.ON_X_ARR;
	public double[] YArr=Constant.ON_Y_ARR;
	
	public int number=46;
	private static double availableRSS=-80.0;	
	
	public OnlineData(String path) {
		initBr(path);
		initRSSData();
		Tools.displayAllRSS(onRssList, aplist);
//		calculateOnAvgRss();
	}

	/**
	 * eachTimeRss中只put了能侦测到的ap以及对应的rss
	 */
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
					onRssList.add(eachPosRss);
					cnt++;times=0;
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
			System.out.println(Constant.ON_POS_ARR[t++]);
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
					System.out.println(aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}


	public static void main(String[] args) {
		OnlineData od=new OnlineData(Constant.ON_PATH);			
//		Tools.showList(od.aplist);
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
