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
 * 对于侦测到频率较少的ap 是否应当做一些处理？例如向量变为侦测到的次数？
 * *****/

public class TestPositioning {
	ReadOffline offline;
	ReadOnline online;
	
	static ArrayList <String> aplist;//aplist必须统一
	static ArrayList<Map<String, Double>>offEachPosRssList;/***一个map存放一个点的信号向量***/
	static ArrayList <String> offPositionlist;
	static ArrayList <Double> offXlist;
	static ArrayList <Double> offYlist;
	static ArrayList <ArrayList<Integer>> offApVectorlist=new ArrayList<>();
	static ArrayList <ArrayList<Double>> offRssVectorlist=new ArrayList<>();
	static ArrayList<Map<String, Double>> onEachPosRssList;/***一个map存放一个点的信号向量***/
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
		 * online和offline的比较小的值出现的次数可能在20附近 虽然概率较小
		 * 考虑一个矩阵的相似程度 或者向量变为平均强度乘以次数会不会更好一些
		 * ***/
//		ReadOnline.setTimes(10);//times过大无意义 算法中是连续取若干次
		ReadOffline.setCollectTimes(20);
		
		init();//初始化各个数据列表
		initOffVectorList();
		initOnVectorList();
		
		/*****对每一个online的点 先计算它和offline点的欧氏距离（在此之前是否应该narrowSearchField？） 
		 * 然后在欧式距离中找最近的n个*****/
		//ReadOffline.setDensity(120);//如果设为130 相当于没有narrow
		//ReadOffline.createPosWithDensity();		
		
//		ReadOffline.buildBetterRSS(-95.0);
		
//		ReadOffline.buildPenaltyList();
		for(int c=0;c<onPositionlist.size();c++){//
//			positionKWNNWithOffDensity(c);
//			positionUsingBetterRSS(c);
//			positionUsingPenalty(c);
			positionKWNN(c, 4, 1);
//			positionKWNNSingle(c, 4, 1);//用于handlingmissedap
//			positionKNN(c, 4);
		}
		calculateDeviationAndVariance();
	}
	
	static void init(){
		ReadOffline.readOff();
		ReadOffline.buildWeightRSSList();
//		ReadOffline.randomRead();//完全初始化offline的数据
		
		ReadOnline.read();//内部初始化 ap pos
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
	
	//利于封装 也许应该加一个定位的信号空间
	static void positionKNN(int c, int k){
		knn=k;
		ArrayList<Integer> searchField=narrowSearchField(c);//参数c代表第c个点
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
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
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static void positionNN(int c){
		ArrayList<Integer> searchField=narrowSearchField(c);//其实可以不用narrow...试试看
		ArrayList<Double> distanceList=new ArrayList<>();//存放和各个offline点的欧氏距离
		ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(1, distanceList);//数组长度为1 得到distancelist当中的位置
		double x=narrowedXList.get(nearestpoints[0]);//offXlist的下标不是narrow之后的下标
		double y=narrowedYList.get(nearestpoints[0]);
				
		double deviationX=x-onXlist.get(c);//计算误差
		double deviationY=y-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+x+","+y+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//利于封装 也许应该加一个定位的信号空间
	static void positionKWNN(int c, int k, double p){
		knn=k;
		ArrayList<Integer> searchField=narrowSearchField(c);//参数c代表第c个点
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
		ArrayList<Double> narrowedYList=new ArrayList<>();
		
		for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
			double y=narrowedYList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***定位得到结果***/
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//有用narrow  	distance_0 = w_0*distance(ap_0,ap_i) distance^2 = sigma(distance_0^2,...,distance_m^2)
	static void positionUsingPenalty(int c){
		ArrayList<Integer> searchField=narrowSearchField(c);//参数c代表第c个点
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
		ArrayList<Double> narrowedYList=new ArrayList<>();
		ArrayList<Map<String, Double>>penaltyList=ReadOffline.penaltyList;

		for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的线下点 计算欧氏距离***/
			String pos=offPositionlist.get(searchField.get(i));
			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
			Map<String,Double>map=penaltyList.get(searchField.get(i));
			narrowedXList.add(offXlist.get(searchField.get(i)));
			narrowedYList.add(offYlist.get(searchField.get(i)));
			double distance=0;
			double sum=0;
			double weight=0;
			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				if(map.get(aplist.get(j))!=null)
					weight=map.get(aplist.get(j));
				sum+=weight*(offrss-onrss)*(offrss-onrss);//每一个ap的维度上
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(4, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
		double []nearestdistances=new double[4];
		double []xarr=new double[4];
		double []yarr=new double[4];
		
		for(int a=0;a<nearestpoints.length;a++){
			double x=narrowedXList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
			double y=narrowedYList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, 1);
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static void positionRSSWeight(int c){
		
	}
	
	//不narrow
	static void positionKWNNSingle(int c, int k, double p){
		knn=k;
		ArrayList<Double> distanceList=new ArrayList<>();
		ArrayList<Double> xList=new ArrayList<>();//记录narrow之后位置的xy坐标
		ArrayList<Double> yList=new ArrayList<>();
		
		for(int i=0;i<offPositionlist.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
			String pos=offPositionlist.get(i);
			ArrayList<Double> rssvector=offRssVectorlist.get(i);
			xList.add(offXlist.get(i));
			yList.add(offYlist.get(i));
			
			double distance=0;
			double sum=0;
			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
				double offrss=rssvector.get(j);
				double onrss=onRssVectorlist.get(c).get(j);
				sum+=(offrss-onrss)*(offrss-onrss);
			}
			distance=Math.sqrt(sum);
			distanceList.add(distance);
//			System.out.println(i+" distance "+pos+": "+distance);
		}
		
		int []nearestpoints=findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
		double []nearestdistances=new double[k];
		double []xarr=new double[k];
		double []yarr=new double[k];
		
		for(int a=0;a<k;a++){
			double x=xList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
			double y=yList.get(nearestpoints[a]);
			xarr[a]=x;
			yarr[a]=y;
			nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
		}
		
		double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***定位得到结果***/
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//没narrow？
	static void positionUsingBetterRSS(int c){
		ArrayList<Map<String, Double>>eachPosQuaRssList=ReadOffline.eachPosQuaRssList;
		Map<String, Double> mapon=onEachPosRssList.get(c);
		ArrayList<Double> distanceList=new ArrayList<>();
		Map<Double,Integer> dindexmap=new HashMap<>();//存放距离和对应的index

		//计算距离
		for(int i=0;i<eachPosQuaRssList.size();i++){
			Map<String, Double> mapoff=eachPosQuaRssList.get(i);
			double sum=0;
			double distance=0;
			for(int j=0;j<aplist.size();j++){
				double on=mapon.get(aplist.get(j));
				if(j!=aplist.size()-1)//注意mapoff向量少一维
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
		//找到最近的距离 默认knn=4
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
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	//k此处默认为4
	static void positionKWNNWithOffDensity(int c){
		
		ArrayList<Map<String, Double>>densityRssList=ReadOffline.densityRssList;
		ArrayList<Integer>rpilist=ReadOffline.randposindexlist;
		ArrayList<Double> distanceList=new ArrayList<>();
		Map<Double,Integer> dindexmap=new HashMap<>();//存放距离和对应的index

		for(int i=0;i<densityRssList.size();i++){//对于信号空间中的所有点 计算对应的欧氏距离 一次循环产生一个距离
											// 线下第i个点的信号向量        当前线上点的信号向量		
			double distance=calculateDistance(densityRssList.get(i),onEachPosRssList.get(c));
			distanceList.add(distance);//对线上信号空间内的点产生距离列表
			int index=rpilist.get(i);
			dindexmap.put(distance, index);//densityRssList中的第i个向量
		}
		Collections.sort(distanceList);
		//for(double d:distanceList)
			//System.out.println("distance:"+d+" "+dindexmap.get(d));	
		
		//接下来寻找最近的点 对最近的距离 找出对应的poslist中的点 以及xy坐标 形成两个数组 传入函数当中
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
		double deviationX=result[0]-onXlist.get(c);//计算误差
		double deviationY=result[1]-onYlist.get(c);
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[c]=deviation;
		System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
							+" deviation:"+deviation);
	}
	
	static double calculateDistance(Map<String, Double> mapoff, Map<String, Double> mapon){//计算两个向量之间的距离
		double sum=0;
		for(int i=0;i<aplist.size();i++){
			double on=mapon.get(aplist.get(i));
			double off=-100.0;
			if(i!=aplist.size()-1)//注意mapoff向量少一维
				off=mapoff.get(aplist.get(i));
			sum+=(on-off)*(on-off);
		}
		return Math.sqrt(sum);
	}
	
	//如果不narrow 那就是offdensity中把密度调为130
	static ArrayList<Integer> narrowSearchField(int pointid){//返回一个缩小的搜索空间的位置索引表
		ArrayList<Integer> nearestPosList;//存放的序号代表positionlist的序号
		int[] similarityArr=new int[130];//分别对29个ap出现的次数进行计数
		for(int i=0;i<130;i++){
			similarityArr[i]=0;
		}
		int posid=0;
		for(ArrayList<Integer>apvector:offApVectorlist){//对130个位置进行遍历
			int k=0;
			for(Integer j:onApVectorlist.get(pointid)){//仅仅对第pointid个点的ap列表进行比较
				//System.out.println(j+" "+apvector.get(k));//apvector里面是代表有无的01
				if(apvector.get(k).equals(j)){
					similarityArr[posid]++;
				}
				k++;
			}
			posid++;
		}
		//找到这4orN个的点在offpositionlist当中的位置
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
			wtotal+=Math.pow(1/darr[i], p);//darr[i]:距离的反比
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
			if(sArr[i]>24){/***********这个次数是一个需要调教的参数 而且这样判断有问题 24是能跑完的临界值*************/
				spos.add(i);//c=11时 只有3个
			}
			//System.out.println(i+" similar:"+sArr[i]);
		}
		return spos;
	}

	//传入的是narrow之后的field（一个位置索引列表）
	static int[] findNNearest(int pnum, ArrayList<Double> dlist){//para1：point number 
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
			darr[narr[i]]=1000.0;//将已经找到的最小距离改为一个很大的值
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
		
		for(Map <String,Double>m: offEachPosRssList){//一个map就是一个向量
			Integer[] apvector=new Integer[29];//向量的顺序按照aplist的顺序
			Double[] rssvector=new Double[29];
			for(int i=0;i<29;i++){
				apvector[i]=0;
				rssvector[i]=-100.0;
			}
			for(String s: m.keySet()){//一个s就是一个ap的地址
				int index=aplist.indexOf(s);
				if(m.get(s)!=-100.0){//信号强度为零
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
		for(Map <String,Double>m: onEachPosRssList){//一个map就是一个向量
			Integer[] apvector=new Integer[29];//向量的顺序按照aplist的顺序
			Double[] rssvector=new Double[29];
			for(int i=0;i<29;i++){
				apvector[i]=0;
				rssvector[i]=-100.0;
			}
			for(String s: m.keySet()){//一个s就是一个ap的地址
				int index=aplist.indexOf(s);
				if(m.get(s)!=-100.0){//信号强度为零
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

