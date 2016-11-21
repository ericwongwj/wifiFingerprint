package test2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.*;
/*****
 * ������⵽Ƶ�ʽ��ٵ�ap �Ƿ�Ӧ����һЩ��������������Ϊ��⵽�Ĵ�����
 * *****/

public class TestPositioning {
	static OfflineData offline=new OfflineData(Constant.OFF_PATH);
	static OnlineData online=new OnlineData(Constant.ON_PATH);
	
	static List <String> aplist= Arrays.asList(Constant.AP_ARR);//aplist����ͳһ
	
	static ArrayList<Map<String, Double>> penaltyList=new ArrayList<Map<String, Double>>();
	
	static double []deviationArr=new double[46];
	
	public static void main(String[] args) {
		init();//��ʼ�����������б�
		//initOffVectorList();
		//initOnVectorList();
		for(int c=0;c<46;c++){//
//			KMeans.KNN(offline, online.avgRssList.get(c), 4, c);
			KNN(online.avgRssList.get(c), 4, c);
		}
		Tools.calculateOverAllDeviationAndVariance(deviationArr);
	}
	
	static void init(){
//		offline.buildWeightRSSList();
//		ReadOffline.randomRead();//��ȫ��ʼ��offline������	
		//penaltyList=ReadOffline.penaltyList;
	}
	
	public static void KNN(Map<String,Double> onrss, int k,int index){
		//narrow ��Ϊ�õ�һ���ų������� ѭ����ʱ��ֱ���ж�
		ArrayList<Integer> invalidField=Tools.reduceField(offline.apVectorlist, online.apVectorlist.get(index));//����c�����c����
		ArrayList<Double> distanceList=new ArrayList<>();
		int p=0;
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
			if(invalidField.contains(i))//���޶���Χ������е�
				continue;
			
			Map<String,Double> offrss=offline.avgRssList.get(i);
			double distance=0;
			double sum=0;
			for(int j=0;j<aplist.size();j++){
				double off,on;
				if(offrss.get(aplist.get(j))!=null)
					off=offrss.get(aplist.get(j));
				else off=-100.0;
				if(onrss.get(aplist.get(j))!=null)
					on=onrss.get(aplist.get(j));
				else on=-100.0;
				sum+=(off-on)*(off-on);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//���off��on�ľ���
		}
		
		int []nearestpoints=Tools.findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ��
		double []nearestdistances=new double[k];
		double xsum=0.0,ysum=0.0;
		for(int a=0;a<k;a++){
			xsum+=offline.XArr[nearestpoints[a]];
			ysum+=offline.YArr[nearestpoints[a]];
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(Constant.OFF_POS_ARR[nearestpoints[a]]+" d="+nearestdistances[a]);
		}
		double []result={xsum/k,ysum/k};
		deviationArr[index]=Tools.calculateDeviation(online.XArr[index], online.YArr[index], result[0], result[1]);
		System.out.println("result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[index]+" deviation:"+deviationArr[index]);
	}
	
	static void initOnRandom(){
//		ReadOffline.readOff();
//		ReadOnline.initFile();
//		ReadOnline.initPosAndAP();
//		ReadOnline.randomRead();
	}

	
	
	
}

