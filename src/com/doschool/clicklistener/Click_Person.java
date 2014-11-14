package com.doschool.clicklistener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.doschool.R;
import com.doschool.aa.activity.Act_PersonPage;
import com.doschool.aa.im.activity.Act_SingleChat;
import com.doschool.app.DoschoolApp;
import com.doschool.app.MySession;
import com.doschool.entity.SimplePerson;
import com.umeng.analytics.MobclickAgent;

/**
 * 点击事件：点用户，跳转到用户主页
 * 功能完成：100%
 * 异常处理：100%
 * @author 是我的海
 */

public class Click_Person implements OnClickListener {
	Context ctx;
	SimplePerson personData;

	public Click_Person(Context ctx, SimplePerson person) {
		this.ctx = ctx;
		this.personData = person;
	}

	@Override
	public void onClick(View v) {

		MobclickAgent.onEvent(ctx, "event_click_person");
		Intent intent = new Intent(ctx, Act_PersonPage.class);
		MySession.getSession().put("person", personData);
		if(personData!=null && personData.personId>0)
		{
			ctx.startActivity(intent);
			((Activity) ctx).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
		
//				Intent it = new Intent(ctx,Act_SingleChat.class);				
////				it.putExtra("toID", "1415779960358466");
//
//				it.putExtra("toID", personData.funId);
//				it.putExtra("chatType",1);
//				ctx.startActivity(it);		

	}
}