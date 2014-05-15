package com.ocpsoft.mementum.droid.poll;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class LocationPollingService extends Service
{
   private static final int DEFAULT_UPDATE_INTERVAL = 10000000;

   protected Location location;

   private final Binder binder = new ServiceBinder();
   private LocationManager locmgr = null;

   public class ServiceBinder extends Binder
   {
      public LocationPollingService getService()
      {
         return LocationPollingService.this;
      }
   }

   public Location getLocation()
   {
      return location;
   }

   @Override
   public IBinder onBind(final Intent intent)
   {
      return binder;
   }

   @Override
   public void onCreate()
   {
      Log.d(LocationPollingService.class.getSimpleName(), "Start up.");
      locmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      super.onCreate();
   }

   @Override
   public void onDestroy()
   {
      if (!this.observers.isEmpty())
      {
         cancelLocationUpdates();
      }
      Log.d(LocationPollingService.class.getSimpleName(), "Shut down.");
      super.onDestroy();
   }

   // Start a location listener
   LocationListener onLocationChange = new LocationListener()
   {
      public void onLocationChanged(final Location loc)
      {
         location = loc;
         for (LocationObserver o : observers)
         {
            o.updateLocation(loc);
         }
      }

      public void onProviderDisabled(final String provider)
      {
         // required for interface, not used
      }

      public void onProviderEnabled(final String provider)
      {
         // required for interface, not used
      }

      public void onStatusChanged(final String provider, final int status,
               final Bundle extras)
      {
         // required for interface, not used
      }
   };

   private final List<LocationObserver> observers = new ArrayList<LocationObserver>();

   public void addObserver(final LocationObserver observer)
   {
      if (!this.observers.isEmpty())
      {
         cancelLocationUpdates();
      }
      this.observers.add(observer);
      requestLocationUpdates();
   }

   public void removeObserver(final LocationObserver observer)
   {
      if (!this.observers.isEmpty())
      {
         cancelLocationUpdates();
      }
      this.observers.remove(observer);
      if (!this.observers.isEmpty())
      {
         requestLocationUpdates();
      }
   }

   private void cancelLocationUpdates()
   {
      locmgr.removeUpdates(onLocationChange);
      Log.d(LocationPollingService.class.getSimpleName(), "Cancelled GPS updates");
   }

   private void requestLocationUpdates()
   {
      long updateInterval = getUpdateInterval();
      locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0.0f, onLocationChange);
      Log.d(LocationPollingService.class.getSimpleName(), "Registered for GPS updates with interval of ["
               + updateInterval + "]");
   }

   public long getUpdateInterval()
   {
      long currentInterval = -1;
      for (LocationObserver o : observers)
      {
         if ((currentInterval == -1) || (o.getRequestedInterval() < currentInterval))
         {
            currentInterval = o.getRequestedInterval();
         }
      }
      if (currentInterval == -1)
      {
         currentInterval = DEFAULT_UPDATE_INTERVAL;
      }
      return currentInterval;
   }

   public static Intent getIntent(final Context context)
   {
      return new Intent(context, LocationPollingService.class);
   }
}
