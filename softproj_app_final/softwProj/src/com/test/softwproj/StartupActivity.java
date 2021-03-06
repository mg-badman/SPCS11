package com.test.softwproj;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartupActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		
		 final Button button = (Button) findViewById(R.id.button1);
		 final Context c = this.getApplicationContext();
	     button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View view) {
		    	EditText usrname =  (EditText)findViewById(R.id.usrName1);
		    	String usernm = usrname.getText().toString();
		        Toast.makeText(c, "Joining chat with name "+usernm, Toast.LENGTH_SHORT).show();
		        Intent intent = new Intent(c, MainActivity.class);
		        
		        Bundle b = new Bundle();
		        b.putString("user", usernm); 
		        intent.putExtras(b); 
		        
		        startActivity(intent);
		        finish();
		    }
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
