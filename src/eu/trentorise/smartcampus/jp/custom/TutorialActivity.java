package eu.trentorise.smartcampus.jp.custom;

import android.view.View;

import com.github.espiandev.showcaseview.BaseTutorialActivity;

import eu.trentorise.smartcampus.jp.helper.JPHelper;

public class TutorialActivity extends BaseTutorialActivity {

	@Override
	public void skipTutorial(View v) {
		JPHelper.setWantTour(this, false);
		this.mShowcaseView.hide();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		//maybe we want to navigate back in tutorials?
		skipTutorial(null);
	}
	
	
}
