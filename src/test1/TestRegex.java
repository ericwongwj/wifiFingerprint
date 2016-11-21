package test1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestRegex {
	public static void main(String[] args) {
		String line="t=1187889149712;"
				+ "pos=-23.5,-10.75,0.0;"
				+ "id=00:02:2D:21:0F:33;"
				+ "00:14:BF:B1:7C:54=-50,2.412E9,3,-96;"
				+ "00:11:88:5A:31:50=-55,2.462E9,3,-97;"
				+ "00:16:B6:B7:5D:8F=-64,2.412E9,3,-96;";
		String fixedid="id=00:02:2D:21:0F:33;";
		String target=line.replace(fixedid, "");
		System.out.println("line after deletion: "+target);
		String rss_regex="00(.*?)=(.*?),";
		Pattern p=Pattern.compile(rss_regex);
		Matcher m=p.matcher(target);
		int sum=0;
		while(m.find()){
			System.out.println(m.group(1)+" "+m.group(2));
			int a=Integer.valueOf("-50");
			System.out.println(a);
		}
		
		Double[] d=new Double[2];
		System.out.println("d:"+d[0]);
		
		String st="00:14:BF:B1:7C:54:-53.0";
		Pattern p1=Pattern.compile("00:\\w\\w:\\w\\w:\\w\\w:\\w\\w:\\w\\w:(.*?)");
		Matcher m1=p1.matcher(st);
		if(m1.matches())
			System.out.println("found"+m1.group(1));
	}
	
	
}
