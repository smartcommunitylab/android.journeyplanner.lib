package eu.trentorise.smartcampus.jp;

import android.content.Context;
import android.view.View;
import android.widget.HorizontalScrollView;

public class HorizzontalDetectEndScrollView extends HorizontalScrollView 
    { 

      public HorizzontalDetectEndScrollView(Context context) 
      { 
        super(context); 
      } 

      @Override 
      protected void onScrollChanged(int l, int t, int oldl, int oldt) 
      { 
    	  // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
          View view = (View) getChildAt(getChildCount()-1);          
          // Calculate the scrolldiff
          int diff = (view.getBottom()-(getHeight()+getScrollY()));          
          // if diff is zero, then the bottom has been reached
          if( diff == 0 )
          {
                  // notify that we have reached the right or the left
                  // Toast.makeText(getSherlockActivity(), "fine",Toast.LENGTH_SHORT).show();
          }

       
    }
    }