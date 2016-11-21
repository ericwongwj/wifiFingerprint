package test1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.*;
/*****
 * ������⵽Ƶ�ʽ��ٵ�ap �Ƿ�Ӧ����һЩ��������������Ϊ��⵽�Ĵ�����
 * *****/

public class TestPositioning {
	ReadOffline offline;
	ReadOnline online;
	
	static ArrayList <String> aplist;//aplist����ͳһ
	static ArrayList<Map<String, Double>>offEachPosRssList;/***һ��map���һ������ź�����***/
	static ArrayList <String> offPositionlist;
	static ArrayList <Double> offXlist;
	static ArrayList <Double> offYlist;
	static ArrayList <ArrayList<Integer>> offApVectorlist=new ArrayList<>();
	static ArrayList <ArrayList<Double>> offRssVectorlist=new ArrayList<>();
	static ArrayList<Map<String, Double>> onEachPosRssList;/***һ��map���һ������ź�����***/
	static ArrayList<Map<String, Double>> penaltyList=new ArrayList<Map<String, Double>>();
	static ArrayList <String> onPositionlist;
	static ArrayList <ArrayList<Integer>> onApVectorlist=new ArrayList<>();
	static ArrayList <ArrayList<Double>> onRssVectorlist=new ArrayList<>();
	static ArrayList <Double> onXlist;
	static ArrayList <Double> onYlist;
	static Double []deviationArr=new Double[46];
	
	static int knn=4;
	
	public static void main(String[] args) {
		/***
		 * online��offline�ıȽ�С��ֵ���ֵĴ���������20���� ��Ȼ���ʽ�С
		 * ����һ����������Ƴ̶� ����������Ϊƽ��ǿ�ȳ��Դ����᲻�����һЩ
		 * ***/
//		ReadOnline.setTimes(10);//times���������� �㷨��������ȡ���ɴ�
		ReadOffline.setCollectTimes(20);
		
		init();//��ʼ�����������б�
		initOffVectorList();
		initOnVectorList();
		
		/*****��ÿһ��online�ĵ� �ȼ�������offline���ŷ�Ͼ��루�ڴ�֮ǰ�Ƿ�Ӧ��narrowSearchField���� 
		 * Ȼ����ŷʽ�������������n��*****/
		//ReadOffline.setDensity(120);//�����Ϊ130 �൱��û��narrow
		//ReadOffline.createPosWithDensity();		
		
//		ReadOffline.buildBetterRSS(-95.0);
		
//		ReadOffline.buildPenaltyList();
		for(int c=0;c<onPositionlist.size();c++){//
//			positionKWNNWithOffDensity(c);
//			positionUsingBetterRSS(c);
//			positionUsingPenalty(c);
			positionKWNN(c, 4, 1);
//			positionKWNNSingle(c, 4, 1);//����handlingmissedap
//			positionKNN(c, 4);
		}
		calculateDeviationAndVariance();
	}
	
	static void init(){
		ReadOffline.readOff();
		ReadOffline.buildWeightRSSList();
//		ReadOffline.randomRead();//��ȫ��ʼ��offline������
		
		ReadOnline.read();//�ڲ���ʼ�� ap pos
//		initOnRandom();
//		ReadOnline.readHandlingMissedAP();
		
		aplist=ReadOnline.aplist;
		offEachPosRssList=ReadOffline.eachPosRssList;
		offPositionlist=ReadOffline.positionlist;
		offXlist=ReadOffline.posXlist;
		offYlist=ReadOffline.posYlist;
		penaltyList=ReadOffline.penaltyList;
		
		onEachPosRssList=ReadOnline.eachPosRssList;
		onPositionlist=ReadOnline.positionlist;
		onXlist=ReadOnline.posXlist;
		onYlist=ReadOnline.posYlist;
	}
	
	//���ڷ�װ Ҳ��Ӧ�ü�һ����λ���źſռ�
	static void positionKNN(int c, int k){
		knn=k;
		ArrayList<Integer> searchField=narrowSearchField(c);//����c�����c����
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
			double y=narrowedYList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
		}
		
		double xtotal=0, ytotal=0;
		for(int i=0;i<knn;i++){
			xtotal+=xarr[i];
			ytotal+=yarr[i];
		}
		double []result={xtotal/knn,ytotal/knn};
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static void positionNN(int c){
		ArrayList<Integer> searchField=narrowSearchField(c);//��ʵ���Բ���narrow...���Կ�
		ArrayList<Double> distanceList=new ArrayList<>();//��ź͸���offline���ŷ�Ͼ���
		ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(1, distanceList);//���鳤��Ϊ1 �õ�distancelist���е�λ��
		double x=narrowedXList.get(nearestpoints[0]);//offXlist���±겻��narrow֮����±�
		double y=narrowedYList.get(nearestpoints[0]);
				
		double deviationX=x-onXlist.get(c);//�������
		double deviationY=y-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+x+","+y+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//���ڷ�װ Ҳ��Ӧ�ü�һ����λ���źſռ�
	static void positionKWNN(int c, int k, double p){
		knn=k;
		ArrayList<Integer> searchField=narrowSearchField(c);//����c�����c����
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
			double y=narrowedYList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***��λ�õ����***/
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//����narrow  	distance_0 = w_0*distance(ap_0,ap_i) distance^2 = sigma(distance_0^2,...,distance_m^2)
	static void positionUsingPenalty(int c){
		ArrayList<Integer> searchField=narrowSearchField(c);//����c�����c����
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
		ArrayList<Double> narrowedYList=new ArrayList<>();
		ArrayList<Map<String, Double>>penaltyList=ReadOffline.penaltyList;

		for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�е����µ� ����ŷ�Ͼ���***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			Map<String,Double>map=penaltyList.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			double distance=0;
			double sum=0;
			double weight=0;
			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				if(map.get(aplist.get(j))!=null)
					weight=map.get(aplist.get(j));
				sum+=weight*(offrss-onrss)*(offrss-onrss);//ÿһ��ap��ά����
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(4, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
		double []nearestdistances=new double[4];
		double []xarr=new double[4];
		double []yarr=new double[4];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
			double y=narrowedYList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, 1);
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static void positionRSSWeight(int c){
		
	}
	
	//��narrow
	static void positionKWNNSingle(int c, int k, double p){
		knn=k;
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> xList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
		ArrayList<Double> yList=new ArrayList<>();
		
		for(int i=0;i<offPositionlist.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
			String pos=offPositionlist.get(i);
			ArrayList<Double> rssvector=offRssVectorlist.get(i);
			xList.add(offXlist.get(i));
			yList.add(offYlist.get(i));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<k;a++){
			double x=xList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
			double y=yList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***��λ�õ����***/
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//ûnarrow��
	static void positionUsingBetterRSS(int c){
		ArrayList<Map<String, Double>>eachPosQuaRssList=ReadOffline.eachPosQuaRssList;
		Map<String, Double> mapon=onEachPosRssList.get(c);
		ArrayList<Double> distanceList=new ArrayList<>();
		Map<Double,Integer> dindexmap=new HashMap<>();//��ž���Ͷ�Ӧ��index

		//�������
		for(int i=0;i<eachPosQuaRssList.size();i++){
			Map<String, Double> mapoff=eachPosQuaRssList.get(i);
			double sum=0;
			double distance=0;
			for(int j=0;j<aplist.size();j++){
				double on=mapon.get(aplist.get(j));
				if(j!=aplist.size()-1)//ע��mapoff������һά
					if(mapoff.get(aplist.get(j))!=null){
						double off=mapoff.get(aplist.get(j));
						sum+=(on-off)*(on-off);
					}
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
			dindexmap.put(distance, i);
//			System.out.println(i+" "+distance);
		}
		//�ҵ�����ľ��� Ĭ��knn=4
		Collections.sort(distanceList);
		double []ndist=new double[4];
		double []xarr=new double[4];
		double []yarr=new double[4];
		for(int i=0;i<4;i++){
			ndist[i]=distanceList.get(i);
			int index=dindexmap.get(ndist[i]);
//			System.out.println("distance:"+ndist[i]+" "+offPositionlist.get(dindexmap.get(ndist[i])));	
			xarr[i]=offXlist.get(index);
			yarr[i]=offYlist.get(index);
		}
		
		double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//k�˴�Ĭ��Ϊ4
	static void positionKWNNWithOffDensity(int c){
		
		ArrayList<Map<String, Double>>densityRssList=ReadOffline.densityRssList;
		ArrayList<Integer>rpilist=ReadOffline.randposindexlist;
		ArrayList<Double> distanceList=new ArrayList<>();
		Map<Double,Integer> dindexmap=new HashMap<>();//��ž���Ͷ�Ӧ��index

		for(int i=0;i<densityRssList.size();i++){//�����źſռ��е����е� �����Ӧ��ŷ�Ͼ��� һ��ѭ������һ������
											// ���µ�i������ź�����        ��ǰ���ϵ���ź�����		
			double distance=calculateDistance(densityRssList.get(i),onEachPosRssList.get(c));
			distanceList.add(distance);//�������źſռ��ڵĵ���������б�
			int index=rpilist.get(i);
			dindexmap.put(distance, index);//densityRssList�еĵ�i������
		}
		Collections.sort(distanceList);
		//for(double d:distanceList)
			//System.out.println("distance:"+d+" "+dindexmap.get(d));	
		
		//������Ѱ������ĵ� ������ľ��� �ҳ���Ӧ��poslist�еĵ� �Լ�xy���� �γ��������� ���뺯������
		double []ndist=new double[4];
		double []xarr=new double[4];
		double []yarr=new double[4];
		for(int i=0;i<4;i++){
			ndist[i]=distanceList.get(i);
			int index=dindexmap.get(ndist[i]);
			xarr[i]=offXlist.get(index);
			yarr[i]=offYlist.get(index);
		}
		
		double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
		double deviationX=result[0]-onXlist.get(c);//�������
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static double calculateDistance(Map<String, Double> mapoff, Map<String, Double> mapon){//������������֮��ľ���
		double sum=0;
		for(int i=0;i<aplist.size();i++){
			double on=mapon.get(aplist.get(i));
			double off=-100.0;
			if(i!=aplist.size()-1)//ע��mapoff������һά
				off=mapoff.get(aplist.get(i));
			sum+=(on-off)*(on-off);
		}
		return Math.sqrt(sum);
	}
	
	//�����narrow �Ǿ���offdensity�а��ܶȵ�Ϊ130
	static ArrayList<Integer> narrowSearchField(int pointid){//����һ����С�������ռ��λ��������
		ArrayList<Integer> nearestPosList;//��ŵ���Ŵ���positionlist�����
		int[] similarityArr=new int[130];//�ֱ��29��ap���ֵĴ������м���
		for(int i=0;i<130;i++){
			similarityArr[i]=0;
		}
		int posid=0;
		for(ArrayList<Integer>apvector:offApVectorlist){//��130��λ�ý��б���
			int k=0;
			for(Integer j:onApVectorlist.get(pointid)){//�����Ե�pointid�����ap�б���бȽ�
				//System.out.println(j+" "+apvector.get(k));//apvector�����Ǵ������޵�01
				if(apvector.get(k).equals(j)){
					similarityArr[posid]++;
				}
				k++;
			}
			posid++;
		}
		//�ҵ���4orN���ĵ���offpositionlist���е�λ��
//		for(int i=0;i<similarityArr.length;i++)
//			System.out.println(i+" "+similarityArr[i]);
		nearestPosList=findSimilarVector(similarityArr);
//		for(int i=0;i<nearestPosList.size();i++){
//			System.out.println(i+" "+offPositionlist.get(nearestPosList.get(i)));
//		}
		return nearestPosList;
	}
	
	static double[] calculatePosition(double []xarr,double []yarr,double []darr,int length, double p){
		double[] result=new double[2];//0:x 1:y
		double wtotal=0;
		for(int i=0;i<length;i++){
			wtotal+=Math.pow(1/darr[i], p);//darr[i]:����ķ���
		}
		for(int i=0;i<length;i++){
			double a=Math.pow(darr[i], p);
			result[0]+=xarr[i]/(wtotal*a);
			result[1]+=yarr[i]/(wtotal*a);
		}	
		return result;
	}
	
	static ArrayList<Integer> findSimilarVector(int[] sArr){
		ArrayList<Integer> spos=new ArrayList<>();
		for(int i=0;i<sArr.length-1;i++){
			if(sArr[i]>24){/***********���������һ����Ҫ���̵Ĳ��� ���������ж������� 24����������ٽ�ֵ*************/
				spos.add(i);//c=11ʱ ֻ��3��
			}
			//System.out.println(i+" similar:"+sArr[i]);
		}
		return spos;
	}

	//�������narrow֮���field��һ��λ�������б�
	static int[] findNNearest(int pnum, ArrayList<Double> dlist){//para1��point number 
		int []narr=new int[pnum];
		int i=0;
		Double[]darr=new Double[dlist.size()];
		dlist.toArray(darr);
		while(i<pnum){
			double min=darr[0];
			for(int j=0;j<darr.length-1;j++){
				if(min>darr[j+1]){
					min=darr[j+1];
				}
			}
			narr[i]=dlist.indexOf(min);
//			System.out.println("min:"+min+" "+narr[i]);
			darr[narr[i]]=1000.0;//���Ѿ��ҵ�����С�����Ϊһ���ܴ��ֵ
			i++;
		}
		//for(int k=0;k<darr.length;k++) System.out.println(dlist.get(k));
		return narr;
	}
	
	static void calculateDeviationAndVariance(){
		double deviationsum=0.0;
		for(double d: deviationArr){
			deviationsum+=d;
		}
		double avgdeviation=deviationsum/46;
		
		double variancesum=0.0;
		for(double d:deviationArr){
			variancesum+=(d-avgdeviation)*(d-avgdeviation);
		}
		double variance=Math.sqrt(variancesum);
		System.out.println("average deviation:"+avgdeviation+"  variance:"+variance);
	}
	
	static void initOnRandom(){
		ReadOffline.readOff();
		ReadOnline.initFile();
		ReadOnline.initPosAndAP();
		ReadOnline.randomRead();
	}

	
	static void initOffVectorList(){
		
		for(Map <String,Double>m: offEachPosRssList){//һ��map����һ������
			Integer[] apvector=new Integer[29];//������˳����aplist��˳��
			Double[] rssvector=new Double[29];
			for(int i=0;i<29;i++){
				apvector[i]=0;
				rssvector[i]=-100.0;
			}
			for(String s: m.keySet()){//һ��s����һ��ap�ĵ�ַ
				int index=aplist.indexOf(s);
				if(m.get(s)!=-100.0){//�ź�ǿ��Ϊ��
					apvector[index]=1;
					rssvector[index]=m.get(s);
				}
			}
			
			ArrayList<Integer>temp1=new ArrayList<>();
			ArrayList<Double>temp2=new ArrayList<>();
			for(int i=0;i<29;i++){
				temp1.add(apvector[i]);
				temp2.add(rssvector[i]);
			}
			offApVectorlist.add(temp1);
			offRssVectorlist.add(temp2);
		}
		
//		for(int i=0;i<130;i++){
//			System.out.println(offPositionlist.get(i));
//			System.out.println(offApVectorlist.get(i));
//			System.out.println(offRssVectorlist.get(i));
//		}	
	}
	
	static void initOnVectorList(){
		for(Map <String,Double>m: onEachPosRssList){//һ��map����һ������
			Integer[] apvector=new Integer[29];//������˳����aplist��˳��
			Double[] rssvector=new Double[29];
			for(int i=0;i<29;i++){
				apvector[i]=0;
				rssvector[i]=-100.0;
			}
			for(String s: m.keySet()){//һ��s����һ��ap�ĵ�ַ
				int index=aplist.indexOf(s);
				if(m.get(s)!=-100.0){//�ź�ǿ��Ϊ��
					apvector[index]=1;
					rssvector[index]=m.get(s);
				}
			}
			
			ArrayList<Integer>temp1=new ArrayList<>();
			ArrayList<Double>temp2=new ArrayList<>();
			for(int i=0;i<29;i++){
				temp1.add(apvector[i]);
				temp2.add(rssvector[i]);
			}
			onApVectorlist.add(temp1);
			onRssVectorlist.add(temp2);
		}
		
//		for(int i=0;i<46;i++){
//			System.out.println(onPositionlist.get(i));
//			System.out.println(onApVectorlist.get(i));
//			System.out.println(onRssVectorlist.get(i));
//		}
	}
	
	static void setKNN(int k){
		knn=k;
	}

}

