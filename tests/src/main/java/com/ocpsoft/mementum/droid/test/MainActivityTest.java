package com.ocpsoft.mementum.droid.test;

import android.test.ActivityInstrumentationTestCase2;

import com.ocpsoft.mementum.droid.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>
{

   public MainActivityTest()
   {
      super(MainActivity.class);
   }

   public void testActivity()
   {
      MainActivity activity = getActivity();
      assertNotNull(activity);
   }
}
