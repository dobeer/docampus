package com.doschool.aa.activity;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.doschool.R;
import com.doschool.app.DoschoolApp;
import com.doschool.asynctask.RefreshMsgListTask;
import com.doschool.asynctask.RefreshRequestListTask;
import com.doschool.asynctask.RefreshScipListTask;
import com.doschool.methods.ConvertMethods;
import com.doschool.methods.SpMethods;
import com.doschool.zother.MJSONArray;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.doschool.aa.aa.Fgm_Standard_Linear;
import com.doschool.aa.aa.Fgm_Standard_Relative;
import com.doschool.aa.aa.Fgm_TabPager;
import com.doschool.aa.adapter.Adp_Messages;
import com.doschool.aa.adapter.Adp_Request;
import com.doschool.aa.adapter.Adp_Scrip;
import com.doschool.aa.widget.TabPagerSpec;
import com.doschool.component.push2refresh.PullToRefreshBase;
import com.doschool.component.push2refresh.PullToRefreshListView;
import com.doschool.component.push2refresh.PullToRefreshBase.OnRefreshListener;
import com.doschool.entity.FriendRequest;
import com.doschool.entity.Message;
import com.doschool.entity.Scrip;

import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;

public class Fgm_Discover extends Fgm_Standard_Linear {

	RelativeLayout YiQi,NightTalk,BlogWall,ToolLayout;

	@Override
	public void initData() {
	}

	@Override
	public void addViewToFgm() {
		getActionBar().setTittle("口袋小安");
		ScrollView scrollView=new ScrollView(getActivity());
		LayoutInflater.from(getActivity()).inflate(R.layout.fgm_discover, scrollView);
		mParent.addView(scrollView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		YiQi=(RelativeLayout) scrollView.findViewById(R.id.YiQi);
		NightTalk=(RelativeLayout) scrollView.findViewById(R.id.NightTalk);
		BlogWall=(RelativeLayout) scrollView.findViewById(R.id.BlogWall);
		ToolLayout=(RelativeLayout) scrollView.findViewById(R.id.ToolLayout);
	}
	
}
