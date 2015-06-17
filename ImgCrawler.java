

/**
 *
 * @author abhishek
 */import java.net.UnknownHostException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import org.jsoup.Jsoup;
import java.sql.*;
class ImageCrawler {
    
    
    /* function to check whther this url is in database or not */
    public boolean dbOperation(String checkUrl)
    {
        boolean flag=false;
        try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
      
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/imgcrawl","root","");
        Statement st=conn.createStatement();
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
            if(url.indexOf("://")==-1)
            {
                url="http://"+url;
            }
            
         try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
      
        Connection conn=DriverManager.getConnection("jdbc:mysql://linux-pc:3306/imgcrawl","root","");
        Statement st=conn.createStatement();
        st.executeUpdate("insert into imglinks (`imgurl`,`imgname`) values ('"+url+"','"+alt+"')  ");
        
      
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        }
    }
    public String replaceHttp(String r)
    {
       /* trim of http:// and https:// from the url for regex match */
           
        return r.substring(r.indexOf("://")+3,r.length());
    }
    public static void main(String arfs[])
    {
        Pattern pat = null;
        Matcher mat=null;
        Scanner scan=new Scanner(System.in);
        String url;
        ImageCrawler icrawl=new ImageCrawler();
        
        
        try
        {
            url=scan.nextLine();
            if(!icrawl.dbOperation(url))
            {
                Document doc=Jsoup.connect(url).get();
                Elements allImages=doc.select("img");
                for(Element image: allImages)
                {
                     icrawl.dbInsertLink(image.attr("src"),image.attr("alt"));
                             
                     
                }
            }
            else
            {
                System.out.println("Do not Crawl");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
