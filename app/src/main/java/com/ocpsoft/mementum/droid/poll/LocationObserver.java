/*
 * Copyright 2011, Lincoln Baxter, III - All Rights Reserved.
 */
package com.ocpsoft.mementum.droid.poll;

import android.location.Location;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface LocationObserver
{
   long getRequestedInterval();

   void updateLocation(Location location);
}
