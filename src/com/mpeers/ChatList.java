package com.mpeers;

import com.mpeers.connection.SocketConnection;
import com.mpeers.data.ChatManager;
import com.mpeers.data.UserManager;
import com.mpeers.data.UserManager.UserState;
import com.mpeers.data.entities.Chat;
import com.mpeers.service.ConnectionManager;

import android.R.drawable;
import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatList extends ListActivity implements ChatListChangeListner{
	private static final int SEARCH_MENU_ITEM = Menu.FIRST;
	private static final int SETTINGS_MENU_ITEM = Menu.FIRST + 1;
	private static final int LOGOUT_MENU_ITEM = Menu.FIRST + 2;


	ListView listView;
	private ChatManager chatManager;
	private UserManager userManager;
	private ChatListAdapter listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();				
		actionBar.setSubtitle("Inbox");
		actionBar.setTitle("Conversations");
		
		userManager = UserManager.getInstance(this);		

		chatManager = ChatManager.getInstance(this);
		chatManager.addChatListChangeListner(this);

		listView = getListView(); 
		listAdapter = new ChatListAdapter(this, chatManager.getCurrentChats());

		listView.setAdapter(listAdapter);        

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {				
				Chat current = chatManager.getCurrentChats().get(position);
				
				Intent upanel = new Intent(getApplicationContext(), ChatWindow.class);
				upanel.putExtra("CHAT_ID", current.id);
				startActivity(upanel);
			}
		});	

		new CheckUserLogin().execute();			
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		//listAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		int menuItemOrder = Menu.NONE;		

		MenuItem searchMenuItem = menu.add(Menu.NONE, SEARCH_MENU_ITEM, menuItemOrder, "Search");
		searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		searchMenuItem.setIcon(android.R.drawable.ic_search_category_default );
		menu.add(Menu.NONE, SETTINGS_MENU_ITEM,	menuItemOrder, "Settings");
		menu.add(Menu.NONE, LOGOUT_MENU_ITEM, menuItemOrder, "Log out");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent actIntent;
		switch (item.getItemId()) {
		case (android.R.id.home) :
			actIntent = new Intent(this, ChatList.class);
		actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(actIntent);
		return true;
		case LOGOUT_MENU_ITEM: new LogoutUser().execute();
		return true;
		case SETTINGS_MENU_ITEM: actIntent = new Intent(this, Settings.class);
		startActivity(actIntent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onChatListChangeListner() {
		// TODO Auto-generated method stub
		/*Handler mainHandler = new Handler(ChatList.this.getMainLooper());		
		mainHandler.post(new Runnable() {
			@Override
			public void run() {				
				listAdapter.notifyDataSetChanged();
			}
		});*/
		
		ChatList.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	public void startLoginActivity(){
		Intent upanel = new Intent(getApplicationContext(), Login.class);
		upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(upanel);
	}

	private class GetChatList extends AsyncTask<String, String, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			pDialog = new ProgressDialog(ChatList.this);
			pDialog.setTitle("Loading");
			pDialog.setMessage("initialising ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			chatManager.getAllChatsFromDb();		
			return true;
		}

		@Override
		protected void onPostExecute(Boolean loggedIn) {
			listAdapter.notifyDataSetChanged();
			pDialog.dismiss();
		}
	}

	private class CheckUserLogin extends AsyncTask<String, String, UserState> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			pDialog = new ProgressDialog(ChatList.this);
			pDialog.setTitle("Loading");
			pDialog.setMessage("initialising ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected UserState doInBackground(String... params) {
			// TODO Auto-generated method stub					
			return userManager.checkUser();
		}

		@Override
		protected void onPostExecute(UserState state) {
			pDialog.dismiss();
			switch(state){
			case NO_ACCOUNT : startLoginActivity();
			break;
			case LOGGED_IN:
				new GetChatList().execute();
				break;
			case NOT_LOGGED_IN:
				//new GetChatList().execute();
				break;
			default:
				break;		 
			}		
		}
	}

	private class LogoutUser extends AsyncTask<String, String, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			pDialog = new ProgressDialog(ChatList.this);
			pDialog.setTitle("Logging out");
			pDialog.setMessage("please wait ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub					
			return userManager.logoutUser();
		}

		@Override
		protected void onPostExecute(Boolean state) {
			pDialog.dismiss();
			if(state){
				startLoginActivity();
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Error logging out", Toast.LENGTH_LONG).show();
			}
		}
	}


}
