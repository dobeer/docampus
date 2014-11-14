package com.doschool.aa.activity.Oneblog;

import com.doschool.R;
import com.doschool.aa.activity.Act_EditInfo;
import com.doschool.aa.activity.Act_OneBlog;
import com.doschool.aa.activity.Act_Write;
import com.doschool.aa.widget.MyDialog;
import com.doschool.aa.widget.WidgetFactory;
import com.doschool.app.DoschoolApp;
import com.doschool.component.atemotion.BlogContentTextWatcher;
import com.doschool.component.atemotion.BlogEditText;
import com.doschool.entity.Microblog;
import com.doschool.methods.DoMethods;
import com.doschool.methods.SpMethods;
import com.doschool.network.DoBlogSever;
import com.doschool.zother.MJSONObject;


import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Oneblog_LaunchCmt extends LinearLayout {


	/******** 尺寸控制 ****************************************/
	private int parentWidth =DoschoolApp.widthPixels;
	private int mDip=DoschoolApp.pxperdp;
	private static final int ID_EMOTION = 1;
	private static final int ID_AT = 2;
	private static final int ID_SEND = 3;
	private static final int ID_PARENT=4;
	
	/******** 界面组件  ****************************************/
	private ImageButton btEmotion,btAt;
	private BlogEditText metText;
	private TextView mtvSend;
	

	/******** 数据  ****************************************/
	private Act_OneBlog containerAct;
	private int objPersonId;
	private Microblog blogData;
	
	public void resetCmtBoxOnCmtItemClicked()
	{
		objPersonId=containerAct.cmtObjId;
		metText.setText("");
		if(containerAct.cmtObjId==blogData.author.personId)
			metText.setHint("回复楼主");
		else
			metText.setHint("回复"+containerAct.cmtObjNick);
	}
	
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch ((Integer)v.getTag()) {
			case ID_SEND:
				InputMethodManager imm3 =(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
		        imm3.hideSoftInputFromWindow(metText.getWindowToken(),0); 
		        metText.emotionLayout.setVisibility(View.GONE);
		        metText.atListLayout.setVisibility(View.GONE);
				
		        
		        long timeCha = System.currentTimeMillis() - SpMethods.loadLong(getContext(), SpMethods.LAST_COMMENT_TIME, 0);
				String content=metText.getText().toString();
				boolean isEmpty=true;
				for(int i=0;i<content.length();i++)
				{
					if(content.charAt(i)!='\n' && content.charAt(i)!=' ')
					{
						isEmpty=false;
						break;
					}
				}
				if(DoschoolApp.isGuest())
				{

					MyDialog.popURGuest(getContext());
				}
				
				else if (timeCha < 10000)
					DoMethods.showToast(getContext(), "3秒钟内不能连续评论哦(⊙▽⊙)");
				else if(content.length()>DoschoolApp.CMT_CONTENT_CHARACTER_NUM)
				{
					DoMethods.showToast(getContext(), "评论字数不能超过1000字哦");
				}
				else if(isEmpty)
				{
					DoMethods.showToast(getContext(), "评论内容不能为空哦");
				}
				else
				{
					new MakeCommentTask().execute();
				}
				break;
			}
			
		}
	};
	
	public Oneblog_LaunchCmt(Context context,Microblog microblog) {
		super(context);
		//1  初始化数据
		this.containerAct=(Act_OneBlog) context;
		this.blogData=microblog;
		this.objPersonId=microblog.author.personId;
		
		LayoutInflater.from(getContext()).inflate(R.layout.comment_layout, this);
		
		btEmotion=(ImageButton) findViewById(R.id.btEmotion);
		btEmotion.setTag(Integer.valueOf(ID_EMOTION));
		
		btAt=(ImageButton) findViewById(R.id.btAt);
		btAt.setTag(Integer.valueOf(ID_AT));
		
		//3  评论框
		metText =(BlogEditText) findViewById(R.id.metText);
		metText.init(btEmotion, btAt, new LinearLayout(getContext()));
		
		//4  评论按钮
		mtvSend=(Button) findViewById(R.id.mtvSend);
		mtvSend.setTag(Integer.valueOf(ID_SEND));
		mtvSend.setOnClickListener(onClickListener);
		
		RelativeLayout downLayout=(RelativeLayout) findViewById(R.id.downLayout);
		
		downLayout.addView(metText.emotionLayout, LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		downLayout.addView(metText.atListLayout, LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		metText.addTextChangedListener(
				new BlogContentTextWatcher(null, new TextView(getContext()), metText,DoschoolApp.CMT_CONTENT_CHARACTER_NUM));
	}
	
	/******** 评论任务  ****************************************/
	public class MakeCommentTask extends AsyncTask<Void, Void, Void> {

		MJSONObject jResult;
		
		protected void onPreExecute() {
			metText.setEnabled(false);
			mtvSend.setEnabled(false);
//			containerAct.callRefreshOnCommenting();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			jResult = DoBlogSever.MicroblogCommentSend(DoschoolApp.thisUser.personId, objPersonId, blogData.blogId, 0, metText.toJMjsonObject());
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			
			
			
			int code = jResult.getInt("code", 9);
			String toast = jResult.getString("data");
			if (code == 0) {
				metText.setText("");
				metText.setEnabled(true);
				mtvSend.setText("评论");
				mtvSend.setEnabled(true);
				DoMethods.showToast(getContext(), "评论成功");
				SpMethods.saveLong(getContext(), SpMethods.LAST_COMMENT_TIME, System.currentTimeMillis());
				containerAct.callRefreshAfterCommentSucceed();
			} else {
				
				metText.setEnabled(true);
				mtvSend.setText("评论");
				mtvSend.setEnabled(true);
				if (toast.length() == 0) {
					if (code == 1)
						toast = "无此用户";
					else if (code == 2)
						toast = "错误的目标用户";
					else if (code == 3)
						toast = "无此微博";
					else if (code == 3)
						toast = "不和谐的评论内容";
					else if (code == 9)
						toast = "网络服务错误";
				}
				DoMethods.showToast(getContext(), toast);
			}
			
			
			
			
		}
	}

	
}
