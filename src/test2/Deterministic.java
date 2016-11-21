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
	 * ��ĳһ�����rss���ж�λ 
	 * @param onrss ���ϵ��ap-rss map
	 * @param k ��������
	 * @param index Ϊ�˼������֪���ǵڼ�����
	 */
	public static double[] KNN(OfflineData offline, Map<String,Double> onrss, int k,int index){
		ArrayList<Double> distanceList=new ArrayList<>();
		List<String> aplist=offline.aplist;
 		for(int i=0;i<offline.XArr.length;i++){//�����µ����е�
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
//			System.out.println(Constant.offtxts[i]+" distance "+": "+distance);//���off��on�ľ���
		}
		
		int []nearestpoints=Tools.findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ��
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
		//��������µ����е�ľ���
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
//			System.out.println(Constant.txts[index]+" distance "+": "+distance);//���off��on�ľ���
		}
		//�ҵ����k�����λ�� �õ����
		int []nearestpoints=Tools.findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distanceList���е�λ��
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
