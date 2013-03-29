package eu.trentorise.smartcampus.jp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.text.DecimalFormat; 
import java.util.ArrayList; 
import android.app.Activity; 
import android.content.Context; 
import android.os.Bundle; 
import android.widget.HorizontalScrollView; 
import android.widget.LinearLayout; 
import android.widget.ScrollView; 
import android.widget.TableLayout; 
import android.widget.TableRow; 
import android.widget.TextView; 

public class Launcher extends Activity { 
    /** Called when the activity is first created. */ 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.spreadsheet); 

        final int ROW_HEIGHT = 50; 
        final int COL_WIDTH = 80; 
        final int NUM_COLS_AND_ROWS = 15; //keeping it square just because i'm lazy 

        String[] cols = new String[NUM_COLS_AND_ROWS]; 
        String[] rows = new String[NUM_COLS_AND_ROWS]; 
        String[][] data = new String[NUM_COLS_AND_ROWS] 
[NUM_COLS_AND_ROWS]; 
        DecimalFormat twoPlaces = new DecimalFormat("0.00"); 
        for(int i = 0; i < NUM_COLS_AND_ROWS; i++) 
        { 
          cols[i] = "Col" + i; 
          rows[i] = "Row" + i; 
          for(int j = 0; j < NUM_COLS_AND_ROWS; j++) 
          { 
            data[i][j] = twoPlaces.format(Math.random() * 1000); 
          } 
        } 


        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_spreadsheet); 

        //setup left column with row labels 
        LinkedScrollView lsvLeftCol = new LinkedScrollView(this); 
        lsvLeftCol.setVerticalScrollBarEnabled(false); //this one will look wrong 
        TableLayout tlLeftCol = new TableLayout(this); 
        TableLayout.LayoutParams tlLeftColParams = new 
TableLayout.LayoutParams(); 
        tlLeftColParams.width= COL_WIDTH; 
        tlLeftCol.setLayoutParams(tlLeftColParams); 
        for(int i = -1; i < rows.length; i++) 
        { 
          TableRow tr = new TableRow(this); 
          TextView tv = new TextView(this); 
          if(i >= 0) //-1 is the blank top left cell - this should really be outside the scroll to look right 
          { 
            tv.setText(rows[i]); 
          } 
          tr.addView(tv); 
          tr.setMinimumHeight(ROW_HEIGHT); 
          tlLeftCol.addView(tr); 
        } 
        lsvLeftCol.addView(tlLeftCol); 

        //add the main horizontal scroll 
        HorizontalScrollView hsvMainContent = new 
HorizontalScrollView(this); 
        hsvMainContent.setHorizontalScrollBarEnabled(false); //you could probably leave this one enabled if you want 

        LinearLayout llMainContent = new LinearLayout(this); //Scroll view needs a single child 
        llMainContent.setOrientation(LinearLayout.VERTICAL); 

        //add the headings 
        TableLayout tlColHeadings = new TableLayout(this); 
        TableRow trHeading = new TableRow(this); 
        trHeading.setMinimumHeight(ROW_HEIGHT); 
        for(int i = 0; i < cols.length; i++) 
        { 
          TextView tv = new TextView(this); 
          tv.setText(rows[i]); 
          tv.setMinWidth(COL_WIDTH); 
          trHeading.addView(tv); 
        } 

        tlColHeadings.addView(trHeading); 
        llMainContent.addView(tlColHeadings); 

        //now lets add the main content 
        LinkedScrollView lsvMainVertical = new LinkedScrollView(this); 
        lsvMainVertical.setVerticalScrollBarEnabled(false); //this will not be visible most of the time anyway 

        TableLayout tlMainContent = new TableLayout(this); 

        for(int i = 0; i < rows.length; i++) 
        { 
          TableRow tr = new TableRow(this); 
          tr.setMinimumHeight(ROW_HEIGHT); 
          for(int j = 0; j < cols.length; j++) 
          { 
            TextView tv = new TextView(this); 
            tv.setText(data[i][j]); 
            tv.setMinWidth(COL_WIDTH); 
            tr.addView(tv); 
          } 
          tlMainContent.addView(tr); 
        } 

        lsvMainVertical.addView(tlMainContent); 

        llMainContent.addView(lsvMainVertical); 

        hsvMainContent.addView(llMainContent); 

        layout.addView(lsvLeftCol); 
        layout.addView(hsvMainContent); 

        //the magic 
        lsvMainVertical.others.add(lsvLeftCol); 
        lsvLeftCol.others.add(lsvMainVertical); 
    } 

    private class LinkedScrollView extends ScrollView 
    { 
      public boolean cascadeScroll = true; 
      public ArrayList<LinkedScrollView> others = new 
ArrayList<LinkedScrollView>(); 

      public LinkedScrollView(Context context) 
      { 
        super(context); 
      } 

      @Override 
      protected void onScrollChanged(int l, int t, int oldl, int oldt) 
      { 
        super.onScrollChanged(l, t, oldl, oldt); 

        if(cascadeScroll) 
        { 
          for(int i = 0; i < others.size(); i++) 
          { 
            others.get(i).cascadeScroll = false; 
            others.get(i).scrollTo(l, t); 
            others.get(i).cascadeScroll = true; 
          } 
        } 
      } 
    } 
} 
