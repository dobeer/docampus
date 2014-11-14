﻿package com.doschool.aa.activity;

import java.util.ArrayList;

import com.doschool.R;
import com.doschool.aa.aa.Fgm_TabPager;
import com.doschool.aa.adapter.Adp_SquareLatest;
import com.doschool.aa.widget.TabPagerSpec;
import com.doschool.app.DoschoolApp;
import com.doschool.asynctask.RefreshSquareListTask;
import com.doschool.component.push2refresh.PullToRefreshBase;
import com.doschool.component.push2refresh.PullToRefreshListView;
import com.doschool.component.push2refresh.PullToRefreshBase.OnRefreshListener;
import com.doschool.methods.ConvertMethods;
import com.doschool.methods.SpMethods;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.doschool.entity.SquareEntity;

import android.widget.ListView;

public class Fgm_BlogSquare extends Fgm_TabPager {

	/******** 微博列表三金刚 **********************************/
	private ArrayList<PullToRefreshListView> mplvList = new ArrayList<PullToRefreshListView>();
	private ArrayList<ArrayList<SquareEntity>> dataList = new ArrayList<ArrayList<SquareEntity>>();
	private ArrayList<Adp_SquareLatest> adapterList = new ArrayList<Adp_SquareLatest>();

	
	@Override
	public void initData() {
		indicateList = new String[] { "最新", "最热","好友" };
		isHideWhenBrowse = true;
	}

	@Override
	public void addViewToFgm() {
		super.addViewToFgm();
		getActionBar().setTittle("口袋小安");
		getActionBar().addOperateButton(R.drawable.sicon_msg_cmt, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), Act_BlogMsg.class);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});

	}

	
	@Override
	public void onPageChanged(int arg0) {
		if (dataList.get(arg0).size() == 0)
			doRefresh(false);
//		else
//			for (int i = 0; i < dataList.get(currentPageIndex).size(); i++)
//				if (dataList.get(currentPageIndex).get(i).blogId == -1)
//					dataList.get(currentPageIndex).remove(i);
//		adapterList.get(currentPageIndex).notifyDataSetChanged();
	}

	public void doRefresh(final boolean ForceFromNet) {
//		// 如果翻到的这页，数据集大小为0
//		if (dataList.get(currentPageIndex).size() == 0) {
//			// 那么想都不要想，先去本地读缓存数据
//			try {
//				MJSONArray jarrayData = new MJSONArray(SpMethods.loadString(getActivity(), SpMethods.BLOG_STR_LIST + pageIndex2contentType(currentPageIndex)));
//				for (int i = 0; i < jarrayData.length(); i++)
//					dataList.get(currentPageIndex).add(new Microblog(jarrayData.getMJSONObject(i)));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			adapterList.get(currentPageIndex).notifyDataSetChanged();
//		}
		// 如果强制联网读，或者读完本地数据还是0，那么必然要联网了
		if (ForceFromNet || dataList.get(currentPageIndex).size() == 0)
			mplvList.get(currentPageIndex).doPullRefreshing(true, 0);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
//		doRefresh(true);
		super.onActivityCreated(savedInstanceState);
	}

	public int pageIndex2contentType(int page) {
		switch (page) {
		case 0:
			return 1;
		case 1:
			return 2;
		case 2:
			return 3;
		}
		return 1;
	}

	public void onResume() {
		super.onResume();
//		for (int i = 0; i < dataList.get(currentPageIndex).size(); i++)
//			if (dataList.get(currentPageIndex).get(i).blogId == -1)
//				dataList.get(currentPageIndex).remove(i);
//		adapterList.get(currentPageIndex).notifyDataSetChanged();
		

	}



	@Override
	public TabPagerSpec[] giveTabPagerSpec() {

		for (int tempI = 0; tempI < indicateList.length; tempI++) {

			PullToRefreshListView ptrlv = new PullToRefreshListView(getActivity());
			ptrlv.getRefreshableView().setDivider(null);
			ptrlv.getRefreshableView().setCacheColorHint(getResources().getColor(R.color.bg_yellow));
			mplvList.add(ptrlv);
			long lastRefreshTime = SpMethods.loadLong(getActivity(), SpMethods.BLOG_TIME_LIST + pageIndex2contentType(tempI), 0);
			ptrlv.setLastUpdatedLabel(ConvertMethods.dateLongToDiyStr(lastRefreshTime));

			// 设置mplv工作模式
			if(tempI==0 || tempI==2)
			{
				ptrlv.setPullLoadEnabled(false);
				ptrlv.setScrollLoadEnabled(true);
			}
			else
			{
				ptrlv.setPullLoadEnabled(false);
				ptrlv.setScrollLoadEnabled(false);
			}

			// 设置mplv的适配器
			dataList.add(new ArrayList<SquareEntity>());
			adapterList.add(new Adp_SquareLatest(getActivity(), dataList.get(tempI)));
			ptrlv.getRefreshableView().setAdapter(adapterList.get(tempI));

			// 设置mplv滑动监听
			ptrlv.setOnScrollListener(new onListViewScrollListener(DoschoolApp.newImageLoader, true, true));
			mplvList.get(tempI).setOnRefreshListener(new OnRefreshListener<ListView>() {
				@Override
				public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
					int contentType = pageIndex2contentType(currentPageIndex);
					new RefreshSquareListTask(
							mplvList.get(currentPageIndex), 
							adapterList.get(currentPageIndex),
							dataList.get(currentPageIndex), 
							contentType, 
							0, 
							"", 
							false)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);	
				}

				@Override
				public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
					int contentType = pageIndex2contentType(currentPageIndex);
					new RefreshSquareListTask(
							mplvList.get(currentPageIndex), 
							adapterList.get(currentPageIndex),
							dataList.get(currentPageIndex), 
							contentType, 
							0, 
							"", 
							true)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);	
				}
			});
		}
		// 准备标签
		TabPagerSpec[] specList = new TabPagerSpec[indicateList.length];
		for (int tempI = 0; tempI < indicateList.length; tempI++) {
			TabPagerSpec spec = new TabPagerSpec(mplvList.get(tempI), indicateList[tempI]);
			specList[tempI] = spec;
		}
		return specList;
	}

}
