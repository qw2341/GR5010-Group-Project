import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.HashMap;
/**
 * DataReader - TODO Description
 * 
 * Bugs: none known
 * @author Qingqi Wu
 * @version 1.0
 * 
 */
public class DataReader {
    public List<Calendar> dates;
    
    public HashMap<String,Double[]> stocks;

  public DataReader(String fileName) {
    System.out.println("Reading Data");
    String line = "";  
    String splitBy = ","; 
    stocks = new HashMap<>();
    
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    
    
    try   
    {  
    //parsing a CSV file into BufferedReader class constructor  
    BufferedReader br = new BufferedReader(new FileReader(fileName));  
    //read first line, the dates
    line = br.readLine();
    String[] rawDates = line.split(splitBy);
    dates = new ArrayList<Calendar>();
    for (int i=2;i<rawDates.length; i++) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(sdf.parse(rawDates[i]));
      dates.add(cal);
    }
    
   
    //read rest
    while ((line = br.readLine()) != null)   //returns a Boolean value  
    {  
    String[] stockInfo = line.split(splitBy);    // use comma as separator  
    //add name
    String ticker = stockInfo[0].split(" ")[0];
    
    Double[] prices = new Double[stockInfo.length-2];
    for(int i=2;i<stockInfo.length;i++) {
      double price;
       try {
         price = Double.parseDouble(stockInfo[i]);
       } catch (Exception e) {
         price = -1;//-1 to indicate invalid entries
       }
       prices[i-2]=price;
    }
    stocks.put(ticker,prices); 
    //System.out.println("Parsed "+ticker+" with a length of "+prices.length);
    }  
    
    br.close();
    }   
    catch (IOException e)   
    {  
    e.printStackTrace();  
    } catch (ParseException e) {
      System.out.println("Error occured while adding dates!");
      e.printStackTrace();
    } 
    
    System.out.println("Complete");
  }
  
}
