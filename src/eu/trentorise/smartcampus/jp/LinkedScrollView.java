package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;

public class LinkedScrollView extends ScrollView 
{ 
  public boolean cascadeScroll = true; 
  public ArrayList<LinkedScrollView> others = new ArrayList<LinkedScrollView>(); 

  public LinkedScrollView(Context context) 
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
              // notify that we have reached the bottom
              //Toast.makeText(getSherlockActivity(), "fine",Toast.LENGTH_SHORT).show();
      }

    if(cascadeScroll) 
    { 
      for(int i = 0; i < others.size(); i++) 
      { 
        others.get(i).cascadeScroll = false; 
        others.get(i).scrollTo(l, t); 
        others.get(i).cascadeScroll = true; 
      } 
    }
    super.onScrollChanged(l, t, oldl, oldt); 

  }
}
