package test2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Deterministic {
	int k;
	void setK(int k){
		this.k=k;
	}
	
	/**
	 * 对某一个点的rss进行定位 
	 * @param onrss 线上点的ap-rss map
	 * @param k 最近点个数
	 * @param index 为了计算误差知道是第几个点
	 */
	public static double[] KNN(OfflineData offline, Map<String,Double> onrss, int k,int index){
		ArrayList<Double> distanceList=new ArrayList<>();
		List<String> aplist=offline.aplist;
 		for(int i=0;i<offline.XArr.length;i++){//对线下的所有点
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
//			System.out.println(Constant.offtxts[i]+" distance "+": "+distance);//输出off与on的距离
		}
		
		int []nearestpoints=Tools.findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置
		double []nearestdistances=new double[k];
		double xsum=0.0,ysum=0.0;
		for(int a=0;a<k;a++){
			xsum+=offline.XArr[nearestpoints[a]];
			ysum+=offline.YArr[nearestpoints[a]];
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
			System.out.println(Constant.OFF_POS_ARR[nearestpoints[a]]+" d="+nearestdistances[a]);
		}
		
		double []result={xsum/k,ysum/k};
		return result;
	}
	
	public static double[] WKNN(OfflineData offline, Map<String,Double> onrss, int k, int index){
		//计算和线下的所有点的距离
		ArrayList<Double> distanceList=new ArrayList<>();
		List<String> aplist=offline.aplist;
		for(int i=0;i<offline.XArr.length;i++){
			Map<String,Double> offrss=offline.avgRssList.get(i);
			double distance=0,sum=0;
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
//			System.out.println(Constant.txts[index]+" distance "+": "+distance);//输出off与on的距离
		}
		//找到最近k个点的位置 得到结果
		int []nearestpoints=Tools.findNNearest(k, distanceList);//第一个参数为取点的个数 返回distanceList当中的位置
		double []weights=new double[k];
		double x=0.0,y=0.0,weight=0.0,total=0.0;
		for(int a=0;a<k;a++){
			int pos=nearestpoints[a];
			weights[a]=1/distanceList.get(pos);
			weight+=1/distanceList.get(pos);
			System.out.println(offline.XArr[pos]+","+offline.YArr[pos]+" d="+distanceList.get(pos)+" weight="+weights[a]);
		}
		System.out.println("total weight="+weight);
		for(int b=0;b<k;b++){
			int pos=nearestpoints[b];
			x+=offline.XArr[pos]*weights[b]/weight;
			y+=offline.YArr[pos]*weights[b]/weight;
//			System.out.println("x="+x+" y="+y+" "+posXlist[b]+" "+posY);
		}
		double []result={x,y};
		return result;
	}
}
