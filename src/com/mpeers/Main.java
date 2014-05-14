package com.mpeers;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mpeers.data.UserManager;
import com.mpeers.library.UserFunctions;
import com.mpeers.library.DatabaseHandler;

import java.util.HashMap;

public class Main extends Activity {
	Button btnLogout;
	Button changepas;

	@Override
	public void onResume(){
		super.onResume();
		if(UserManager.getInstance(this).getCurrentUser() == null){
			Toast.makeText(getApplicationContext(),
					"not logged in", Toast.LENGTH_SHORT).show();   
		} else {
			Toast.makeText(getApplicationContext(),
					"logged in", Toast.LENGTH_SHORT).show();
		}
	}


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		changepas = (Button) findViewById(R.id.btchangepass);
		btnLogout = (Button) findViewById(R.id.logout);

		DatabaseHandler db = new DatabaseHandler(getApplicationContext());

		/**
		 * Hashmap to load data from the Sqlite database
		 **/
		HashMap<String,String> user = new HashMap<String, String>();
		user = db.getUserDetails();


		/**
		 * Change Password Activity Started
		 **/
		changepas.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0){

				Intent chgpass = new Intent(getApplicationContext(), ChangePassword.class);

				startActivity(chgpass);
			}

		});

		/**
		 *Logout from the User Panel which clears the data in Sqlite database
		 **/
		btnLogout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {

				//UserFunctions logout = new UserFunctions();
				//logout.logoutUser(getApplicationContext());           	
				new LogoutUser().execute();			
			}
		});        
		/**
		 * Sets user first name and last name in text view.
		 **/
		final TextView login = (TextView) findViewById(R.id.textwelcome);
		login.setText("Welcome  "+user.get("fname"));
		final TextView lname = (TextView) findViewById(R.id.lname);
		lname.setText(user.get("lname"));


	}
	
	private class LogoutUser extends AsyncTask<String, String, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();			
			pDialog = new ProgressDialog(Main.this);
			pDialog.setTitle("Contacting Servers");
			pDialog.setMessage("Logging out ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			if(UserManager.getInstance(Main.this).logoutUser()){
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean loggedIn) {
			if(loggedIn){    							
				pDialog.dismiss();
				startLoginActivity();						
				finish();
			} else {
				pDialog.dismiss();    				
			}							
		}

	}
    
    public void startLoginActivity(){
    	Intent login = new Intent(getApplicationContext(), Login.class);
		login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(login);
	}

}