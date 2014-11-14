package com.doschool.aa.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.doschool.R;
import com.doschool.aa.widget.MyDialog;
import com.doschool.app.ActivityName;
import com.doschool.app.DoschoolApp;
import com.doschool.component.updatelater.IPostLater;
import com.doschool.component.updatelater.PostLaterReceiver;
import com.doschool.component.updatelater.PostLaterService;
import com.doschool.component.updatelater.TaskLayout;
import com.doschool.entity.JSONHelper;
import com.doschool.entity.Person;
import com.doschool.entity.SimplePerson;
import com.doschool.entity.User;
import com.doschool.methods.DoMethods;
import com.doschool.methods.SpMethods;
import com.doschool.network.DoUserSever;
import com.doschool.zother.MJSONArray;
import com.doschool.zother.MJSONObject;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 主页框架 完成度:99%
 * 
 * @author 是我的海
 * 
 */
public class zAct_Main extends FragmentActivity implements IPostLater {

	private final static String TAG = "zAct_Main";
	private final static boolean DEBUG = true;
	private final int mScrnHeight = DoschoolApp.heightPixels;
	private final int mScrnWidth = DoschoolApp.widthPixels;
	private final int mDip = DoschoolApp.pxperdp;
	
	
	protected int currentIndex = 0;
	
	private MyTab[] mTabList;
	Button btSendText, btSendGallery, btSendCamera;
	
	protected Fragment[] fgmList;
	protected Fgm_BlogSquare fgmSquare;
	protected Fgm_Communication fgmCommuication;
	protected Fgm_Discover fgmDiscover;
	protected Fgm_Mine fgmMine;
	
	private TaskLayout taskLayout;
	public PopupWindow postBlogPopWindow;
	

	private PostLaterReceiver postLaterReceiver;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 软件升级
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(getApplicationContext());

		// 创建界面
		createUI();

	}

	private void createUI() {

		setContentView(R.layout.act_main);

		// 1.创建所有Fragment
		fgmSquare = new Fgm_BlogSquare();
		fgmCommuication = new Fgm_Communication();
		fgmDiscover = new Fgm_Discover();
		fgmMine = new Fgm_Mine();
		fgmList = new Fragment[] { fgmSquare, fgmCommuication, fgmDiscover, fgmMine };
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fgmList[0]).add(R.id.fragment_container, fgmList[1]).add(R.id.fragment_container, fgmList[2])
				.add(R.id.fragment_container, fgmList[3]).show(fgmList[0]).hide(fgmList[1]).hide(fgmList[2]).hide(fgmList[3]).commit();

		// 2.创建所有Tab
		mTabList = new MyTab[4];
		mTabList[0] = new MyTab((RelativeLayout) findViewById(R.id.tabSquare));
		mTabList[1] = new MyTab((RelativeLayout) findViewById(R.id.tabCommunication));
		mTabList[2] = new MyTab((RelativeLayout) findViewById(R.id.tabDiscover));
		mTabList[3] = new MyTab((RelativeLayout) findViewById(R.id.tabMine));
		for (int i = 0; i < mTabList.length; i++) {
			mTabList[i].tabLayout.setTag(i);
		}

		// 3.创建稍后上传条
		RelativeLayout taskParent = (RelativeLayout) findViewById(R.id.taskParent);
		taskLayout=new TaskLayout(this);
		taskParent.addView(taskLayout, mScrnWidth, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		// 4.创建发表Popupwindow
		View postBlogPopView = LayoutInflater.from(this).inflate(R.layout.add_popwindow, null);
		btSendText = (Button) postBlogPopView.findViewById(R.id.btSendText);
		btSendGallery = (Button) postBlogPopView.findViewById(R.id.btSendGallery);
		btSendCamera = (Button) postBlogPopView.findViewById(R.id.btSendCamera);
		postBlogPopWindow = new PopupWindow(postBlogPopView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, false);
		postBlogPopWindow.setBackgroundDrawable(new BitmapDrawable());

	}

	/**
	 * Tab按钮点击事件
	 */
	public void onTabClicked(View view) {
		changeTab((Integer) view.getTag());
	}

	/**
	 * 发表popupwindow中Item的点击事件
	 */
	public void onPopupItemClick(View v) {
		Intent intent = new Intent(this, Act_Write.class);
		switch (v.getId()) {
		case R.id.postBlogPopView:
			MobclickAgent.onEvent(this, "event_actionbar_writeblog");
			postBlogPopWindow.dismiss();
			break;
		case R.id.btSendText:
			MobclickAgent.onEvent(this, "event_click_post_text");
			intent.putExtra("startType", Act_Write.START_TYPE_TEXT);
			startActivityForResult(intent, ActivityName.CODE_ACT_WRITEBLOG);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		case R.id.btSendGallery:
			MobclickAgent.onEvent(this, "event_click_post_gallery");
			intent.putExtra("startType", Act_Write.START_TYPE_GALLERY);
			startActivityForResult(intent, ActivityName.CODE_ACT_WRITEBLOG);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		case R.id.btSendCamera:
			MobclickAgent.onEvent(this, "event_click_post_cameta");
			intent.putExtra("startType", Act_Write.START_TYPE_CAMERA);
			startActivityForResult(intent, ActivityName.CODE_ACT_WRITEBLOG);
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		}
	}

	/**
	 * 发表按钮点击事件
	 */
	public void onPostClick(View v) {

		MobclickAgent.onEvent(this, "event_actionbar_writeblog");
		if (DoschoolApp.isGuest()) {
			MyDialog.popURGuest(this);
		} else {
			postBlogPopWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

			TranslateAnimation upin = new TranslateAnimation(0, 0, -mScrnHeight, 0);
			upin.setDuration(500);
			upin.setFillAfter(true);
			upin.setInterpolator(new OvershootInterpolator(1.1f));
			btSendText.startAnimation(upin);

			TranslateAnimation upin2 = new TranslateAnimation(0, 0, -mScrnHeight, 0);
			upin2.setDuration(700);
			upin2.setFillAfter(true);
			upin2.setInterpolator(new OvershootInterpolator(1.1f));
			btSendGallery.startAnimation(upin2);

			TranslateAnimation upin3 = new TranslateAnimation(0, 0, -mScrnHeight, 0);
			upin3.setDuration(900);
			upin3.setFillAfter(true);
			upin3.setInterpolator(new OvershootInterpolator(1.1f));
			btSendCamera.startAnimation(upin3);

		}
	}

	/**
	 * 选择切换某个Tab
	 */
	public void changeTab(int which) {
		// if (currentIndex != which) {
		FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
		trx.hide(fgmList[currentIndex]);
		if (!fgmList[which].isAdded()) {
			trx.add(R.id.fragment_container, fgmList[which]);
		}
		trx.show(fgmList[which]).commit();
		// }
		mTabList[currentIndex].btIcon.setSelected(false);
		mTabList[currentIndex].indicator.setSelected(false);

		// 把当前tab设为选中状态
		mTabList[which].btIcon.setSelected(true);
		mTabList[which].indicator.setSelected(true);
		currentIndex = which;
	}

	/**
	 * 验证用户账号密码有效性
	 */
	public void userIdentify() {
		if (User.loadFunId(getApplicationContext()) == null || User.loadFunId(getApplicationContext()).length() == 0)
			MyDialog.popLogoutDialog(zAct_Main.this);
		else
			new Thread(new Runnable() {
				@Override
				public void run() {
					String funId = User.loadFunId(getApplicationContext());
					String password = User.loadPassword(getApplicationContext());

					MJSONObject result = DoUserSever.UserLogin(funId, password);
					int code = result.getInt("code", 9);
					// 处理各状态
					if (code == 2)
						// 账号密码错误
						User.clearAutoLoginInfo(getApplicationContext());
				}
			}).start();
	}

	/**
	 * 处理返回键事件
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (postBlogPopWindow.isShowing() == true) {
				postBlogPopWindow.dismiss();
			} else {
				moveTaskToBack(false);
			}
			return true;
		}
		return false;
	}

	
	public void onResume() {
		super.onResume();
		// =================== MGG'S PART ===================START
		PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, "SuzbZRlEGULgnh5X1PrOEm8u");
		// =================== MGG'S PART ===================END

		if (postBlogPopWindow.isShowing())
			postBlogPopWindow.dismiss();

		Intent startServiceIntent = new Intent(zAct_Main.this, PostLaterService.class);
		Bundle bundle = new Bundle();
		bundle.putString("operate", "start");
		startServiceIntent.putExtras(bundle);
		startService(startServiceIntent);
		
		changeTab(currentIndex);
		registerUploadLaterReceiver();
		
		// 友盟统计
		MobclickAgent.onResume(this);
		MobclickAgent.onPageStart(getClass().getName());
	}

	public void onPause() {
		super.onPause();
		unregisterUploadLaterReceiver();
		// 友盟统计
		MobclickAgent.onPageEnd(getClass().getName());
		MobclickAgent.onPause(this);
	}

	/**
	 * 注册稍后上传的广播
	 */
	public void registerUploadLaterReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(DoschoolApp.UPLOAD_LATER_ACTION_NAME);
		postLaterReceiver = new PostLaterReceiver(taskLayout, this);
		registerReceiver(postLaterReceiver, myIntentFilter);
	}

	/**
	 * 解除稍后上传的广播
	 */
	public void unregisterUploadLaterReceiver() {
		unregisterReceiver(postLaterReceiver);
	}

	@Override
	public void onPostSucceed() {
	}

	@Override
	public void startUploadSevice() {
		Intent startServiceIntent = new Intent(this, PostLaterService.class);
		Bundle bundle = new Bundle();
		bundle.putString("operate", "start");
		startServiceIntent.putExtras(bundle);
		startService(startServiceIntent);
	}

	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看
			Log.i("IMTEST", "收到msg" + intent.getAction());

			// 消息id
			String msgId = intent.getStringExtra("msgid");
			String fromId = intent.getStringExtra("from");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgId);
			if (message.getType() == EMMessage.Type.TXT) {
				EMChatManager.getInstance().getConversation(fromId).addMessage(message);
				// //TODO 如果要改逻辑（看以前的聊天记录的时候是不是设计一个界面小气球，点了才到最下面）
				// String msg =
				// ((TextMessageBody)message.getBody()).getMessage();
				// Log.i("IMTEST","msg:"+msg+"  from:"+message.getFrom()+" to:"+message.getTo());
			}
			abortBroadcast();
		}
	}
	
	class MyTab {
		RelativeLayout tabLayout;
		ImageView btIcon;
		TextView indicator;
		TextView redot;

		public MyTab(RelativeLayout rela) {
			tabLayout = rela;
			btIcon = (ImageView) rela.getChildAt(0);
			indicator = (TextView) rela.getChildAt(1);
			redot = (TextView) rela.getChildAt(2);
		}
	}

	class Usert{
	    private String name;
	    private String password;
	    public String getName() {
	        return name;
	    }
	    public void setName(String name) {
	        this.name = name;
	    }
	    public String getPassword() {
	        return password;
	    }
	    public void setPassword(String password) {
	        this.password = password;
	    }
	}
}
