package grodrich7.tfg.Activities;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import grodrich7.tfg.R;

public class ViewUserActiviy extends AppCompatActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ImageButton fullBtn;

    private double randomLatitudes[] = {
            41.392379,//Ronda Litoral
            41.386377,//Placa Universitat
            41.388732 //Everis
    };
    private double randomLongitudes[] = {
            2.202206, //Ronda Litoral
            2.164178,//Placa Universitat,
            2.128944 //Everis
    };

    private LatLng currentLocation;

    int geoIndex = 0;
    boolean full;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_activiy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        createMapFragment();
        fullBtn = findViewById(R.id.fullBtn);
        fullBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int targetHeight;
                DisplayMetrics displayMetrics = getMetrics();
                if (!full){
                    fullBtn.setImageResource(R.mipmap.unfull_window);
                    targetHeight = displayMetrics.heightPixels;
                    ResizeAnimation resizeAnimation = new ResizeAnimation(
                            mapFragment.getView(),
                            targetHeight,
                            mapFragment.getView().getLayoutParams().height
                    );
                    resizeAnimation.setDuration(1000);
                    mapFragment.getView().startAnimation(resizeAnimation);
                }else{
                    fullBtn.setImageResource(R.mipmap.full_window);
                    targetHeight = displayMetrics.heightPixels/4;
                    ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
                    params.height = getMetrics().heightPixels/4; //25%
                    mapFragment.getView().setLayoutParams(params);
                }
                full = !full;

            }
        });
    }
    //region ToolbarSettings

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.refresh_Action:
                updateLocation();
                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_right_in, R.anim.transition_right_out);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    //endregion
    //region MapFragment
    public void createMapFragment(){
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = getMetrics().heightPixels/4; //25%
        mapFragment.getView().setLayoutParams(params);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        currentLocation = getRandomLocation();
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Marker"));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.moveCamera(getCameraPosition());
        googleMap.setTrafficEnabled(true);
        this.googleMap = googleMap;
    }

    public LatLng getRandomLocation(){
        try{
            geoIndex++;
            return new LatLng(randomLatitudes[geoIndex], randomLongitudes[geoIndex]);
        }catch(ArrayIndexOutOfBoundsException ex){
            geoIndex = 0;
            return new LatLng(randomLatitudes[geoIndex], randomLongitudes[geoIndex]);
        }
    }

    public void updateLocation(){
        currentLocation = getRandomLocation();
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker"));
        googleMap.moveCamera(getCameraPosition());
    }

    public CameraUpdate getCameraPosition(){
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currentLocation).zoom(14.0f).build());
    }
    //endregion

    public DisplayMetrics getMetrics(){
        return getApplicationContext().getResources().getDisplayMetrics();
    }

    public class ResizeAnimation extends Animation {
        final int targetHeight;
        View view;
        int startHeight;

        public ResizeAnimation(View view, int targetHeight, int startHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
            //to support decent animation, change new heigt as Nico S. recommended in comments
            //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
