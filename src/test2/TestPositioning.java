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
 * 对于侦测到频率较少的ap 是否应当做一些处理？例如向量变为侦测到的次数？
 * *****/

public class TestPositioning {
	static OfflineData offline;
	static OnlineData online;
	
	static List <String> aplist= Arrays.asList(Constant.AP_ARR);//aplist必须统一
		
	static double []deviationArr=new double[46];//TODO：应该变为 取几个点定位 就生成
	
	public static void main(String[] args) {
		//double pd, double td, int nf,double deRss, double avRss   (27,99,-100)WKNN k=4 -1.423 KNN-1.428 avail在-95附近有意义
		offline=new OfflineData(Constant.OFF_PATH,new OfflineData.Options(1.0,1.0,27,-99,-100));//
		online=new OnlineData(Constant.ON_PATH,1.0);
		Tools.similar=23;//不提高精度 24以上误差更大 其实没啥用
		for(int c=0;c<46;c++){//
//			KNN(online.avgRssList.get(c), 4, true, c);//5,6精度最好  4+true最好 1.5306
			WKNN(online.avgRssList.get(c), 4, 0.1, true, c);//6精度最好 4+true 1.5297 exp=0.1:1.4217
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
		//narrow 改为得到一个排除的数组 循环的时候直接判断 
		List<Integer> invalidField=Tools.reduceField(offline.apVectorlist, online.apVectorlist.get(index));//参数c代表第c个点
		Map<Double,Integer> distanceMap=new TreeMap<>();//位置（0-129）-距离
		System.out.println("invalid field size:"+invalidField.size());

		int p=0;
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
//			if(invalidField.contains(i)) continue;//对限定范围后的所有点 其实并没有用
			//计算距离 只计算线下出现的ap 线上线下只出现一个的 默认另一个为-100
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
//			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
		}
		
		double xsum=0.0,ysum=0.0;
		int kk=0;
		for(Double d:distanceMap.keySet()){
			if(kk++==k)
				break;
			int pos=distanceMap.get(d);
			xsum+=offline.XArr[pos];
			ysum+=offline.YArr[pos];
			System.out.println(Constant.OFF_POS_ARR[pos]+" d="+d);
		}
		
		double []result={xsum/k,ysum/k};
		deviationArr[index]=Tools.calculateDeviation(online.XArr[index], online.YArr[index], result[0], result[1]);
		System.out.println("result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[index]+" deviation:"+deviationArr[index]);
	}
	
	public static void WKNN(Map<String,Double> onrss, int k, double exp, boolean usePenalty, int index){
		Map<Double,Integer> distanceMap=new TreeMap<>();//位置（0-129）-距离
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
			//计算距离 只计算线下出现的ap 线上线下只出现一个的 默认另一个为-100
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
//			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
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
			x+=offline.XArr[pos]*weights[b]/weight;
			y+=offline.YArr[pos]*weights[b]/weight;
			System.out.println(Constant.OFF_POS_ARR[pos]+" d="+entry.getKey()+" w="+weights[b]);
		}
		double []result={x,y};
		deviationArr[index]=Tools.calculateDeviation(online.XArr[index], online.YArr[index], result[0], result[1]);
		System.out.println("result:"+result[0]+","+result[1]+"  real pos:"+Constant.ON_POS_ARR[index]+" deviation:"+deviationArr[index]);
				
	}	
	
	
}

