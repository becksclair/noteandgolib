package com.heliasar.noteandgolib.ui;

import java.util.List;

import com.heliasar.noteandgolib.R;
import com.heliasar.noteandgolib.data.PhotosAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

public class PageAdapter extends FragmentPagerAdapter implements
		ActionBar.TabListener, ViewPager.OnPageChangeListener {

	private int NUM_ITEMS = 2;
	public static final int NOTES_PAGE = 0;
	public static final int PHOTOS_PAGE = 1;

	private ActionBar ab;
	private FragmentManager fragmentManager;
	private ViewPager pager;
	public boolean dualPane;

	public Fragment notesFragment;
	public Fragment photosFragment;

	public PageAdapter(FragmentManager fm, ActionBar bar, ViewPager viewPager, List<Fragment> fragments) {
		super(fm);

		fragmentManager = fm;
		ab = bar;
		pager = viewPager;

		notesFragment = fragments.get(0);
		photosFragment = fragments.get(1);

		addTabs();

		pager.setAdapter(this);
		pager.setOnPageChangeListener(this);
	}

	@Override
	public Fragment getItem(int position) {
		if (position == NOTES_PAGE){
			return notesFragment;
		}

		else if (position == PHOTOS_PAGE) {
			return photosFragment;
		}
		else
			return null;
	}

	@Override
	public int getCount() {
		return NUM_ITEMS;
	}

	public void onPageScrollStateChanged(int position) {
	}

	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	public void onPageSelected(int position) {
		ab.setSelectedNavigationItem(position);
		// Make sure we have our cache loaded
		if (position == PHOTOS_PAGE) {
			PhotosAdapter a = ((PhotosFragment) photosFragment).getPhotosAdapter();
			if (a != null) {
				if (a.getIsCacheEmpty()) {
					a.refresh();
				}
			}
		}
		
		if (!dualPane) {
			return;
		}
		if (position == PHOTOS_PAGE) {
			ab.setSubtitle("");

			ViewerFragment viewer;

			FragmentTransaction ft = fragmentManager.beginTransaction();
			Fragment f = fragmentManager.findFragmentById(R.id.editFrame);

			if (f != null && (f.getClass() == ViewerFragment.class)) {
				viewer = (ViewerFragment) f;
				ft.replace(R.id.editFrame, viewer);
				
			} else if (f == null) {
				viewer = new ViewerFragment();
				ft.add(R.id.editFrame, viewer);
			}

			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		} else {
			ab.setSubtitle("");
		}
	}

	public void addTabs() {
		ActionBar.Tab notesTab = ab.newTab()
								   .setText(R.string.notes)
								   .setTag(Integer.valueOf(0)).setTabListener(this);

		ActionBar.Tab photosTab = ab.newTab()
				.setText(R.string.photos)
				.setTag(Integer.valueOf(1)).setTabListener(this);

		ab.addTab(notesTab, true);
		ab.addTab(photosTab);
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		pager.setCurrentItem(tab.getPosition());
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
	
	public void setItemsCount(int count) {
		NUM_ITEMS = count;
	}

}
