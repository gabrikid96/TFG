package grodrich7.tfg.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import grodrich7.tfg.Activities.Services.NotificationService;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.R;

import static grodrich7.tfg.Models.Constants.WEATHER_URL;

public class ViewUserActivity extends HelperActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private ImageButton fullBtn;

    private TextView driving;
    private TextView destinationData;
    private TextView startTimeData;
    private TextView acceptCallsData;
    private TextView parkingData;
    private RecyclerView recyclerView;
    private DrivingData drivingData;
    boolean full;
    private String friendUid;

    public static final String VIEW_ACTION  = "VIEW";

    private RequestQueue mRequestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);


        friendUid = (String) getIntent().getSerializableExtra("key");
        getData(friendUid);
    }

    private void getData(String friendUid){
        drivingData = new DrivingData();
        controller.getDataReference().child(friendUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot drivingDataSnapshot : groupSnapshot.getChildren()){
                            if (drivingDataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                drivingData.merge(drivingDataSnapshot.getValue(DrivingData.class));
                            }
                          }
                    }
                }catch (Exception ex){
                    Log.e("VIEW", ex.getMessage());
                }finally {
                    putData(drivingData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_view_user_activiy);

        createMapFragment();
        String name = (String) getIntent().getSerializableExtra("name");
        enableToolbar(name);
        fullBtn = findViewById(R.id.fullBtn);
        fullBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMetrics displayMetrics = getMetrics();
                fullBtn.setBackgroundResource(!full ? R.mipmap.unfull_window : R.mipmap.full_window);
                ResizeAnimation resizeAnimation = new ResizeAnimation(
                        mapFragment.getView(),
                        !full ? displayMetrics.heightPixels : displayMetrics.heightPixels/4,
                        mapFragment.getView().getLayoutParams().height
                );
                resizeAnimation.setDuration(1000);
                mapFragment.getView().startAnimation(resizeAnimation);
                full = !full;
            }
        });

        driving = findViewById(R.id.drivingData);
        destinationData = findViewById(R.id.destinationData);
        startTimeData = findViewById(R.id.startTimeData);
        acceptCallsData = findViewById(R.id.acceptCallsData);
        parkingData = findViewById(R.id.parkingData);
    }

    private void recyclerImages(){
        recyclerView = findViewById(R.id.recyclerImages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.Adapter groupsAdapter = new RecyclerView.Adapter<ImageHolder>() {
            @Override
            public ImageHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_square
                        , viewGroup, false);
                return new ImageHolder(view);
            }

            @Override
            public void onBindViewHolder(final ImageHolder viewHolder, int i) {
                viewHolder.description.setText(getString(R.string.image) + String.valueOf(i+1));
                try{
                    String url = drivingData.getImages().get(i);
                    //Glide.with()

                    //viewHolder.imageButton.setImageResource(R.drawable.front_image);
                    final ImagePopup imagePopup = new ImagePopup(ViewUserActivity.this);
                    imagePopup.setFullScreen(true); // Optional
                    imagePopup.setBackgroundColor(getResources().getColor(R.color.transparent));
                    imagePopup.setImageOnClickClose(true);  // Optional

                    //imagePopup.initiatePopup(viewHolder.imageButton.getDrawable());
                    imagePopup.initiatePopupWithGlide(url);
                    Glide.with(ViewUserActivity.this).load(url).into(viewHolder.imageButton);
                    viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Initiate Popup view
                            pressEffect(viewHolder.imageButton);
                            imagePopup.viewPopup();
                        }
                    });
                }catch (Exception ex){
                    Log.e("REC_IMAGES", ex.getMessage());
                }
            }

            @Override
            public int getItemCount() {
                return drivingData.getImages().size();
            }
        };
        recyclerView.setAdapter(groupsAdapter);
    }

    private class ImageHolder extends RecyclerView.ViewHolder {

        private TextView description;
        private ImageView imageButton;

        public ImageHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.image);
            description = itemView.findViewById(R.id.description);
        }
    }

    private void putData(DrivingData data){
        int booleanColor = data.isDriving() != null && data.isDriving() ? Color.GREEN : Color.RED;
        driving.setText(parseString(data.isDriving()));
        driving.setTextColor(booleanColor);

        destinationData.setText(data.getDestination() != null &&
                !data.getDestination().isEmpty() ? data.getDestination() :
                getResources().getString(R.string.unknownInformation));

        Integer hour = data.getStartTimeHour();
        Integer min = data.getStartTimeMin();
        startTimeData.setText(hour == null && min == null ? "--:--" : String.format("%02d", hour) + ":" + String.format("%02d", min));

        booleanColor = data.isAcceptCalls() != null && data.isAcceptCalls() ? Color.GREEN : Color.RED;
        acceptCallsData.setText(parseString(data.isAcceptCalls()));
        acceptCallsData.setTextColor(booleanColor);

        booleanColor = data.isSearchingParking() != null && data.isSearchingParking() ? Color.GREEN : Color.RED;
        parkingData.setText(parseString(data.isSearchingParking()));
        parkingData.setTextColor(booleanColor);
        updateLocation();
        if (drivingData.getImages() != null){
            recyclerImages();
        }

    }

    public DisplayMetrics getMetrics(){
        return getApplicationContext().getResources().getDisplayMetrics();
    }

    //region ToolbarSettings
    /*@Override
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
                putData(drivingData);
                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }*/
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
        googleMap.setMapType(GoogleMap.	MAP_TYPE_NORMAL	);
       // googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(true);
        this.googleMap = googleMap;
    }

    public void updateLocation(){
        LatLng location = getLocation();
        if (location == null){
            findViewById(R.id.unkown_label).setVisibility(View.VISIBLE);
        }else{
            String moment = getString(R.string.location_time) + " " + drivingData.getLocationInfo().getLastLocationTime();
            findViewById(R.id.unkown_label).setVisibility(View.GONE);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location)
                            .title(getString(R.string.location_info))
                            .snippet(moment)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_marker_icon)));
            LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            googleMap.setInfoWindowAdapter(new InfoWindow(inflater, moment));
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                }
            });
            googleMap.moveCamera(getCameraPosition(location));
        }

    }

    public LatLng getLocation(){
        return drivingData.getLocationInfo() != null ?
                new LatLng(Double.parseDouble(drivingData.getLocationInfo().getLat()),
                        Double.parseDouble(drivingData.getLocationInfo().getLon())) :
                null;
    }

    public CameraUpdate getCameraPosition(LatLng currLatLng){
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currLatLng).zoom(14.0f).build());
    }
    //endregion
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() != null){
            switch (intent.getAction()){
                case VIEW_ACTION:
                    if (isServiceRunning(NotificationService.class)) stopService(new Intent(this, NotificationService.class));
                    break;
            }
        }
        super.onNewIntent(intent);

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
            //int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
            int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
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

    public class InfoWindow implements GoogleMap.InfoWindowAdapter {

        private View popup;
        private LayoutInflater inflater;
        private String moment;
        private String description;
        private String icon;
        private Double temp;


        public InfoWindow(LayoutInflater inflater, String moment) {
            super();
            this.inflater = inflater;
            this.moment = moment;
            getWeatherInformation();
        }

        public InfoWindow(LayoutInflater layoutInflater) {
            super();
            this.inflater = layoutInflater;
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (popup == null) {
                popup = inflater.inflate(R.layout.marker_layout, null);
            }
            TextView tvTitle = (TextView) popup.findViewById(R.id.title);
            tvTitle.setText(marker.getTitle());

            TextView tvSnippet = (TextView) popup.findViewById(R.id.moment);
            tvSnippet.setText(moment);

            LinearLayout weatherLayout = (LinearLayout) popup.findViewById(R.id.weather_info);
            weatherLayout.setVisibility(View.GONE);
            if (description != null && icon != null && temp != null){
                ((TextView)weatherLayout.findViewById(R.id.description_label)).setText(" " + description);
                ((TextView)weatherLayout.findViewById(R.id.temperature)).setText(String.valueOf(temp)+"ºC");
                Glide.with(ViewUserActivity.this).load(icon).into((ImageView) weatherLayout.findViewById(R.id.weather_icon));
                weatherLayout.setVisibility(View.VISIBLE);
            }
            return popup;
        }

        @Override
        public View getInfoWindow(Marker marker) {

            return null;
        }

        public void getWeatherInformation(){
            String url = WEATHER_URL + "lat=" + drivingData.getLocationInfo().getLat() + "&lon="+drivingData.getLocationInfo().getLon() + "&units=metric"
                    + "&appId=5fe1e836d1bf995e7f336ec370666161" + "&lang=" + Locale.getDefault().getLanguage();
            //
            mRequestQueue.start();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                                description = weather.getString("description");
                                icon = "http://openweathermap.org/img/w/" + weather.getString("icon") + ".png";
                                temp = response.getJSONObject("main").getDouble("temp");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mRequestQueue.stop();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mRequestQueue.stop();
                        }
                    });
            mRequestQueue.add(jsonObjectRequest);
        }

    }

}
