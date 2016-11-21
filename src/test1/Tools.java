package test1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tools {
	
	//�������narrow֮���field��һ��λ�������б�
	public static int[] findNNearest(int pnum, List<Double> dlist){//param��point number 
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
			darr[narr[i]]=1000.0;
			i++;
		}
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
	public static double[] calculateALLDeviationAndVariance(double []deviationArr){
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
		
		return new double[]{avgdeviation,variance};
	}
	
	/**����һ����С�������ռ��λ��������*/
	static ArrayList<Integer> narrowSearchField(int pointid){
		ArrayList<Integer> nearestPosList;//��ŵ���Ŵ���positionlist�����
		int[] similarityArr=new int[130];//�ֱ��29��ap���ֵĴ������м���
		for(int i=0;i<130;i++)
			similarityArr[i]=0;
		int posid=0;
		/*for(ArrayList<Integer>apvector:offApVectorlist){//��130��λ�ý��б���
			int k=0;
			for(Integer j:onApVectorlist.get(pointid)){//�����Ե�pointid�����ap�б���бȽ� apvector�����Ǵ������޵�01
				if(apvector.get(k).equals(j)){
					similarityArr[posid]++;
				}
				k++;
			}
			posid++;
		}*/
		nearestPosList=findSimilarVector(similarityArr);		//�ҵ���4orN���ĵ���offpositionlist���е�λ��
		return nearestPosList;
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
	
	public static void main(String[] args) {
//		System.out.println(calculateDeviation(0, 0, 1, 1));
	}
	
}
