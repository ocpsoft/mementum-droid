package com.ocpsoft.mementum.droid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowLocationsActivity extends ListActivity
{
   @Override
   protected void onCreate(final Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      List<Location> savedLocations = getSavedLocations();
      setListAdapter(new ArrayAdapter<Object>(this, R.layout.location_list, savedLocations.toArray()));

      ListView lv = getListView();
      lv.setTextFilterEnabled(true);

      lv.setOnItemClickListener(new OnItemClickListener()
      {
         public void onItemClick(final AdapterView<?> parent, final View view,
                  final int position, final long id)
         {
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                     Toast.LENGTH_SHORT).show();
         }
      });

   }

   private List<Location> getSavedLocations()
   {
      List<Location> result = new ArrayList<Location>();

      SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
      Map<String, ?> all = preferences.getAll();
      for (Entry<String, ?> e : all.entrySet())
      {
         if (e.getKey().startsWith(MainActivity.LOC_PREF_PREFIX))
         {
            String[] split = e.getValue().toString().split(",");
            Location loc = new Location("Custom");
            loc.setLatitude(Double.valueOf(split[0]));
            loc.setLongitude(Double.valueOf(split[1]));
            loc.setAltitude(Double.valueOf(split[2]));
            result.add(loc);
         }
      }
      return result;
   }
}
