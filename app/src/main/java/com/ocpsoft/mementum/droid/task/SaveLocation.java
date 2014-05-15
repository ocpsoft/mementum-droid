/*
 * Copyright 2011, Lincoln Baxter, III - All Rights Reserved.
 */
package com.ocpsoft.mementum.droid.task;

import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ocpsoft.mementum.droid.MainActivity;
import com.ocpsoft.mementum.droid.poll.LocationObserver;
import com.ocpsoft.mementum.droid.poll.LocationPollingService;
import com.ocpsoft.mementum.droid.poll.LocationPollingService.ServiceBinder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SaveLocation extends AsyncTask<Void, Void, Location> implements LocationObserver
{
   protected static ProgressDialog progress;
   private LocationPollingService pollingService;
   private Location location;

   private final Activity activity;

   public SaveLocation(final Activity context)
   {
      activity = context;
   }

   public void updateLocation(final Location location)
   {
      this.location = location;
   }

   @Override
   protected void onPreExecute()
   {
      // runs in UI thread
      activity.bindService(LocationPollingService.getIntent(activity), connection, Service.BIND_AUTO_CREATE);

      progress = new ProgressDialog(activity);
      progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      progress.setMessage("Waiting for location...");
      progress.setCancelable(true);
      progress.setOnCancelListener(new DialogInterface.OnCancelListener()
      {
         public void onCancel(final DialogInterface dialog)
         {
            pollingService.removeObserver(SaveLocation.this);
            try
            {
               activity.unbindService(connection);
            }
            catch (Exception e)
            {
               Log.d(SaveLocation.class.getSimpleName(),
                        "Could not unbind from service because: " + Log.getStackTraceString(e));
            }
            Log.d(SaveLocation.class.getSimpleName(), "Unbound from GPS Service");
            SaveLocation.this.cancel(true);
         }
      });

      progress.setOnDismissListener(new DialogInterface.OnDismissListener()
      {

         public void onDismiss(final DialogInterface dialog)
         {
            pollingService.removeObserver(SaveLocation.this);
            try
            {
               activity.unbindService(connection);
            }
            catch (Exception e)
            {
               Log.d(SaveLocation.class.getSimpleName(),
                        "Could not unbind from service because: " + Log.getStackTraceString(e));
            }
            Log.d(SaveLocation.class.getSimpleName(), "Unbound from GPS Service");
         }
      });
      progress.show();

      super.onPreExecute();
   }

   @Override
   protected Location doInBackground(final Void... contexts)
   {
      while (!isCancelled() && (location == null))
      {
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
            cancel(true);
         }
      }

      return location;
   }

   @Override
   protected void onPostExecute(final Location l)
   {
      // runs in UI thread

      if ((progress != null) && progress.isShowing())
         progress.dismiss();

      try
      {
         Log.d(SaveLocation.class.getSimpleName(), "Attempting to save location to preferences: " + l);
         SharedPreferences preferences = activity.getPreferences(Activity.MODE_PRIVATE);
         Map<String, ?> all = preferences.getAll();
         int i = 0;
         for (Entry<String, ?> e : all.entrySet())
         {
            if (e.getKey().startsWith(MainActivity.LOC_PREF_PREFIX))
               i++;
         }
         Editor edit = preferences.edit();
         edit.putString(MainActivity.LOC_PREF_PREFIX + (i + 1),
                  l.getLatitude() + "," + l.getLongitude() + "," + l.getAltitude());
         edit.commit();

         Log.d(SaveLocation.class.getSimpleName(), "Committed preferences.");
         Toast.makeText(activity, "Saved Location: " + l, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e)
      {
         Toast.makeText(activity, "Could not save Location", Toast.LENGTH_SHORT).show();
      }

      super.onPostExecute(l);
   }

   private final ServiceConnection connection = new ServiceConnection()
   {
      public void onServiceConnected(final ComponentName name,
               final IBinder binder)
      {
         ServiceBinder serviceBinder = (ServiceBinder) binder;
         pollingService = serviceBinder.getService();
         pollingService.addObserver(SaveLocation.this);
         Log.d(SaveLocation.class.getSimpleName(), "Bound to GPS Service");
      }

      public void onServiceDisconnected(final ComponentName name)
      {
         pollingService = null;
         SaveLocation.this.cancel(true);
      }
   };

   public long getRequestedInterval()
   {
      return 0;
   }

}
