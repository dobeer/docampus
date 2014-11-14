package com.doschool.aa.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.doschool.aa.item.Blog_Item;
import com.doschool.aa.item.SquareTopic_Item;
import com.doschool.aa.item.SquareWallTopic_Item;
import com.doschool.app.DoschoolApp;
import com.doschool.entity.Microblog;
import com.doschool.entity.SquareEntity;
import com.doschool.entity.Topic;
import com.doschool.methods.DoMethods;
import com.doschool.methods.SpMethods;



import android.content.Context;
import android.provider.CalendarContract.Instances;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class Adp_SquareLatest extends Adp_Standard {
	private Context context;
	private ArrayList<SquareEntity> sayDataSet;
	
	
	public Adp_SquareLatest(Context context,ArrayList<SquareEntity> sayDataSet){
		this.context = context;
		this.sayDataSet = sayDataSet;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		

		Log.v("position="+position, "yyy");
		
		if(sayDataSet.get(position).type==SquareEntity.TYPE_BLOG)
		{
			if (convertView == null || !(convertView instanceof Blog_Item)) {
		    	Blog_Item A =new Blog_Item(context);
		    	convertView =A;
		     }
			((Blog_Item) convertView).updateUI(sayDataSet.get(position).blog);
		}
		else if(sayDataSet.get(position).type==SquareEntity.TYPE_TOPIC_RECOMMEND)
		{
			if (convertView == null || !(convertView instanceof SquareTopic_Item)) {
		    	SquareTopic_Item A =new SquareTopic_Item(context);
		    	convertView =A;
		     }
			((SquareTopic_Item) convertView).updateUI(sayDataSet.get(position).topic);
		}
		else if(sayDataSet.get(position).type==SquareEntity.TYPE_TOPIC_BLOGWALL)
		{
			if (convertView == null || !(convertView instanceof SquareWallTopic_Item)) {
				SquareWallTopic_Item A =new SquareWallTopic_Item(context);
		    	convertView =A;
		     }
			((SquareWallTopic_Item) convertView).updateUI(sayDataSet.get(position).topic);
			((SquareWallTopic_Item) convertView).setOnCloseClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Adp_SquareLatest.this.notifyDataSetChanged();
					Topic topic=sayDataSet.get(position).topic;
					DoschoolApp.mDBHelper.insertTopic(topic.tid, topic.topic, true);
					sayDataSet.remove(position);
					DoMethods.showToast(context, "小安把这个话题移到“发现”->“微博墙”里面了^_^");
				}
			});
		}
		
		
		
	     if(position==0)
	    	 convertView.setPadding(0, DoschoolApp.pxperdp*40, 0, 0);
	     else
	    	 convertView.setPadding(0, DoschoolApp.pxperdp*0, 0, 0);
	     return convertView;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return sayDataSet.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return sayDataSet.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
}
