package com.example.intel.getmapcurrentlocation;


    import android.app.Application;
    import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

    import org.acra.ACRA;
    import org.acra.ReportingInteractionMode;
    import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(mailTo = "manik0078@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
    public class MyApplicaton extends Application {
        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            ACRA.init(this);
            MultiDex.install(this);
        }
    }

