package com.mpeers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mpeers.data.UserManager;
import com.mpeers.data.entities.User;
import com.mpeers.service.ConnectionManager;
import com.mpeers.service.ConnectionManager.ConnectionBinder;

public class Login extends Activity implements onUserLoginListner{

	Button btnLogin;
	Button Btnregister;
	Button passreset;
	EditText inputEmail;
	EditText inputPassword;
	private TextView loginErrorMsg;
	/**
	 * Called when the activity is first created.
	 */
	private static String KEY_SUCCESS = "success";
	private static String KEY_UID = "uid";
	private static String KEY_USERNAME = "uname";
	private static String KEY_FIRSTNAME = "fname";
	private static String KEY_LASTNAME = "lname";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "created_at";
	
	public ProgressDialog loginDialog;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.pword);
		Btnregister = (Button) findViewById(R.id.registerbtn);
		btnLogin = (Button) findViewById(R.id.login);
		passreset = (Button)findViewById(R.id.passres);
		loginErrorMsg = (TextView) findViewById(R.id.loginErrorMsg);

		passreset.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), PasswordReset.class);
				startActivityForResult(myIntent, 0);
				finish();
			}});


		Btnregister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Register.class);
				startActivityForResult(myIntent, 0);
				finish();
			}});

		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				if (  ( !inputEmail.getText().toString().equals("")) && ( !inputPassword.getText().toString().equals("")) )
				{
					//NetAsync(view);
					loginDialog = new ProgressDialog(Login.this);
					loginDialog.setTitle("Contacting Servers");
					loginDialog.setMessage("Logging in ...");
					loginDialog.setIndeterminate(false);
					loginDialog.setCancelable(true);
					loginDialog.show();
					
					String username = inputEmail.getText().toString().trim();
					String password = inputPassword.getText().toString().trim();
					
					Intent intent = new Intent(Login.this, ConnectionManager.class);
					intent.putExtra("type", "UserLogin");
					intent.putExtra("username", username);
					intent.putExtra("password", password);
					startService(intent);
				}
				else if ( ( !inputEmail.getText().toString().equals("")) )
				{
					Toast.makeText(getApplicationContext(),
							"Password field empty", Toast.LENGTH_SHORT).show();
				}
				else if ( ( !inputPassword.getText().toString().equals("")) )
				{
					Toast.makeText(getApplicationContext(),
							"Email field empty", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(getApplicationContext(),
							"Email and Password field are empty", Toast.LENGTH_SHORT).show();
				}
			}
		});		
		
		UserManager.getInstance(this).addLoginListner(this);
	}

	private ConnectionManager mService;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			System.out.println("bound service");
			ConnectionBinder binder = (ConnectionBinder) service;
			mService = binder.getService();
			User current = mService.getCurrentUser();
			System.out.println("current user: " + current.displayName);
			if(current != null)
				Toast.makeText(getApplicationContext(),
					"current user: " + current.displayName, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};


	/**
	 * Async Task to check whether internet connection is working.
	 **/

	private class NetCheck extends AsyncTask<String,String,Boolean>
	{
		private ProgressDialog nDialog;

		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			nDialog = new ProgressDialog(Login.this);
			nDialog.setTitle("Checking Network");
			nDialog.setMessage("Loading..");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			nDialog.show();
		}
		/**
		 * Gets current device state and checks for working internet connection by trying Google.
		 **/
		@Override
		protected Boolean doInBackground(String... args){
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				try {
					URL url = new URL("http://www.google.com");
					HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
					urlc.setConnectTimeout(3000);
					urlc.connect();
					if (urlc.getResponseCode() == 200) {
						return true;
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean th){
			if(th == true){
				nDialog.dismiss();
				new ProcessLogin().execute();
				
				//Intent intent = new Intent(Login.this, ConnectionManager.class);
		        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			}
			else{
				nDialog.dismiss();
				loginErrorMsg.setText("Error in Network Connection");
			}
		}
	}

	private class ProcessLogin extends AsyncTask<String, String, Boolean> {
		private ProgressDialog pDialog;
		String email,password;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			inputEmail = (EditText) findViewById(R.id.email);
			inputPassword = (EditText) findViewById(R.id.pword);
			email = inputEmail.getText().toString();
			password = inputPassword.getText().toString();
			pDialog = new ProgressDialog(Login.this);
			pDialog.setTitle("Contacting Servers");
			pDialog.setMessage("Logging in ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... args) {			
			User currentUser = UserManager.getInstance(Login.this).loginUser(email, password);
			if(currentUser != null)
				return true;
			return false;
		}

		@Override
		protected void onPostExecute(Boolean loggedIn) {
			if(loggedIn){
				pDialog.setMessage("Loading User Space");
				pDialog.setTitle("Getting Data");			
				pDialog.dismiss();
				startMainActivity();						
				finish();
			} else {
				pDialog.dismiss();
				loginErrorMsg.setText("Incorrect username/password");
			}							
		}
	}

	public void NetAsync(View view){
		new NetCheck().execute();
	}
	
	@Override
	public void onUserLogin(User user){
		/*Toast.makeText(getApplicationContext(),
				"current user: " + user.displayName, Toast.LENGTH_SHORT).show();*/
		loginDialog.dismiss();
		System.out.println("current user: " + user.displayName);
		startMainActivity();						
		finish();
	}

	public void startMainActivity(){
		Intent upanel = new Intent(getApplicationContext(), ChatList.class);
		upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(upanel);
	}
}