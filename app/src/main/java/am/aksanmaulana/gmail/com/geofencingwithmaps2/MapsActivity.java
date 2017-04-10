package am.aksanmaulana.gmail.com.geofencingwithmaps2;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private Marker mPosSekarang;
    private static final int MY_PERMISSIONS_REQUEST = 99;//int bebas, maks 1 byte
    GoogleApiClient mGoogleApiClient ;
    //Location mLastLocation;
    String strKeterangan;
    double LatNow, LongNow;

    LocationRequest mLocationRequest;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        //10 detik sekali minta lokasi (10000ms = 10 detik)
        mLocationRequest.setInterval(5000);
        //tapi tidak boleh lebih cepat dari 5 detik
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void ambilLokasi() {
   /* mulai Android 6 (API 23), pemberian persmission
    dilakukan secara dinamik (tdk diawal)
    untuk jenis2 persmisson tertentu, termasuk lokasi
    */

        // cek apakah sudah diijinkan oleh user, jika belum tampilkan dialog
        if (ActivityCompat.checkSelfPermission (this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        //set agar setiap update lokasi maka UI bisa diupdate
        //setiap ada update maka onLocationChanged akan dipanggil
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    //muncul dialog & user memberikan reson (allow/deny), method ini akan dipanggil
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                ambilLokasi();
            } else {
                //permssion tidak diberikan, tampilkan pesan
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setMessage("Tidak mendapat ijin, tidak dapat mengambil lokasi");
                ad.show();
            }
            return;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();
        createLocationRequest();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //batas lokasi UPI
        //urutan harus kiri bawah, kanan atas kotak

        LatLng posSekarang = new LatLng(LatNow, LongNow);
        mPosSekarang = mMap.addMarker(new
                MarkerOptions().position(posSekarang).title("PosSekarang"));

        //marker gedung ilkom
        LatLng gedungIlkom = new LatLng(-6.860418, 107.589889);
        mMap.addMarker(new MarkerOptions().position(gedungIlkom).title("Marker di GIK"));

        //zoom ke gedung ilkom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posSekarang, 17));

        // batas UPI, kiri-bawah dan kanan-atas (urutan harus spt itu)
        LatLngBounds UPI = new LatLngBounds(
                new LatLng(-6.863273, 107.587212),new LatLng(-6.858025, 107.597839));

        // Set kamera sesuai batas UPI
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset dari edges
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(UPI, width, height, padding));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ambilLokasi();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        //AlertDialog ad = new AlertDialog.Builder(this).create();
        //ad.setMessage("update lokasi");
        //ad.show();
        mPosSekarang.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        LatNow = location.getLatitude();
        LongNow = location.getLongitude();

        Log.i("shadow",String.valueOf(LatNow));
        Log.i("shadow",String.valueOf(LongNow));

        // pengecekan kalau si user berada di area GIK
        if((LatNow >= -6.860363 && LatNow <= -6.860195) &&
                (LongNow >= 107.589644 && LongNow <= 107.590127)) {

            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setMessage("Banyak pelanggan!");
            ad.show();

            strKeterangan = "disini banyak pelanggan!";
            Log.i("shadow","Masuk");
        }else{
            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setMessage("Sedikit pelanggan!");
            ad.show();

            strKeterangan = "disini sedikit pelanggan!";
            Log.i("shadow","Tidak");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
