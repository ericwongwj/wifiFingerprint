package test2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.math.*;
/*****
 * ������⵽Ƶ�ʽ��ٵ�ap �Ƿ�Ӧ����һЩ��������������Ϊ��⵽�Ĵ�����
 * *****/

public class TestPositioning {
	static OfflineData offline;
	static OnlineData online;
	
	static List <String> aplist= Arrays.asList(Constant.AP_ARR);//aplist����ͳһ
	
	static Point []resultPoints=new Point[46];
	static double []deviationArr=new double[46];//TODO��Ӧ�ñ�Ϊ ȡ�����㶨λ ������
	
	public static void main(String[] args) {
		//double pd, double td, int nf,double deRss, double avRss   (27,99,-100)WKNN k=4 -1.423 KNN-1.428 avail��-95����������
		offline=new OfflineData(Constant.OFF_PATH,new OfflineData.Options(1.0,1.0,27,-99,-100));//
		online=new OnlineData(Constant.ON_PATH,1.0);
		Tools.similar=23;//����߾��� 24���������� ��ʵûɶ��
		for(int c=0;c<46;c++){//
//			KNN(online.avgRssList.get(c), 4, true, c);//5,6�������  4+true��� 1.5306
//			WKNN(online.avgRssList.get(c), 4, 0.1, true, c);//6������� 4+true 1.5297 exp=0.1:1.4217
			positioningALL(offline.rssVectorlist, online);
		}
		Tools.calculateOverAllDeviationAndVariance(deviationArr);
	}
	
	/**
	 * 
	 * @param onrss
	 * @param k
	 * @param usePenalty
	 * @param index
	 */
	public static void KNN(Map<String,Double> onrss, int k, boolean usePenalty, int index){
		//narrow ��Ϊ�õ�һ���ų������� ѭ����ʱ��ֱ���ж� 
		List<Integer> invalidField=Tools.reduceField(offline.apVectorlist, online.apVectorlist.get(index));//����c�����c����
		Map<Double,Integer> distanceMap=new TreeMap<>();//λ�ã�0-129��-����
		System.out.println("invalid field size:"+invalidField.size());

		int p=0;
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
//			if(invalidField.contains(i)) continue;//���޶���Χ������е� ��ʵ��û����
			//������� ֻ�������³��ֵ�ap ��������ֻ����һ���� Ĭ����һ��Ϊ-100
			Map<String,Double> offrss=offline.avgRssList.get(i);
			Map<String,Double> penaltyMap=offline.penaltyList.get(i);
			double distance, sum=0;
			for(int j=0;j<aplist.size();j++){
				double penalty=penaltyMap.get(aplist.get(j));
				double off,on;
				if(offrss.get(aplist.get(j))!=null)
					off=offrss.get(aplist.get(j));
				else off=-100.0;
				
				if(onrss.get(aplist.get(j))!=null)
					on=onrss.get(aplist.get(j));
				else on=-100.0;
				sum += usePenalty ? penalty*(off-on)*(off-on) : (off-on)*(off-on);
			}
			distance=Math.sqrt(sum);
			distanceMap.put(distance,i);
//			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//���off��on�ľ���
		}
		
		double xsum=0.0,ysum=0.0;
		int kk=0;
		for(Double d:distanceMap.keySet()){
			if(kk++==k)
				break;
			int pos=distanceMap.get(d);
			xsum+=offline.points[pos].x;
			ysum+=offline.points[pos].y;
			System.out.println(offline.points[pos]+" d="+d);
		}
		Point result=new Point(xsum/k,ysum/k);
		deviationArr[index]=online.points[index].distance(result);
		System.out.println("result:"+result+"   real:"+online.points[index]+" deviation:"+deviationArr[index]);
	}
	
	public static void WKNN(Map<String,Double> onrss, int k, double exp, boolean usePenalty, int index){
		Map<Double,Integer> distanceMap=new TreeMap<>();//λ�ã�0-129��-����
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
			//������� ֻ�������³��ֵ�ap ��������ֻ����һ���� Ĭ����һ��Ϊ-100
			Map<String,Double> offrss=offline.avgRssList.get(i);
			Map<String,Double> penaltyMap=offline.penaltyList.get(i);
			double distance, sum=0;
			for(int j=0;j<aplist.size();j++){
				double penalty=penaltyMap.get(aplist.get(j));
				double off,on;
				if(offrss.get(aplist.get(j))!=null)
					off=offrss.get(aplist.get(j));
				else off=-100.0;
				
				if(onrss.get(aplist.get(j))!=null)
					on=onrss.get(aplist.get(j));
				else on=-100.0;
				sum += usePenalty ? penalty*(off-on)*(off-on) : (off-on)*(off-on);
			}
			distance=Math.sqrt(sum);
			distanceMap.put(distance,i);
//			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//���off��on�ľ���
		}
		
		double x=0.0,y=0.0,weight=0.0;
		double []weights=new double[k];
		Iterator<Map.Entry<Double, Integer>> iterator = distanceMap.entrySet().iterator();
		for(int a=0;a<k;a++){
			Map.Entry<Double, Integer> entry = iterator.next();  
			int pos=entry.getValue();
			weights[a]=Math.pow(1/entry.getKey(), exp);
			weight+=Math.pow(1/entry.getKey(), exp);
//			System.out.println(offline.XArr[pos]+","+offline.YArr[pos]+" d="+entry.getKey()+" weight="+weights[a]);
		}
		System.out.println("total weight="+weight);
		Iterator<Map.Entry<Double, Integer>> entries = distanceMap.entrySet().iterator();

		for(int b=0;b<k;b++){
			Map.Entry<Double, Integer> entry = entries.next();  
			int pos=entry.getValue();
			x+=offline.points[pos].x*weights[b]/weight;
			y+=offline.points[pos].y*weights[b]/weight;
			System.out.println(Constant.OFF_POS_ARR[pos]+" d="+entry.getKey()+" w="+weights[b]);
		}
		Point result=new Point(x,y);
		deviationArr[index]=online.points[index].distance(result);
		System.out.println("result:"+result+"   real:"+online.points[index]+" deviation:"+deviationArr[index]);
				
	}	

		
	/**
	 * ��oneline��ÿһ����ÿһ�εĶ�λ����������
	 * @param histogram �������е�offlineֱ��ͼ
	 * @param online ��������
	 */
	public static void positioningALL(List<List<Map<Double,Integer>>> histogram, OnlineData online){
		List<List<Map<String, Double>>> onRssList=online.allRssList;
		for(int i=4;i<5;i++){//onRssList.size()
			List<Map<String, Double>> onePosRss=onRssList.get(i);
			System.out.println(online.points[i]);
			for(Map<String,Double>oneTimeRss:onePosRss){
				positionUsingHistogram(histogram, oneTimeRss);
			}
		}
	}
	public static Map<Double,Integer> findMapByAp(List<Map<Double,Integer>> onePosHistogram, String ap){//????
		int index=aplist.indexOf(ap);
		for(Map<Double,Integer>oneApHistogram:onePosHistogram){
			if(oneApHistogram.get(-1.0)==index){
				return oneApHistogram;
			}
		}
		return null;
	}
	public static void positionUsingHistogram(List<List<Map<Double,Integer>>> histogram,Map<String,Double>oneTimeRss){
		double pArr[]=new double[histogram.size()];//��¼ÿ����ĸ���
		Tools.cleanArr(pArr);
		for(int i=0;i<histogram.size();i++){
			List<Map<Double,Integer>> onePosHistogram=histogram.get(i);
			double p=1.0;//�����ĸ���
			for(String ap:oneTimeRss.keySet()){//ֻ�ȶ� �������¶��е� ���Կ��Ƕ�ȱʧ�ĵ���д���
				double onrss=oneTimeRss.get(ap);
				Map<Double,Integer> oneApHistogram=findMapByAp(onePosHistogram, ap);//�����Ҳ������ap
//				System.out.println(oneApHistogram.get(onrss));
				if(oneApHistogram!=null)
					if(oneApHistogram.get(onrss)!=null)
						p+=oneApHistogram.get(onrss);//����д�Ǵ���֮��
			}
			pArr[i]=p;
		}
		Tools.showArr(pArr);
		int []maxIndex=getNMax(pArr,4);
		double x = 0,y = 0;
		for(int i=0;i<maxIndex.length;i++){
			x+=offline.points[maxIndex[i]].x;
			y+=offline.points[maxIndex[i]].y;
		}
		System.out.println(maxIndex[0]+" "+maxIndex[1]+" "+maxIndex[2]+" "+maxIndex[3]);
		System.out.println("result:"+x/4+","+y/4);
	}
	
	//����pArr�е�index
	public static int[] getNMax(double []parr,int n){
		int []idx=new int[n];
		double []temp=new double[n];
		int i=0;
		while(i<n){//ȡn����
			int index=0;
			double max=parr[0];
			for(int j=0;j<parr.length-1;j++){//�ҵ���ǰ������ֵ����λ��
				if(max<parr[j+1]){
					max=parr[j+1];
					index=j+1;
				}
			}
			idx[i]=index;
			parr[index]=-1000.0;//���Ѿ��ҵ����������Ϊһ����С��ֵ ע���ʱԭ����ֵ�Ѿ��ı�
			i++;
		}
		return idx;
	}
	
}

