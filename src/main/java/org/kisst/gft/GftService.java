package org.kisst.gft;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class GftService  implements WrapperListener  {
	protected GftRunner runner;

	public static void main(String[] args) {
		// Start the application. If the JVM was launched from the native
		// Wrapper then the application will wait for the native Wrapper to
		// call the application's start method. Otherwise the start method
		// will be called immediately.
		WrapperManager.start(new GftService(), args);
	}

	public Integer start(String[] args) {
		System.out.println("Starting GFT service with working dir "+System.getProperty("user.dir"));
		String configFile=null;
		if (args.length>0)
			configFile=args[0];
		runner= new GftRunner(configFile);
		runner.start();
		return null;
	}
	public int stop(int exitcode) {
		runner.shutdown();
		return exitcode;
	}
	
    public void controlEvent( int event )
    {
        if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
            && ( WrapperManager.isLaunchedAsService() )) //|| WrapperManager.isIgnoreUserLogoffs() ) )
        {
            // Ignore
        }
        else
        {
            WrapperManager.stop( 0 );
            // Will not get here.
        }
    }

}
