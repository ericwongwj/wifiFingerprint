package test2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import test1.Constant;

public class ProbabilityMethods {
//	static void positionNN(int c){
//		ArrayList<Integer> searchField=narrowSearchField(c);//��ʵ���Բ���narrow...���Կ�
//		ArrayList<Double> distanceList=new ArrayList<>();//��ź͸���offline���ŷ�Ͼ���
//		ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
//		ArrayList<Double> narrowedYList=new ArrayList<>();
//		
//		for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
//			String pos=offPositionlist.get(searchField.get(i));
//			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//			narrowedXList.add(offXlist.get(searchField.get(i)));
//			narrowedYList.add(offYlist.get(searchField.get(i)));
//			
//			double distance=0;
//			double sum=0;
//			for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
//				double offrss=rssvector.get(j);
//				double onrss=onRssVectorlist.get(c).get(j);
//				sum+=(offrss-onrss)*(offrss-onrss);
//			}
//			distance=Math.sqrt(sum);
//			distanceList.add(distance);
////			System.out.println(i+" distance "+pos+": "+distance);
//		}
//		
//		int []nearestpoints=findNNearest(1, distanceList);//���鳤��Ϊ1 �õ�distancelist���е�λ��
//		double x=narrowedXList.get(nearestpoints[0]);//offXlist���±겻��narrow֮����±�
//		double y=narrowedYList.get(nearestpoints[0]);
//				
//		double deviationX=x-onXlist.get(c);//�������
//		double deviationY=y-onYlist.get(c);
//		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//		deviationArr[c]=deviation;
//		System.out.println(c+" result:"+x+","+y+"   true position:"+Constant.ON_POS_ARR[c]
//							+" deviation:"+deviation);
//	}
	//���ڷ�װ Ҳ��Ӧ�ü�һ����λ���źſռ�
//		static void positionKWNN(int c, int k, double p){
//			knn=k;
//			ArrayList<Integer> searchField=narrowSearchField(c);//����c�����c����
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
//			ArrayList<Double> narrowedYList=new ArrayList<>();
//			
//			for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
//				String pos=offPositionlist.get(searchField.get(i));
//				ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//				narrowedXList.add(offXlist.get(searchField.get(i)));
//				narrowedYList.add(offYlist.get(searchField.get(i)));
//				
//				double distance=0;
//				double sum=0;
//				for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					sum+=(offrss-onrss)*(offrss-onrss);
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
//			double []nearestdistances=new double[k];
//			double []xarr=new double[k];
//			double []yarr=new double[k];
//			
//			for(int a=0;a<nearestpoints.length;a++){
//				double x=narrowedXList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
//				double y=narrowedYList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
////				System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***��λ�õ����***/
//			double deviationX=result[0]-onXlist.get(c);//�������
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
//		
//		//����narrow  	distance_0 = w_0*distance(ap_0,ap_i) distance^2 = sigma(distance_0^2,...,distance_m^2)
//		static void positionUsingPenalty(int c){
//			ArrayList<Integer> searchField=narrowSearchField(c);//����c�����c����
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> narrowedXList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
//			ArrayList<Double> narrowedYList=new ArrayList<>();
//			ArrayList<Map<String, Double>>penaltyList=ReadOffline.penaltyList;
//
//			for(int i=0;i<searchField.size();i++){/***����С֮��ķ�Χ�е����µ� ����ŷ�Ͼ���***/
//				String pos=offPositionlist.get(searchField.get(i));
//				ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//				Map<String,Double>map=penaltyList.get(searchField.get(i));
//				narrowedXList.add(offXlist.get(searchField.get(i)));
//				narrowedYList.add(offYlist.get(searchField.get(i)));
//				double distance=0;
//				double sum=0;
//				double weight=0;
//				for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					if(map.get(aplist.get(j))!=null)
//						weight=map.get(aplist.get(j));
//					sum+=weight*(offrss-onrss)*(offrss-onrss);//ÿһ��ap��ά����
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(4, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
//			double []nearestdistances=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			
//			for(int a=0;a<nearestpoints.length;a++){
//				double x=narrowedXList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
//				double y=narrowedYList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//�������
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
//		
//		static void positionRSSWeight(int c){
//			
//		}
//		
//		//��narrow
//		static void positionKWNNSingle(int c, int k, double p){
//			knn=k;
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> xList=new ArrayList<>();//��¼narrow֮��λ�õ�xy����
//			ArrayList<Double> yList=new ArrayList<>();
//			
//			for(int i=0;i<offPositionlist.size();i++){/***����С֮��ķ�Χ�еĵ� ����ŷ�Ͼ���***/
//				String pos=offPositionlist.get(i);
//				ArrayList<Double> rssvector=offRssVectorlist.get(i);
//				xList.add(offXlist.get(i));
//				yList.add(offYlist.get(i));
//				
//				double distance=0;
//				double sum=0;
//				for(int j=0;j<rssvector.size();j++){//�����������Ƿ���ͬά�ģ�
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					sum+=(offrss-onrss)*(offrss-onrss);
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(k, distanceList);//��һ������Ϊȡ��ĸ��� ����distancelist���е�λ������
//			double []nearestdistances=new double[k];
//			double []xarr=new double[k];
//			double []yarr=new double[k];
//			
//			for(int a=0;a<k;a++){
//				double x=xList.get(nearestpoints[a]);//offXlist���±겻��narrow֮����±�
//				double y=yList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
////				System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***��λ�õ����***/
//			double deviationX=result[0]-onXlist.get(c);//�������
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
//								+" deviation:"+deviation);
//		}
//		
//		//ûnarrow��
//		static void positionUsingBetterRSS(int c){
//			ArrayList<Map<String, Double>>eachPosQuaRssList=ReadOffline.eachPosQuaRssList;
//			Map<String, Double> mapon=onEachPosRssList.get(c);
//			ArrayList<Double> distanceList=new ArrayList<>();
//			Map<Double,Integer> dindexmap=new HashMap<>();//��ž���Ͷ�Ӧ��index
//
//			//�������
//			for(int i=0;i<eachPosQuaRssList.size();i++){
//				Map<String, Double> mapoff=eachPosQuaRssList.get(i);
//				double sum=0;
//				double distance=0;
//				for(int j=0;j<aplist.size();j++){
//					double on=mapon.get(aplist.get(j));
//					if(j!=aplist.size()-1)//ע��mapoff������һά
//						if(mapoff.get(aplist.get(j))!=null){
//							double off=mapoff.get(aplist.get(j));
//							sum+=(on-off)*(on-off);
//						}
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
//				dindexmap.put(distance, i);
////				System.out.println(i+" "+distance);
//			}
//			//�ҵ�����ľ��� Ĭ��knn=4
//			Collections.sort(distanceList);
//			double []ndist=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			for(int i=0;i<4;i++){
//				ndist[i]=distanceList.get(i);
//				int index=dindexmap.get(ndist[i]);
////				System.out.println("distance:"+ndist[i]+" "+offPositionlist.get(dindexmap.get(ndist[i])));	
//				xarr[i]=offXlist.get(index);
//				yarr[i]=offYlist.get(index);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//�������
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
//								+" deviation:"+deviation);
//		}
//		
//		//k�˴�Ĭ��Ϊ4
//		static void positionKWNNWithOffDensity(int c){
//			
//			ArrayList<Map<String, Double>>densityRssList=ReadOffline.densityRssList;
//			ArrayList<Integer>rpilist=ReadOffline.randposindexlist;
//			ArrayList<Double> distanceList=new ArrayList<>();
//			Map<Double,Integer> dindexmap=new HashMap<>();//��ž���Ͷ�Ӧ��index
//
//			for(int i=0;i<densityRssList.size();i++){//�����źſռ��е����е� �����Ӧ��ŷ�Ͼ��� һ��ѭ������һ������
//												// ���µ�i������ź�����        ��ǰ���ϵ���ź�����		
//				double distance=calculateDistance(densityRssList.get(i),onEachPosRssList.get(c));
//				distanceList.add(distance);//�������źſռ��ڵĵ���������б�
//				int index=rpilist.get(i);
//				dindexmap.put(distance, index);//densityRssList�еĵ�i������
//			}
//			Collections.sort(distanceList);
//			//for(double d:distanceList)
//				//System.out.println("distance:"+d+" "+dindexmap.get(d));	
//			
//			//������Ѱ������ĵ� ������ľ��� �ҳ���Ӧ��poslist�еĵ� �Լ�xy���� �γ��������� ���뺯������
//			double []ndist=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			for(int i=0;i<4;i++){
//				ndist[i]=distanceList.get(i);
//				int index=dindexmap.get(ndist[i]);
//				xarr[i]=offXlist.get(index);
//				yarr[i]=offYlist.get(index);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//�������
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
}
