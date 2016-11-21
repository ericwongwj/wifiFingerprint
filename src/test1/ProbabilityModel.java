package test1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProbabilityModel {

	/*
	 * ����ȡ����offline��rss��ÿһ��ap���и��ʷֲ����
	 * ֱ��ͼ��ģ ��rss i��ap j��ֵ ���Խ���ͳ�Ƶõ����� ���ܻ��غϵıȽ϶�
	 * ��̬�ֲ� �� rssij ��-40~-110�������� �õ��ֲ�ֱ��ͼ ����matlab������� ���ݵ���ܲ��㣿
	 * ��ÿһ�����µ���д����õ�ÿһ�����rss�ĸ��ʷֲ�
	 * �����ϵĵ㣬rss��ÿ��ap�ķ����������� �����ÿ�������ĸ���
	 * �Ը���ȡ��󣨵�n����ֵ  ����õ����ݽ��м��㡣
	 * */
	static String OfflinePath="C:\\Users\\Eric\\Desktop\\jssec\\fingerprint\\fingerprint\\1.5meters.offline.trace.txt";
	static String OnlinePath="C:\\Users\\Eric\\Desktop\\jssec\\fingerprint\\onlinerandom1.txt";
	static String outPath="C:\\Users\\Eric\\Desktop\\jssec\\fingerprint\\offlineP1.txt";
	static BufferedReader br;
	static Pattern rss_pattern=Pattern.compile(Constant.rss_regex);
	static Pattern pos_pattern=Pattern.compile(Constant.pos_regex);
	static Matcher pos_matcher;
	
	static ArrayList <ArrayList<Double[][]>> offRssList=new ArrayList<>();//130���� ÿ����28��ap ap�ڲ���value��times
	static ArrayList<Double[]> OnRssList=new ArrayList<>();
	static List <String> aplist=Arrays.asList(Constant.aparr);
	static List <String> onPositionlist=Arrays.asList(Constant.onPosArr);
	static ArrayList <Double> onXlist=new ArrayList<>();
	static ArrayList <Double> onYlist=new ArrayList<>();
	static ArrayList <String> offPositionlist=new ArrayList<>();
	static ArrayList <Double> offXlist=new ArrayList<>();
	static ArrayList <Double> offYlist=new ArrayList<>();

	public static void main(String[] args) {
		readOff();
//		displayOffRss();
		readRandomOn();
		positioning();
	}
	
	public static void readOff() {
		initOffPos();
		try {
			File file=new File(outPath);
			FileWriter fw = new FileWriter(file,true);//��ĩβ���
		
			String line;
			int i=0,j=0,t=0,p=0;
			double [][]onePosRssArr=new double[110][29];
			while((line=br.readLine())!=null ){//&& i<140
				if(i>0&&i<13&&j==0){//					
				}else if(i==13&&j==0){
					i=j=1;					
				}
				else if(i%116>=0 && i%116<=5){//�����ʽ ����ͷβ
						//System.out.println("ignore");//1-17 18-127 128 
				}
				else{//130��110��
					String target=line.replace(Constant.fixedid, "");
					
					getEachRSS(target, onePosRssArr,t);
					t++;
					
					if((i+1)%116==0){//ÿ����һ���� ����һ����ķֲ�
						String s="position"+p+" : "+offPositionlist.get(p);//+"\r\n"
						
//						fw.write(s);
						//System.out.println(s);
						p++;
						createHistogram(onePosRssArr,fw);
						t=0;
					}
				}/***else���� ������һ��***/
				i++;//������һ
			}//end while ��ȡ����				
			
//			fw.close();
			System.out.println("Offline reading completed.");
			
		}catch (FileNotFoundException e) {
				e.printStackTrace();
		}catch (IOException e) {
				e.printStackTrace();
		}
	}
		
	public static void getEachRSS(String target, double[][]arr, int t){//110*29
		Matcher m=rss_pattern.matcher(target);//m1 mac��ַ  m2�ź�ǿ��
		while(m.find()){/***��ȡĳһ�е���Ч��Ϣ***/
			String each_ap="00:"+m.group(1);
			int k=aplist.indexOf(each_ap);
			double rss=Double.valueOf(m.group(2));
			arr[t][k]=rss;//t 0-109  k 0-28  ����Ϊ0�ĸ�����Ϊapmiss
		}
	}
	
	public static void createHistogram(double [][]arr, FileWriter fw){//����
		ArrayList<Double> plist=new ArrayList<>();
		ArrayList<Double> valuelist=new ArrayList<>();
		ArrayList<Double> timeslist=new ArrayList<>(); 
		ArrayList<Double[][]>onePosRssList=new ArrayList<>();
            
		for(int i=0;i<29;i++){
			for(int j=0;j<110;j++){
				if(valuelist.contains(arr[j][i])){
					int idx=valuelist.indexOf(arr[j][i]);
					double times=timeslist.get(idx);
					times++;
					timeslist.set(idx, times);
				}else{
					valuelist.add(arr[j][i]);
					timeslist.add(1.0);
				}
			}
			String s1="AP  "+aplist.get(i)+"***********************************";//\r\n"
			//System.out.println(s1);

//			try {
//				fw.write(s1);
				
			for(int a=0;a<timeslist.size();a++){
				double t=timeslist.get(a);
				double p=t/110;
				plist.add(p);
			}
			
			Double [][]aprssArr=new Double[valuelist.size()][2];//value--times
			for(int b=0;b<valuelist.size();b++){
				String s2="value:"+valuelist.get(b)+" times:"+timeslist.get(b)+" P="+plist.get(b);//+"\r\n";
//				fw.write(s2);
				//System.out.println(s2);
				aprssArr[b][0]=valuelist.get(b);
				aprssArr[b][1]=timeslist.get(b);
			}
			
			onePosRssList.add(aprssArr);
			
//			}catch (IOException e) {
//				e.printStackTrace();
//			}
			plist=new ArrayList<>();
			valuelist=new ArrayList<>();
			timeslist=new ArrayList<>();
		}
		
		offRssList.add(onePosRssList);
		
		for(int i=0;i<110;i++){
			//System.out.println("time"+i+"********************************************");
			for(int j=0;j<29;j++){
				//System.out.println(arr[i][j]);
				arr[i][j]=0;
			}
		}
	}
	
	public static void displayOffRss(){
		for(int i=0;i<130;i++){
			System.out.println("Position :"+offPositionlist.get(i)+"  ****************");
			ArrayList<Double[][]>apinfolist=offRssList.get(i);//29
			
			for(int j=0;j<apinfolist.size();j++){
				Double[][]arr=apinfolist.get(j);
				System.out.println(aplist.get(j)+"*********************************************");
				
				for(int k=0;k<arr.length;k++)
					System.out.println("value:"+arr[k][0]+" times:"+arr[k][1]);
			}
		}
	}

	public static void positioning(){
		for(int i=2;i<46;i++){
			Double []onrssArr=OnRssList.get(i);//size=29
			double []product=new double[130];
			for(int s=0;s<130;s++)
				product[s]=1;
			for(int j=0;j<130;j++){
				ArrayList<Double[][]> offrss=offRssList.get(j);//��СΪ29
				for(int k=0;k<29;k++){
					Double [][]aprss=offrss.get(k);
//					System.out.println(aprss.length);
					for(int p=0;p<aprss.length;p++){
						//System.out.println(aprss[p][0]+" "+aprss[p][1]+" "+onrssArr[k]);
						if(aprss[p][0].equals(onrssArr[k])){
							product[j]*=aprss[p][1];//�п�����Ҫ������
//							System.out.println("yes"+product[j]);

						}
					}
				}
			}
			//Ȼ���ҵ����ֵ ��index ��Ϊλ��
			double []probs=new double[2];
			int []index=getNMax(product,2,probs);
			
			double []result=calculatePosition(index, probs, 1);
			double d=calculateDeviation(result[0], result[1], onXlist.get(i), onYlist.get(i), i);
			System.out.println(onPositionlist.get(i)+" Most likely position is "+result[0]+","+result[1]+" deviation is "+d);
		}
		double sum=0;
		for(int i=2;i<46;i++)
			sum+=deviations[i];
		System.out.println("average deviation is "+sum/44);
	}

	public static int[] getNMax(double []darr,int n,double []probs){
		
		int []idx=new int[n];
		int i=0;
		while(i<n){
			int index=0;
			double max=darr[0];
			for(int j=0;j<darr.length-1;j++){
				if(max<darr[j+1]){
					max=darr[j+1];
					index=j+1;
				}
			}
			idx[i]=index;
			probs[i]=max;
			darr[index]=-1000.0;//���Ѿ��ҵ�����С�����Ϊһ���ܴ��ֵ
			i++;
		}
		return idx;
	}
	
	static double[] calculatePosition(int []index,double []probabilities, double p){
		double []xarr={offXlist.get(index[0]),offXlist.get(index[1])};//,offXlist.get(index[2]),offXlist.get(index[3])
		double []yarr={offYlist.get(index[0]),offYlist.get(index[1])};//,offYlist.get(index[2]),offYlist.get(index[3])

		double[] result=new double[2];//0:x 1:y
		double wtotal=0;
		for(int i=0;i<index.length;i++){
			wtotal+=Math.pow(probabilities[i], p);//��������
		}
		for(int i=0;i<index.length;i++){
			result[0]+=xarr[i]*probabilities[i]/wtotal;
			result[1]+=yarr[i]*probabilities[i]/wtotal;
		}	
		return result;
	}
	
	static double []deviations=new double[46];
	public static double calculateDeviation(double x1,double y1,double x2,double y2,int i){
		double sum=(x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
		double deviation=Math.sqrt(sum);
		deviations[i]=deviation;
		return deviation;
	}
	
	public static void initOffFile(){
		File file=new File(OfflinePath);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void initOffPos(){
		initOffFile();
		try {
			int i=0,j=0;
			String line,each_pos = null;
			while((line=br.readLine())!=null){//&& i<1400
				if(i>0&&i<13&&j==0);
				else if(i==13&&j==0){
					i=j=1;
				}else if(i%116==6){//ÿ110��һ��ͷ �ȵõ���ǰλ�� �磨-23.5��-10.75��
					pos_matcher=pos_pattern.matcher(line);
					if(pos_matcher.find()){
						each_pos=pos_matcher.group(1);
						offPositionlist.add(each_pos);
					}
					Pattern p=Pattern.compile("(.*?),(.*?),0.0");
					Matcher m=p.matcher(each_pos);
					if(m.matches()){
						offXlist.add(Double.valueOf(m.group(1)));
						offYlist.add(Double.valueOf(m.group(2)));
					}
				} 
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("aplist: "+aplist.size()+"   positionlist: "+offPositionlist.size());
		initOffFile();
	}
	
	public static void readRandomOn(){
		File file=new File(OnlinePath);
		Pattern pp=Pattern.compile("(.*?),(.*?),0.0");
		for(int i=0;i<46;i++){
			Matcher m=pp.matcher(onPositionlist.get(i));
			if(m.matches()){
				onXlist.add(Double.valueOf(m.group(1)));
				onYlist.add(Double.valueOf(m.group(2)));
			}
		}
		
		try {			
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			BufferedReader brr=new BufferedReader(isr);
			String line = null;
			for(String s:onPositionlist){
				Pattern p=Pattern.compile("(.*?),(.*?),0.0");
				Matcher m=p.matcher(s);
				if(m.matches()){
					onXlist.add(Double.valueOf(m.group(1)));
					onYlist.add(Double.valueOf(m.group(2)));
				}
			}
			int i=0;
			Double []arr=new Double[29];
			while((line=brr.readLine())!=null){//&& i<1400
				//System.out.println(i+" "+line);
				if(i!=0){//0-29 30-59 60-89
					Pattern p1=Pattern.compile("00:\\w\\w:\\w\\w:\\w\\w:\\w\\w:\\w\\w:(.*?)");
					Matcher m1=p1.matcher(line);//m1 mac��ַ  m2�ź�ǿ��
					if(m1.matches()){
						double rss=Double.valueOf(m1.group(1));
						arr[i-1]=rss;//t 0-109  k 0-28  ����Ϊ0�ĸ�����Ϊapmiss
					}
					if(i==29){
						OnRssList.add(arr);
						arr=new Double[29];
						i=-1;
					}
				}				
				i++;	
			}
			
//			int u=0;
//			for(Double []darr:OnRssList){
//				System.out.println(onPositionlist.get(u));u++;
//				for(int p=0;p<darr.length;p++)
//					System.out.println(darr[p]);
//			}
			
			System.out.println("Online reading completed.");
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
