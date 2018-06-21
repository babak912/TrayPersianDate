import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Calendar;


public class ImageLoader {
	
	final String EXIT = "خروج";
	final String COPY_DATE="ذخيره تاريخ";
	final String UPDATE_DATE="آپديت تاريخ";
	
	TrayIcon trayIcon;
	String today;

	// Based on current date load image from img folder
	public void loadImage() {

		PersianCalendar persianCalendar = new PersianCalendar(Calendar.getInstance());


		if(persianCalendar.getMonth()<12){
			persianCalendar.addMonth(1);
		}
		
		today= String.valueOf(persianCalendar.getDate());

		
		SystemTray tray = SystemTray.getSystemTray();
	     
		 PopupMenu popup = new PopupMenu();
		
		
		URL url = getClass().getResource("/img/"+persianCalendar.getDayOfMonth()+".png");
	    Image image = Toolkit.getDefaultToolkit().getImage(url);
		trayIcon = new TrayIcon(image, today, popup);
		

		    MouseListener mouseListener = new MouseListener() {
		                
		        public void mouseClicked(MouseEvent e) {
//		            System.out.println("Tray Icon - Mouse clicked!");                 
		        }

		        public void mouseEntered(MouseEvent e) {
//		            System.out.println("Tray Icon - Mouse entered!");                 
		        }

		        public void mouseExited(MouseEvent e) {
//		            System.out.println("Tray Icon - Mouse exited!");                 
		        }

		        public void mousePressed(MouseEvent e) {
//		            System.out.println("Tray Icon - Mouse pressed!");
			    
		        }

		        public void mouseReleased(MouseEvent e) {
//		            System.out.println("Tray Icon - Mouse released!");                 
		        }
		    };

		    if (SystemTray.isSupported()) {

				ActionListener exitListener = new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
//			            System.out.println("Exiting...");
			            System.exit(0);
			        }
			    };
			    
			    // Save current date to clip board menu
			    ActionListener clipListener = new ActionListener() {
			        public void actionPerformed(ActionEvent e) {

			        	StringSelection stringSelection = new StringSelection(today);
			        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			        	clipboard.setContents(stringSelection, null);
			        	
			        }
			    };
			    
			    // update image in system tray based on current date
			    ActionListener updateListener = new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {

						updateImage();
						
					}
				};
			    
			    
		            
		    // Menu whenever right click on icon in system tray
		    MenuItem updateItem = new MenuItem(UPDATE_DATE);
		    MenuItem clipboardItem = new MenuItem(COPY_DATE);
		    MenuItem exitItem = new MenuItem(EXIT);
		    

		    updateItem.addActionListener(updateListener);
		    clipboardItem.addActionListener(clipListener);
		    exitItem.addActionListener(exitListener);

		    popup.add(updateItem);
		    popup.add(clipboardItem);
		    popup.add(exitItem);

		    
		    // Uncomment whenever you want action on double click on icon 
		    /*ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            trayIcon.displayMessage("Action Event", 
		                "An Action Event Has Been Performed!",
		                TrayIcon.MessageType.INFO);
		        }
		    };*/
		            
		    trayIcon.setImageAutoSize(true);
//		    trayIcon.addActionListener(actionListener);
		    trayIcon.addMouseListener(mouseListener);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }

		} else {

		    //  System Tray is not supported.

		}

		
	}

	
	// update image of icon in system tray
	public  void updateImage()  {

		PersianCalendar updateCalendar = new PersianCalendar(Calendar.getInstance());

		if(updateCalendar.getMonth()<12){
			updateCalendar.addMonth(1);
		}
		
		today= String.valueOf(updateCalendar.getDate());

		URL url = getClass().getResource("/img/"+(updateCalendar.getDayOfMonth())+".png");
	    Image image = Toolkit.getDefaultToolkit().getImage(url);
		 trayIcon.setImage(image);
		 trayIcon.setToolTip(today);
		 
		
	}

	
}
