package test2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class Tools {
	
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
			System.out.println("min:"+min+" "+narr[i]);
			darr[narr[i]]=1000.0;//���Ѿ��ҵ�����С�����Ϊһ���ܴ��ֵ
			i++;
		}
		//for(int k=0;k<darr.length;k++) System.out.println(dlist.get(k));
		return narr;
	}
	
	public static double calculateDistance(Map<String, Double> mapoff, Map<String, Double> mapon, List<String> aplist){//������������֮��ľ���
		double sum=0;
		for(int i=0;i<aplist.size();i++){
			double on=mapon.get(aplist.get(i));
			double off=-100.0;
			if(i!=aplist.size()-1)
				off=mapoff.get(aplist.get(i));
			sum+=(on-off)*(on-off);
		}
		return Math.sqrt(sum);
	}
	
	public static double[] calculatePosition(double []xarr,double []yarr,double []darr,int length, double p){
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
	
	public static double calculateDeviation(double xC, double yC, double xO, double yO){
		double d=(xC-xO)*(xC-xO)+(yC-yO)*(yC-yO);
		return Math.sqrt(d);
	}
	
	/**������������ͷ���*/
	static void calculateOverAllDeviationAndVariance(double []deviationArr){
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
	
	/**����һ����С�������ռ��λ�������� �����narrow �ǵ�����offdensity�а��ܶȵ�Ϊ130*/
	@Deprecated
	static ArrayList<Integer> narrowSearchField(ArrayList <ArrayList<Integer>> offApVectorlist,
			ArrayList <ArrayList<Integer>> onApVectorlist,int pointid){//����һ����С�������ռ��λ��������
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
		nearestPosList=Tools.findSimilarVector(similarityArr);
//		for(int i=0;i<nearestPosList.size();i++){
//			System.out.println(i+" "+offPositionlist.get(nearestPosList.get(i)));
//		}
		return nearestPosList;
	}
	
	public static ArrayList<Integer> reduceField(ArrayList <Integer[]> offApVectorlist,
												Integer[] onApVector){//����һ����С�������ռ��λ��������
		ArrayList<Integer> invaildPosList;//������
		int[] similarityArr=new int[130];//�ֱ��ap���ֵĴ������м���
		cleanArr(similarityArr);
		int posid=0;
		for(Integer[]offApVector:offApVectorlist){//��130��λ�ý��б���
			int k=0;
			for(Integer j:onApVector){//�����Ե�pointid�����ap�б���бȽ�
				//System.out.println(j+" "+apvector.get(k));//apvector�����Ǵ������޵�01
				if(offApVector[k++].equals(j)){
					similarityArr[posid]++;
				}
			}
			posid++;
		}
//		showArr(similarityArr);
		invaildPosList=Tools.findSimilarVector(similarityArr);
//		for(int i=0;i<nearestPosList.size();i++){
//			System.out.println(i+" "+offPositionlist.get(nearestPosList.get(i)));
//		}
		return invaildPosList;
	}
	
	static ArrayList<Integer> findSimilarVector(int[] sArr){
		ArrayList<Integer> spos=new ArrayList<>();
		for(int i=0;i<sArr.length-1;i++){
			if(sArr[i]>24){/***********���������һ����Ҫ���̵Ĳ��� ���������ж�������*************/
				spos.add(i);
			}
			//System.out.println(i+" similar:"+sArr[i]);
		}
		return spos;
	}
	
	public static ArrayList<Integer> findUnSimilarVector(int[] sArr){
		ArrayList<Integer> invalid=new ArrayList<>();
		for(int i=0;i<sArr.length;i++){
			if(sArr[i]<24){/***********���������һ����Ҫ���̵Ĳ��� ���������ж�������*************/
				invalid.add(i);
			}
			//System.out.println(i+" unsimilar:"+sArr[i]);
		}
		return invalid;
	}
	
	public static TreeSet generateRandArr(double d,int bound){
		if(d>1||d<0){
			System.out.println("wrong density");
			return null;
		}
		
		Random rand=new Random();
		int num=(int) (bound*d);
		TreeSet<Integer> set=new TreeSet<>();
		while(set.size()!=num){
			int r=rand.nextInt(bound);
			if(!set.contains(r))
				set.add(r);
		}
		return set;
	}
	
	public static void displayAllRSS(ArrayList<ArrayList<Map<String, Double>>> offRssList, List<String>aplist){
		for(int i=0;i<offRssList.size();i++){
			System.out.println(i+1+"th pos"+ Constant.OFF_POS_ARR[i]+"***********************");
			ArrayList<Map<String,Double>> eachpos=offRssList.get(i);
			for(int j=0;j<eachpos.size();j++){
				Map<String,Double> eachtime=eachpos.get(j);
				System.out.println("time "+j);
				for(String ap:aplist){
					if(eachtime.get(ap)!=null)
						System.out.println(ap+" "+eachtime.get(ap)+"dBm");
				}
				
			}
		}
	}
	
	public static void showList(List<String> list){
		System.out.println("size="+list.size());
		for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}
	
	public static void showMapList(List<Map<String,Double>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(Map<String,Double> map:list){
			System.out.println(Constant.OFF_POS_ARR[i++]);
			for(String ap:map.keySet())
				System.out.println(ap+" "+map.get(ap));
		}
	}
	
	public static void showArr(double[] pArr){
		System.out.println("size="+pArr.length);
		for(int i=0;i<pArr.length;i++){
			System.out.println(pArr[i]);
		}
	}
	
	public static void showArr(int[] pArr){
		System.out.println("size="+pArr.length);
		for(int i=0;i<pArr.length;i++){
			System.out.println(pArr[i]);
		}
	}
	
	public static void cleanArr(int[] cnt, double[] sum){
		for(int i=0;i<cnt.length;i++){
			cnt[i]=0;
			sum[i]=0;
		}
	}
	
	public static void cleanArr(double[] arr){
		for(int i=0;i<arr.length;i++){
			arr[i]=0;
		}
	}
	
	public static void cleanArr(int[] arr){
		for(int i=0;i<arr.length;i++){
			arr[i]=0;
		}
	}
	
	public static void cleanArr(Integer[] arr){
		for(int i=0;i<arr.length;i++){
			arr[i]=0;
		}
	}
	
	public static void showSet(Set set){
		System.out.println("Set size is "+set.size());
		for(Object i:set)
			System.out.println(i);
	}
	public static void main(String[] args) {
//		System.out.println(calculateDeviation(0, 0, 1, 1));
	}
	
}
