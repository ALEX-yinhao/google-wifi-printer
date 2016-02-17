//package com.example.wonders.mobile.printer;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.print.PrintManager;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//
//import com.example.wonders.mobile.printer.adapter.MyPrintDocumentAdapter;
//
//public class MainActivity extends AppCompatActivity {
//    private Context context;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        context = MainActivity.this;
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//
//            }
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void doPrint() {
//        // Get a PrintManager instance
//        PrintManager printManager = (PrintManager)context.getSystemService(Context.PRINT_SERVICE);
//
//        // Set job name, which will be displayed in the print queue
//        String jobName = context.getString(R.string.app_name) + " Document";
//
//        // Start a print job, passing in a PrintDocumentAdapter implementation
//        // to handle the generation of a print document
//        printManager.print(jobName, new MyPrintDocumentAdapter(context),
//                null); //
//    }
//}
