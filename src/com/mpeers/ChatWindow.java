package com.mpeers;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mpeers.connection.NewChatMessageListner;
import com.mpeers.data.ChatManager;
import com.mpeers.data.ChatMessageManager;
import com.mpeers.data.entities.Chat;
import com.mpeers.data.entities.ChatMessage;

public class ChatWindow extends ListActivity implements NewChatMessageListner{

	private Chat currentChat;
	private ArrayList<ChatMessage> chatMessages;
	private ChatMessageAdapter msgAdapter;
	private Button sendMsgButton;
	private EditText msgEditText;
	private ListView listView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chat);

		listView = getListView();		
		sendMsgButton = (Button) findViewById(R.id.send_msg_button);
		msgEditText = (EditText) findViewById(R.id.msg_text);
		
		sendMsgButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String msg = msgEditText.getText().toString().trim();				
				new SendMessage().execute(msg);
				hideSoftKeyboard(ChatWindow.this, view);
			}});
		
		Intent intent = getIntent();
		try {			
			String chatId = intent.getExtras().getString("CHAT_ID");
			currentChat = ChatManager.getInstance(this).getChatById(chatId);			
		} catch(Exception e){
			e.printStackTrace();
			finish();
		}		
		
		chatMessages = new ArrayList<ChatMessage>();
		msgAdapter = new ChatMessageAdapter(this, chatMessages, currentChat);
		setListAdapter(msgAdapter);
		
		ChatMessageManager.getInstance(this).addNewMessageListner(this);
		
		new GetChatMessages().execute();
	}
	
	private class SendMessage extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();		
		}

		@Override
		protected Boolean doInBackground(String... params) {
			ChatMessageManager.getInstance(ChatWindow.this).sendMessage(currentChat.room, params[0]);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean loggedIn) {			
		}
	}
	
	private class GetChatMessages extends AsyncTask<String, String, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			pDialog = new ProgressDialog(ChatWindow.this);
			pDialog.setTitle("Loading");
			pDialog.setMessage("getting conversation ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub			
			chatMessages.clear();
			chatMessages.addAll(ChatMessageManager.getInstance(ChatWindow.this).getRoomMessages(currentChat.room));				
			return true;
		}

		@Override
		protected void onPostExecute(Boolean loggedIn) {
			msgAdapter.notifyDataSetChanged();			
			msgEditText.setText("");
			msgEditText.invalidate();			
			pDialog.dismiss();
		}
	}
	
	public static void hideSoftKeyboard (Activity activity, View view) 
	{
	    InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	}

	@Override
	public void onNewChatMessage(final ChatMessage newMsg) {
		// TODO Auto-generated method stub
		ChatWindow.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				chatMessages.add(newMsg);
				msgAdapter.notifyDataSetChanged();		
				listView.setSelection(msgAdapter.getCount() - 1);
			}
		});		
	}

}
