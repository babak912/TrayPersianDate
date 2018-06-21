import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainTray {

	static ImageLoader imageLoader = new ImageLoader();

	// create a class for use TimerTask and run update image at specific time (Midnight)
	private static class MyTimeTask extends TimerTask
	{

	    public void run()
	    {
			imageLoader.updateImage();
	    }
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		imageLoader.loadImage();
    	
		// Create date format for today and tomorrow and set specific time(5 second after midnight) that run updateImage() method
		try {
			DateFormat tomorowFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat todayFormat = new SimpleDateFormat( "yyyy-MM-dd" ); 
			String todayDate = todayFormat.format(System.currentTimeMillis()).substring(0,10);
			   
			Calendar cal = Calendar.getInstance();    
			cal.setTime( todayFormat.parse(todayDate));    
			cal.add( Calendar.DATE, 1 );    
			String tomorrow=todayFormat.format(cal.getTime());    
			
			Timer timer = new Timer();
			Date date = tomorowFormat.parse(tomorrow+" 00:00:05");
			
			
			timer.schedule(new MyTimeTask(), date);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
		

	}

}
