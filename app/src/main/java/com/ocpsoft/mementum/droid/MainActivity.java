package com.ocpsoft.mementum.droid;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.ocpsoft.mementum.droid.poll.LocationObserver;
import com.ocpsoft.mementum.droid.poll.LocationPollingService;
import com.ocpsoft.mementum.droid.poll.LocationPollingService.ServiceBinder;
import com.ocpsoft.mementum.droid.task.SaveLocation;
public class MainActivity extends Activity implements LocationObserver {
  public static final String LOC_PREF_PREFIX="LOCATION.";
  private static final int STOP=0;
  private static final int START=1;
  private static final int ADD_LOCATION=2;
  private static final int SHOW_LOCATIONS=3;
  protected static LocationPollingService pollingService;
  private TextView latitude;
  private TextView longitude;
  private TextView bearing;
  private TextView accuracy;
  private Intent locationPollingIntent;
  /** 
 * Called when the activity is first created. 
 */
  @Override public void onCreate(  final Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    locationPollingIntent=LocationPollingService.getIntent(this);
    setContentView(R.layout.main);
    latitude=(TextView)findViewById(R.id.latitude);
    longitude=(TextView)findViewById(R.id.longitude);
    bearing=(TextView)findViewById(R.id.bearing);
    accuracy=(TextView)findViewById(R.id.accuracy);
    startService(locationPollingIntent);
    bind();
  }
  @Override public boolean onMenuOpened(  final int featureId,  final Menu menu){
    return super.onMenuOpened(featureId,menu);
  }
  @Override public boolean onCreateOptionsMenu(  final Menu menu){
    MenuItem start=menu.add(0,START,0,"Start Service");
    MenuItem stop=menu.add(0,STOP,1,"Stop Service");
    MenuItem here=menu.add(0,ADD_LOCATION,2,"Save My Location");
    MenuItem show=menu.add(0,SHOW_LOCATIONS,3,"Show Saved Locations");
    return super.onCreateOptionsMenu(menu);
  }
  @Override public boolean onMenuItemSelected(  final int featureId,  final MenuItem item){
    if (STOP == item.getItemId()) {
      unbind();
      stopService(locationPollingIntent);
      Toast.makeText(this,"GPS Polling Stopped.",Toast.LENGTH_SHORT).show();
    }
 else     if (START == item.getItemId()) {
      startService(locationPollingIntent);
      bind();
      Toast.makeText(this,"GPS Polling Started.",Toast.LENGTH_SHORT).show();
    }
 else     if (ADD_LOCATION == item.getItemId()) {
      SaveLocation task=new SaveLocation(this);
      task.execute();
    }
 else     if (SHOW_LOCATIONS == item.getItemId()) {
      startActivity(new Intent(this,ShowLocationsActivity.class));
    }
 else {
      return super.onMenuItemSelected(featureId,item);
    }
    return true;
  }
  @Override public void onPause(){
    unbind();
    super.onPause();
  }
  @Override public void onResume(){
    bind();
    super.onResume();
  }
  @Override protected void onDestroy(){
    unbind();
    super.onDestroy();
  }
  private void bind(){
    bindService(locationPollingIntent,serviceConnection,BIND_AUTO_CREATE);
  }
  private void unbind(){
    try {
      pollingService.removeObserver(MainActivity.this);
      unbindService(serviceConnection);
    }
 catch (    Exception e) {
      Log.d(MainActivity.class.getSimpleName(),"Could not unbind from service because: " + Log.getStackTraceString(e));
    }
  }
  private final ServiceConnection serviceConnection=new ServiceConnection(){
    public void onServiceConnected(    final ComponentName name,    final IBinder binder){
      ServiceBinder serviceBinder=(ServiceBinder)binder;
      pollingService=serviceBinder.getService();
      pollingService.addObserver(MainActivity.this);
    }
    public void onServiceDisconnected(    final ComponentName name){
      pollingService=null;
    }
  }
;
  public void updateLocation(  final Location loc){
    if (loc != null) {
      latitude.setText("Latitude:\n " + loc.getLatitude());
      longitude.setText("Longitude:\n " + loc.getLongitude());
      bearing.setText("Bearing:\n " + loc.getBearing());
      accuracy.setText("Accuracy:\n " + loc.getAccuracy());
    }
  }
  public long getRequestedInterval(){
    return 30000;
  }
}
