package com.doschool.aa.item;

import com.doschool.R;
import com.doschool.clicklistener.Click_Blog;
import com.doschool.clicklistener.Click_Topic;
import com.doschool.entity.Microblog;
import com.doschool.entity.Topic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 微博广场ListView的Item布局
 * 
 * @author 是我的海
 */

public class SquareWallTopic_Item extends LinearLayout {

	/******** 界面组件 ****************************************/

	/******** 数据等 ****************************************/
	private Topic topicData;
	TextView tvTopic;
	Button btnClose;

	
	// 初始化界面
	public SquareWallTopic_Item(Context context) {
		super(context);
		createUI();
	}

	public SquareWallTopic_Item(Context context, AttributeSet attrs) {
		super(context, attrs);
		createUI();
	}

	private void createUI() {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.item_square_wall_topic, this);
		tvTopic=(TextView) findViewById(R.id.tvTopic);
		btnClose=(Button) findViewById(R.id.btClose);
		
	}

	// 注入数据
	public void updateUI(Topic data) {
		topicData = data;
		this.setOnClickListener(new Click_Topic(getContext(), topicData));
		tvTopic.setText(topicData.topic+topicData.hot);
		
	}

	public void setOnCloseClickListener(OnClickListener clickListener)
	{
		btnClose.setOnClickListener(clickListener);
	}

}
