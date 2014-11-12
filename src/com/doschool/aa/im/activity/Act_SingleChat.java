package com.doschool.aa.im.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.doschool.R;
import com.doschool.aa.widget.ProgressImageView;
import com.doschool.app.DoschoolApp;
import com.doschool.component.choosephoto.Act_PhotoChoose;
import com.doschool.methods.SpMethods;
import com.easemob.EMCallBack;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.GroupReomveListener;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;


/**
 * 先实现个最简单的文字发送？
 * @author Ma Ganxuan
 *
 */
public class Act_SingleChat extends Activity{
	public static final int CHATTYPE_SINGLE = 1;
	public static final int CHATTYPE_GROUP = 2;
	private static final int CHOOSE_PHOTO = 10;
	String toID = "";
	int chatType = 0;
	ListView msgList;
	EditText editText;
	Button sendTextButton;
	Button sendImageButton;
	
	ChatListAdapter adapter;
	
	Handler handler;
	
	private ArrayList<String> pathList = new ArrayList<String>();

	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
//			String msgst = ((TextMessageBody)message.getBody()).getMessage();
//			Log.i("IMTEST ACK","msg:"+msgst+"  from:"+message.getFrom()+" to:"+message.getTo());
		
			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if (msg != null) {
					msg.isAcked = true;
				}
			}
			abortBroadcast();
			adapter.notifyDataSetChanged();
		}
	};
	
	/**
	 * 消息送达BroadcastReceiver
	 */
	private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");
			EMConversation conversation = EMChatManager.getInstance().getConversation(from);
			EMMessage message = EMChatManager.getInstance().getMessage(msgid);
//			String msgst = ((TextMessageBody)message.getBody()).getMessage();
//			Log.i("IMTEST 送达","msg:"+msgst+"  from:"+message.getFrom()+" to:"+message.getTo());

			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);
				if (msg != null) {
					msg.isDelivered = true;
				}
			}
			abortBroadcast();
			adapter.notifyDataSetChanged();
		}
	};

	
	
	NewMessageBroadcastReceiver msgReceiver;
	EMConversation conversations;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_im_chat);
		msgList = (ListView)findViewById(R.id.act_im_chatlist);
		editText = (EditText)findViewById(R.id.act_im_chat_editview);
		sendTextButton = (Button)findViewById(R.id.act_im_chat_sendtext);
		sendImageButton = (Button)findViewById(R.id.act_im_chat_sendimage);
				
		toID = getIntent().getStringExtra("toID").toLowerCase();
		chatType = getIntent().getIntExtra("chatType",1);
		handler = new Handler();

		
		//线程启动后登陆成功即更新界面
		new Thread(){
			public void run(){
				try {
					Log.i("IMTEST",SpMethods.loadString(Act_SingleChat.this, SpMethods.USER_FUNID).toLowerCase());
					Log.i("IMTEST",SpMethods.loadString(Act_SingleChat.this, SpMethods.USER_PASSWORD));
					EMChatManager.getInstance().
						createAccountOnServer(""+SpMethods.loadString(Act_SingleChat.this, SpMethods.USER_FUNID).toLowerCase(),
								""+SpMethods.loadString(Act_SingleChat.this, SpMethods.USER_PASSWORD));
				}catch (Exception e) {
					Log.i("IMTEST","注册失败");

					e.printStackTrace();
				}finally{
					EMChatManager.getInstance().login(""+SpMethods.loadString(Act_SingleChat.this, SpMethods.USER_FUNID).toLowerCase(), 
							""+SpMethods.loadString(Act_SingleChat.this,SpMethods.USER_PASSWORD ),new OnLogFinished());
				}
			}
		}.start();
	}
	
	void onLogginFinish(){
		//监听器s
		EMChatManager.getInstance().addConnectionListener(new IMConnectionListener());
		if(chatType == CHATTYPE_GROUP){
			EMGroupManager.getInstance().addGroupChangeListener(new GroupListener());
		}
		//注册消息接受的广播接收器
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager
				.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		registerReceiver(msgReceiver, intentFilter);

		// 注册一个ack回执消息的BroadcastReceiver
		IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getAckMessageBroadcastAction());
		ackMessageIntentFilter.setPriority(5);
		registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

		// 注册一个消息送达的BroadcastReceiver
		IntentFilter deliveryAckMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getDeliveryAckMessageBroadcastAction());
		deliveryAckMessageIntentFilter.setPriority(5);
		registerReceiver(deliveryAckMessageReceiver, deliveryAckMessageIntentFilter);

		
		//TODO 获取之前的消息对话  取50条
		conversations= EMChatManager.getInstance().getConversation(toID,chatType == CHATTYPE_GROUP ?true:false);
//		if(conversations.getLastMessage()!=null){
//			if (chatType == CHATTYPE_SINGLE)
//				messages = conversations.loadMoreMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
//			else
//				messages = conversations.loadMoreGroupMsgFromDB(adapter.getItem(0).getMsgId(), pagesize);
//		}
		
		Log.i("IMTEST",""+conversations.getMsgCount());
		
		//按钮添加监听器
		sendTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendText(editText.getText().toString());
			}
		});
		
		sendImageButton.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it=new Intent(Act_SingleChat.this, Act_PhotoChoose.class);
				it.putExtra("maxCount", DoschoolApp.BLOG_PIC_MAX_COUNT);
				it.putExtra("autoFinish", false);
				Bundle b=new Bundle();
				b.putStringArrayList("pathList", pathList);
				it.putExtra("bundle", b);
				startActivityForResult(it,CHOOSE_PHOTO);
			}
		});
		
		adapter = new ChatListAdapter();
		msgList.setAdapter(adapter);
		EMChat.getInstance().setAppInited();
	}
	
	private void sendText(String string) {
		
		
		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
		if(chatType == CHATTYPE_GROUP)
			msg.setChatType(EMMessage.ChatType.GroupChat);

		//设置消息的接收方
		//设置消息内容。本消息类型为文本消息。
		TextMessageBody body = new TextMessageBody(string);
		msg.addBody(body);
		msg.setReceipt(toID);
		Log.i("IMTEST","toID: "+toID+ "  String:"+string);

		conversations.addMessage(msg);
		adapter.notifyDataSetChanged();
		try {
			if(chatType == CHATTYPE_SINGLE)

			EMChatManager.getInstance().sendMessage(msg);
			else if(chatType == CHATTYPE_GROUP){

		   //发送消息
		   EMChatManager.getInstance().sendGroupMessage(msg,new EMCallBack() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplication(), "成功222", 1).show();
				Log.i("IMTEST","  ssss:");

			}
			
			@Override
			public void onProgress(int arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.i("IMTEST","  pro:"+arg0+"  "+arg1);
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.i("IMTEST","err  pro:"+arg0+"  "+arg1);

				Toast.makeText(getApplication(), "失败222", 1).show();
			}
		});
			}
		} catch (Exception e) {
		   e.printStackTrace();
		}

	}

	private String getContentUri(String picPath){
        Uri mUri = Uri.parse("content://media/external/images/media"); 
        Uri mImageUri = null;
        picPath = picPath.replace("file://", "");
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            String data = cursor.getString(cursor
                    .getColumnIndex(MediaStore.MediaColumns.DATA));
//            Log.i("IMTEST","pic   "+picPath+"  " +data);

            if (picPath.equals(data)) {
                int ringtoneID = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                mImageUri = Uri.withAppendedPath(mUri, ""
                        + ringtoneID);
                break;
            }
            cursor.moveToNext();
        }
        
        Log.i("IMTEST","URi   "+mImageUri.toString());
        return mImageUri.getPath();
	}
	
	private void sendPicByUri(String selectedImage) {
		// String[] filePathColumn = { MediaStore.Images.Media.DATA };
//		selectedImage = Uri.parse("content://media/external/images/media/262279");
		Cursor cursor = getContentResolver().query(Uri.parse(selectedImage), null, null, null, null);
		if (cursor != null) {
//		if(false){
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex("_data");
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			cursor = null;

			if (picturePath == null || picturePath.equals("null")) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			sendPicture(picturePath);
		} else {
			File file = new File(Scheme.FILE.crop(selectedImage));
			Log.i("IMTEST","file.getAbsolutePath  "+file.getAbsolutePath()+" selectedImage.getPath()   "+selectedImage);

			if (!file.exists()) {
				Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;

			}

			sendPicture(file.getAbsolutePath());
		}

	}

	
	/**
	 * 发送图片
	 * 
	 * @param filePath
	 */
	private void sendPicture(final String filePath) {
		String to = toID;
		// create and add image message in view
		final EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
		// 如果是群聊，设置chattype,默认是单聊
		if (chatType == CHATTYPE_GROUP)
			message.setChatType(ChatType.GroupChat);
		message.setReceipt(to);
		ImageMessageBody body = new ImageMessageBody(new File(filePath));
		// 默认超过100k的图片会压缩后发给对方，可以设置成发送原图
//		body.
//		 body.setSendOriginalImage(true);
		message.addBody(body);
		conversations.addMessage(message);
		try {
			   //发送消息
			   EMChatManager.getInstance().sendMessage(message);
			} catch (Exception e) {
			   e.printStackTrace();
			}

//		listView.setAdapter(adapter);
//		adapter.refresh();
//		listView.setSelection(listView.getCount() - 1);
		setResult(RESULT_OK);
// more(more);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CHOOSE_PHOTO:
				pathList=data.getBundleExtra("bundle").getStringArrayList("pathList");
				for(String path : pathList)
					sendPicByUri(path);
				break;
			default:break;
			}
		}
		
	}

	@Override
	protected void onDestroy() {
		// 注销广播
		try {
			unregisterReceiver(msgReceiver);
			msgReceiver = null;
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(ackMessageReceiver);
			ackMessageReceiver = null;
			unregisterReceiver(deliveryAckMessageReceiver);
			deliveryAckMessageReceiver = null;
		} catch (Exception e) {
		
		}

		super.onDestroy();
	}

	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看
			Log.i("IMTEST","收到msg");

			// 消息id
			String msgId = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgId);
				conversations.addMessage(message);
				conversations.resetUnsetMsgCount();
				adapter.notifyDataSetChanged();
				msgList.setSelection(msgList.getCount()-1);
				//TODO 如果要改逻辑（看以前的聊天记录的时候是不是设计一个界面小气球，点了才到最下面）
			if (message.getType() == EMMessage.Type.TXT){
				String msg = ((TextMessageBody)message.getBody()).getMessage();
				Log.i("IMTEST","msg:"+msg+"  from:"+message.getFrom()+" to:"+message.getTo());
//				t.append("msg:"+msg+"  from:"+message.getFrom()+" to:"+message.getTo()+"\r\n");
			}

			abortBroadcast();
		}
	}

	private class OnLogFinished implements EMCallBack{
		@Override
		public void onError(int arg0, String arg1) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(Act_SingleChat.this, "登陆聊天界面失败", 1).show();
					Act_SingleChat.this.finish();
				}
			});
		}

		@Override
		public void onProgress(int arg0, String arg1) {
		}

		@Override
		public void onSuccess() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					onLogginFinish();
				}
			});
		}
		
	}
	
	class GroupListener extends GroupReomveListener {

		@Override
		public void onUserRemoved(final String groupId, String groupName) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (toID.equals(groupId)) {
						Toast.makeText(Act_SingleChat.this, "你被群创建者从此群中移除", 1).show();
						finish();
					}
				}
			});
		}

		@Override
		public void onGroupDestroy(final String groupId, String groupName) {
			// 群组解散正好在此页面，提示群组被解散，并finish此页面
			runOnUiThread(new Runnable() {
				public void run() {
					if (toID.equals(groupId)) {
						Toast.makeText(Act_SingleChat.this, "当前群聊已被群创建者解散", 1).show();
						finish();
					}
				}
			});
		}

	}
	

	private class IMConnectionListener implements ConnectionListener{

		@Override
		public void onConnected() {
			// TODO Auto-generated method stub
			Log.i("IMTEST","con: onConnected");
		}

		@Override
		public void onConnecting(String arg0) {
			// TODO Auto-generated method stub
			Log.i("IMTEST","con: onConnecting");

		}

		@Override
		public void onDisConnected(String arg0) {
			// TODO Auto-generated method stub
			Log.i("IMTEST","con: onDisConnected");

		}

		@Override
		public void onReConnected() {
			// TODO Auto-generated method stub
			Log.i("IMTEST","con: onReConnected");

		}

		@Override
		public void onReConnecting() {
			// TODO Auto-generated method stub
			Log.i("IMTEST","con: onReConnecting");

		}
		
	}
	
	private class ChatListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return conversations.getMsgCount()>50?50:conversations.getMsgCount();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int lastPosition = conversations.getMessagePosition(conversations.getLastMessage());
			int msgPosition = lastPosition - getCount() + position + 1;
			EMMessage msg = conversations.getMessage(msgPosition);
			try {
				EMChatManager.getInstance().ackMessageRead(toID, msg.getMsgId());
			} catch (EaseMobException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TextView t = new TextView(Act_SingleChat.this);
			if(msg.getType()==EMMessage.Type.TXT)
				t.setText(msg.getFrom()+": "+((TextMessageBody)msg.getBody()).getMessage());
			else if(msg.getType()==EMMessage.Type.IMAGE){

				final ProgressImageView ivImage = new ProgressImageView(
						Act_SingleChat.this, 200);
				String imgPath = "";
				if(((ImageMessageBody)msg.getBody()).getLocalUrl() != null){
					if (msg.direct == EMMessage.Direct.SEND){
						imgPath = ((ImageMessageBody)msg.getBody()).getLocalUrl();
						Log.i("IMTEST","getLocalUrl   "+imgPath+""+new File(imgPath).exists());
						ImageView im = new ImageView(Act_SingleChat.this);
					
						im.setImageURI(Uri.fromFile(new File(imgPath)));
						return im;
					}
				}
				
				imgPath = ((ImageMessageBody)msg.getBody()).getRemoteUrl();
				
				Log.i("IMTESTIMAGE",imgPath);
				DoschoolApp.newImageLoader.displayImage(imgPath, ivImage.mImageView,DoschoolApp.dioSquare, new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplication(), "开始！", 1).show();
							}
						});
						ivImage.mCircleProgress.setProgress(0);
						ivImage.mCircleProgress.setVisibility(View.VISIBLE);

					}
					
					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplication(), "失败！", 1).show();
							}
						});
					}
					
					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplication(), "完成！", 1).show();
							}
						});
						ivImage.mCircleProgress
						.setVisibility(View.INVISIBLE);

					}
					
					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(getApplication(), "取消！", 1).show();
							}
						});

					}
				}, new ImageLoadingProgressListener() {
					@Override
					public void onProgressUpdate(String imageUri,
							View view, final int current, final int total) {
						Log.i("TOOOLonProgressUpdate", "" + 100 * current
								/ (total + 1));
						ivImage.mCircleProgress.setProgress(100 * current
								/ (total + 1));
					}
				});
				return ivImage;
			}
			return t;
		}
		
	}
	
}
