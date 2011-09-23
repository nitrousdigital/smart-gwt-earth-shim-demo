package com.nitrous.earth.smartgwt.shim.demo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.nitrous.gwt.earth.client.api.GEHtmlStringBalloon;
import com.nitrous.gwt.earth.client.api.GELayerId;
import com.nitrous.gwt.earth.client.api.GEPlugin;
import com.nitrous.gwt.earth.client.api.GEPluginReadyListener;
import com.nitrous.gwt.earth.client.api.GoogleEarthWidget;
import com.nitrous.gwt.earth.client.api.KmlAltitudeMode;
import com.nitrous.gwt.earth.client.api.KmlLookAt;
import com.nitrous.gwt.earth.client.api.KmlObject;
import com.nitrous.gwt.earth.client.api.KmlPlacemark;
import com.nitrous.gwt.earth.client.api.KmlPoint;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SmartGwtEarthShimDemo implements EntryPoint {

    private GoogleEarthWidget earth;
    public void onModuleLoad() {
        // construct the UI widget
        earth = new GoogleEarthWidget();

        // register a listener to be notified when the earth plug-in has loaded
        earth.addPluginReadyListener(new GEPluginReadyListener() {
            public void pluginReady(GEPlugin ge) {
                // show map content once the plugin has loaded
                loadMapContent();
            }

            public void pluginInitFailure() {
                // failure!
                Window.alert("Failed to initialize Google Earth Plug-in");
            }
        });

           
         earth.setWidth("100%");
         earth.setHeight("100%");
         RootLayoutPanel.get().add(earth);

        // begin loading the Google Earth Plug-in
        earth.init();
    }
     
    /**
     * Display content on the map
     */
    private void loadMapContent() {
        // The GEPlugin is the core class and is a great place to start browsing the API
        GEPlugin ge = earth.getGEPlugin();
        ge.getWindow().setVisibility(true);
        
        // show some layers
        ge.enableLayer(GELayerId.LAYER_BUILDINGS, true);
        ge.enableLayer(GELayerId.LAYER_BORDERS, true);
        ge.enableLayer(GELayerId.LAYER_ROADS, true);
        ge.enableLayer(GELayerId.LAYER_TERRAIN, true);
        ge.enableLayer(GELayerId.LAYER_TREES, true);

        // show an over-view pane
        ge.getOptions().setOverviewMapVisibility(true);

        // plot a placemark
    	final String placemarkId = "MyPlacemark1";
        final KmlPlacemark placemark = ge.createPlacemark(placemarkId);
		KmlPoint kmlPoint = ge.createPoint("");
		kmlPoint.setLatLng(34.73D, -86.59D);
		kmlPoint.setAltitudeMode(KmlAltitudeMode.ALTITUDE_CLAMP_TO_GROUND);
		placemark.setGeometry(kmlPoint);
		
		// configure a popup balloon for the placemark
		final GEHtmlStringBalloon balloon = ge.createHtmlStringBalloon("MyBalloon1");
		balloon.setContentString("This is a test");
		balloon.setFeature(placemark);
		ge.getFeatures().appendChild(placemark);
		
		// give the map 2 seconds to pan and then show the balloon
		Timer timer = new Timer(){
			@Override
			public void run() {
				earth.getGEPlugin().setBalloon(balloon);
			}
		};
		timer.schedule(2000);
		
		// look at the placemark
		KmlLookAt lookAt = ge.getView().copyAsLookAt(KmlAltitudeMode.ALTITUDE_RELATIVE_TO_GROUND);
		lookAt.setLatitude(kmlPoint.getLatitude());
		lookAt.setLongitude(kmlPoint.getLongitude());
		lookAt.setRange(500000D);		
		ge.getView().setAbstractView(lookAt);
		
		//Popup a window
		showSmartGwtWindow();
		
		
		// move the placemark once every second
		Timer t = new Timer() {
			@Override
			public void run() {
				KmlObject obj = earth.getGEPlugin().getElementById(placemarkId);				
				
				KmlPlacemark placemark =  (KmlPlacemark)obj;
				KmlPoint point = (KmlPoint)placemark.getGeometry();
				point.setLatitude(point.getLatitude()+.1D);
				
				
			}
		};
		t.scheduleRepeating(1000);
    }
    
	/**
	 * This is how to display a SmartGWT window over the top of the Google Earth
	 * Plugin using the 'shim' technique. The shim technique places an IFrame
	 * infront of the earth plugin but behind the SmartGWT window by configuring
	 * the absolute position, size and z-index of the iframe.
	 */
    private void showSmartGwtWindow() {
    	final com.smartgwt.client.widgets.Window window = new com.smartgwt.client.widgets.Window();
    	com.smartgwt.client.widgets.Label label 
    		= new com.smartgwt.client.widgets.Label(  
    			"<b>Window test</b><br>This window is visible since we have an IFrame (shim) between the Window and the Google Earth Plugin.");  
	     label.setHeight100();  
	     label.setPadding(5);  
	     label.setValign(com.smartgwt.client.types.VerticalAlignment.TOP);  
		 window.setTitle("Shim test");  
         window.setWidth(300);  
         window.setHeight(100);  
         window.setCanDragReposition(false);  
         window.setCanDragResize(false);  
         window.setShowMinimizeButton(false);
         window.setShowMaximizeButton(false);
         window.addItem(label);
         window.show();
         window.setZIndex(Integer.MAX_VALUE);
         window.centerInPage();        
         
         Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
		        final IFrameElement iframe = Document.get().createIFrameElement();
		        
		        // initialize the window to be the same size and position as the window
		        // but with a smaller z-index value 
				int top = window.getAbsoluteTop();
				int left = window.getAbsoluteLeft();
				int width = window.getWidth();
				int height = window.getHeight();
		        iframe.setAttribute("style", 
		        		"z-index: "+(Integer.MAX_VALUE-1)+";" +
		        		" width: "+width+"px;" +
		        		" height: "+height+"px;" +
		        		" position: absolute;" +
		        		" left: "+left+"px;" +
		        		" top: "+top+"px;");	
		        
		        // add the iframe to the document body
		        Document.get().getBody().appendChild(iframe);
		         
		        // remove the iframe when the window is closed		        
		        window.addCloseClickHandler(new CloseClickHandler() {					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						iframe.removeFromParent();
					}
				});
			}
		});
    }

}
