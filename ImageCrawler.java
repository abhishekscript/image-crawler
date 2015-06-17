/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import org.jsoup.Jsoup;
import java.sql.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
class ImageCrawler {
    
    
    /* function to check whther this url is in database or not */
    public boolean dbOperation(String checkUrl)
    {
        boolean flag=false;
        try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgcrawl","root",""); Statement st = conn.createStatement()) {
                ResultSet rs=st.executeQuery("select * from imglinks");
                while(rs.next())        
                {
                    if(checkUrl.equals(rs.getString("imgurl")))
                    {
                        flag=true;
                        break;
                    }
                }
            }
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return flag;
    }
    public void dbInsertLink(String url,String alt)
    {
        
        if(alt.length()>1)
        {
           alt=alt.replaceAll("-"," ");
           alt=alt.replaceAll("_"," ");
		   alt=alt.replaceAll("'"," ");
            
         try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
               Statement st;
               try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgcrawl","root","")) {
                   st = conn.createStatement();
                   st.executeUpdate("insert into imglinks (`imgurl`,`imgname`) values ('"+url+"','"+alt+"')  ");
               }
        st.close();
        
      
        
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e)
        {
            e.printStackTrace();
        }
        }
    }
	public boolean isLinkCrawled(String url)throws IOException
	{
		if(url.charAt(url.length()-1)=='/'||url.charAt(url.length()-1)=='#')
		url=url.substring(0,url.length()-1);
	FileWriter fw=new FileWriter("trainingData.txt",true);
	FileWriter checkPoint=new FileWriter("checkpoint.txt");
	boolean flag=false;
	try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/imgcrawl","root",""); 
			Statement st = conn.createStatement()
			
			) {
                ResultSet rs=st.executeQuery("select count(url) as rowcount from crawledlink where url ='"+url+"'");
               rs.next();
			   int count = rs.getInt("rowcount");
				if(count==0)
				{
					flag=true;
					System.out.println("New Link Found .... ");
					st.executeUpdate("INSERT INTO `crawledlink`( `url`) VALUES ('"+url+"')");
					fw.write(url+"\n");
					checkPoint.write(url+"\n");
				}
			
            
                     st.close();
		rs.close();
		conn.close();
                
            }
        checkPoint.close();
		fw.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
	
	return flag;
	}
    public String replaceHttp(String r)
    {
       /* trim of http:// and https:// from the url for regex match */
           
        return r.substring(r.indexOf("://")+3,r.length());
    }
    public static void main(String arfs[])throws IOException
    {
        Pattern pat = null;
        Matcher mat=null;
        Scanner scan=new Scanner(System.in);
        String url="";
        ImageCrawler icrawl=new ImageCrawler();
		boolean firstCheck=false;
        FileInputStream fis=null;
        Document doc=null;
        try
        {

		fis=new FileInputStream("trainingData.txt");
		InputStreamReader isr=new InputStreamReader(fis);
		BufferedReader br=new BufferedReader(isr);		
		
		
		while((url=br.readLine())!=null)
		{		
            	
			
			System.out.println("Started Crawling from "+url);
				firstCheck=true;
            		if(!icrawl.dbOperation(url))
           		 {
						try {
                		doc=Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("http://www.google.com").get();
						throw new Exception();
                		}
						catch(Exception e)
						{
						}
						Elements allImages=doc.select("img");
                		for(Element image: allImages)
                			{
				
						if(!icrawl.dbOperation(image.absUrl("src")))
                   				  icrawl.dbInsertLink(image.absUrl("src"),image.attr("alt"));
                             
                     
                			}
								
				

				Elements allLinks=doc.select("a");
				for(Element link: allLinks)
					{
					if(link.attr("abs:href").toString().length()>1 )
					
						icrawl.isLinkCrawled(link.attr("abs:href"));
						
						
						
					}
					
            		}
            		else
            		{
             			   System.out.println("Do not Crawl");
            		}
					
				 // end of checking if firs time the link was found
       		}	 // end if while loop reading over file
			throw new Exception();
	} 
        catch(Exception e)
        {
            e.printStackTrace();
			
        }
	
    }
    
}
