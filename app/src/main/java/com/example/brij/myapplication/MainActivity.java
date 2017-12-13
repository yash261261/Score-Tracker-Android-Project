package com.example.brij.myapplication;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.brij.myapplication.Database.Contract;
import com.example.brij.myapplication.Database.DBHelper;
import com.example.brij.myapplication.Database.DBUtils;
import com.example.brij.myapplication.Scheduler.SchedulerUtils;
import com.example.brij.myapplication.model.NBAData;
import com.example.brij.myapplication.utilities.NBAAdapter;
import com.example.brij.myapplication.utilities.NetworkUtils;
import com.example.brij.myapplication.utilities.parseJSON;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<ArrayList<Void>>, NBAAdapter.ItemClickListener {

    static final String TAG = "mainactivity";

    private TextView errorMessgaeTextView;

    private ProgressBar progressIndicator;

    private NavigationView navigationView;


    private Cursor cursor;
    private NBAAdapter nbaAdapter;
    private SQLiteDatabase db;

    private static final int LOADER = 1;

    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = new DBHelper(this).getReadableDatabase();
        cursor = DBUtils.getAllitems(db);
        nbaAdapter = new NBAAdapter(cursor, this);


        SchedulerUtils.scheduleRefresh(this);


        progressIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        errorMessgaeTextView = (TextView) findViewById(R.id.error_message_display);
        rv = (RecyclerView) findViewById(R.id.nba_response_result);

        rv.setAdapter(nbaAdapter);

        rv.setLayoutManager(new LinearLayoutManager(this));

    }


    @Override
    protected void onStart() {
        super.onStart();
        //Initialize the scheduler
        DBUtils.deleteNewsitem(db);
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(LOADER, null, this).forceLoad();
        SchedulerUtils.scheduleRefresh(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SchedulerUtils.stopScheduledNewsLoad(this);
    }

    private void showErrorMessage() {

        errorMessgaeTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();


        if (id == R.id.nav_nba) {
            Intent intent = new Intent(this, NbaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //String gameName = null;
            intent.putExtra("gameName", "nba");
            startActivity(intent);

        }
        if (id == R.id.nav_mlb) {
            Intent intent = new Intent(this, NbaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //String gameName = null;
            intent.putExtra("gameName", "mlb");
            startActivity(intent);

        }
        if (id == R.id.nav_all) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //String gameName = null;7
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            intent.putExtra("gameName", "all");
            startActivity(intent);

        }
        if(id == R.id.nav_live){
            Intent intent = new Intent(this, LiveScoreDemo.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //String gameName = null;7
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            //intent.putExtra("gameName", "all");
            startActivity(intent);

        }
        if(id == R.id.nav_schedule){
            Intent intent = new Intent(this, ScheduleGames.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //String gameName = null;7
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            //intent.putExtra("gameName", "all");
            startActivity(intent);

        }
        if(id == R.id.nav_schedule_mlb){
            Intent intent = new Intent(this, ScheduleGamesMLB.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(intent);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<ArrayList<Void>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ArrayList<Void>>(this) {


            @Override
            public void onStartLoading() {
                super.onStartLoading();


                progressIndicator.setVisibility(View.VISIBLE);

            }

            ArrayList<NBAData> nba = null;
            ArrayList<NBAData> mlb = null;

            ArrayList<NBAData> scoreFinal = new ArrayList<>();

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override

            public ArrayList<Void> loadInBackground() {

                try {
                    String jsonNBA = NetworkUtils.getResponseFromHttpUrl();
                    nba = parseJSON.parseJsonData(MainActivity.this, jsonNBA);

                    Log.d(TAG, "NBA------------" + jsonNBA);

                    String jsonMLB = NetworkUtils.getResponseFromHttpUrlMlb();
                    mlb = parseJSON.parseJsonDataMLB(MainActivity.this, jsonMLB);

//scoreFinal.addAll(nba);
//scoreFinal.addAll(mlb);
                    int nbaLength = nba.size();
                    int mlbLength = mlb.size();
                    if(nbaLength>=mlbLength){
                        for(int i = 0;i<nba.size();i++){
                            if(i!=mlbLength && i<mlbLength){
                                scoreFinal.add(mlb.get(i));
                                scoreFinal.add(nba.get(i));
                            }
                            else{
                                scoreFinal.add(nba.get(i));
                            }
                        }
                    }
                    else{
                        for(int i = 0;i<mlb.size();i++){
                            if(i!=nbaLength && i<nbaLength){
                                scoreFinal.add(mlb.get(i));
                                scoreFinal.add(nba.get(i));
                            }
                            else{
                                scoreFinal.add(mlb.get(i));
                            }
                        }
                    }


                    db = new DBHelper(this.getContext()).getWritableDatabase();
                    DBUtils.insertnews(db, scoreFinal);


                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Void>> loader, ArrayList<Void> data) {


        progressIndicator.setVisibility(View.INVISIBLE);
        errorMessgaeTextView.setVisibility(View.INVISIBLE);

        db = new DBHelper(MainActivity.this).getReadableDatabase();
        cursor = DBUtils.getAllitems(db);
        nbaAdapter = new NBAAdapter(cursor, this);
        rv.setAdapter(nbaAdapter);
        nbaAdapter.notifyDataSetChanged();


//
//        if (data!=null) {
//            Log.d(TAG, "g");
//            NBAAdapter adapter = new NBAAdapter(data);
//            rv.setAdapter(adapter);
//                NBAAdapter adapternhl = new NBAAdapter(newsNhl);
//                rv.setAdapter(adapternhl);

//        } else {
//            showErrorMessage();

        // }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Void>> loader) {

    }

//    public class NewsTask extends AsyncTask<String, Void, ArrayList<NBAData>> {
//
//
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressIndicator.setVisibility(View.VISIBLE);
//
//
//        }
//
//
//
//        @Override
//        protected ArrayList<NBAData> doInBackground(String... params) {
//            ArrayList<NBAData> nba= null;
//            ArrayList<NBAData> mlb= null;
//
//            //New arrayList to keep all scores togather.
//            ArrayList<NBAData> scoreFinal= new ArrayList<>();
//
//           // URL newsURL = NetworkUtils.buildUrl();
//            //Log.d(TAG, "url: " + newsURL.toString());
//
//            try {
//                //two calls for diffrent apis
//
//                //NBA api call
//                String jsonNBA = NetworkUtils.getResponseFromHttpUrl();
//                nba = parseJSON.parseJsonData(MainActivity.this , jsonNBA);
//
//                //MLB api call
//                String jsonMLB = NetworkUtils.getResponseFromHttpUrlMlb();
//                mlb = parseJSON.parseJsonData(MainActivity.this , jsonMLB);
//
//
//                //adding all api results.
//                scoreFinal.addAll(nba);
//                scoreFinal.addAll(mlb);;
//            }
//             catch(JSONException e) {
//                e.printStackTrace();
//            }
//            return scoreFinal;
//
//        }
//
//        @Override
//        protected void onPostExecute(final ArrayList<NBAData> data) {
//            super.onPostExecute(data);
//            progressIndicator.setVisibility(View.INVISIBLE);
//            errorMessgaeTextView.setVisibility(View.INVISIBLE);
//            if (data!=null) {
//                Log.d(TAG, "g");
//                NBAAdapter adapter = new NBAAdapter(data);
//                        rv.setAdapter(adapter);
////                NBAAdapter adapternhl = new NBAAdapter(newsNhl);
////                rv.setAdapter(adapternhl);
//
//            } else {
//                showErrorMessage();
//
//            }
//            //return data;
//        }
//
//    }

//    public void openWebPage(String url) {
//        Uri webpage = Uri.parse(url);
//        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemSelected = item.getItemId();
        if (itemSelected == R.id.action_search) {
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.restartLoader(LOADER, null, this).forceLoad();
        }

        return true;
    }

    @Override
    public void onItemClick(int clickedItemIndex) {
        SchedulerUtils.stopScheduledNewsLoad(this);
        cursor.moveToPosition(clickedItemIndex);
// Intent intent=new Intent(this,GameDetails.class);
        String hometeam = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_HOMETEAM));
        String awayteam = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_AWAYTEAM));
        String hometeamcity = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_HOMETEAMCITY));
        String awayteamcity = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_AWAYTEAMCITY));
        String hometeamscore = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_HOMESCORE));
        String awayteamscore = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_AWAYSCORE));
        String location = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_LOCATION));
        String gameDate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_GAMES.COLUMN_NAME_DATE));
        findViewById(R.id.nba_response_result).setVisibility(View.GONE);
        Bundle bundle = new Bundle();
        bundle.putString("hometeam", hometeam);
        bundle.putString("awayteam",awayteam);
        bundle.putString("hometeamcity",hometeamcity);
        bundle.putString("awayteamcity",awayteamcity);
        bundle.putString("hometeamscore",hometeamscore);
        bundle.putString("awayteamscore",awayteamscore);
        bundle.putString("location",location);
        bundle.putString("gameDate", gameDate);
        NbaFragment fragobj = new NbaFragment();
        fragobj.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentMain, fragobj)
                .addToBackStack(null)
                .commit();
    }
}
