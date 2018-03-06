package miage.parisnanterre.fr.runwithme;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.FloatMath;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RunningActivity extends AppCompatActivity {

    Button button_rythme;
    Button button_distance;
    Button button_distance_titre;
    Button button_cal;
    LocationManager  locationManager;
    LocationListener locationListener;

    Location current_location;
    Location last_location;
    double distance;
    double speed;
    java.text.DecimalFormat df;
    java.text.DecimalFormat df2;
    Chronometer simpleChronometer;
    ImageView play_and_stop;

    boolean isLaunch;

    String date, heure, distancee,duree,rythme, calories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        getSupportActionBar().hide();

        button_distance= findViewById(R.id.button_distance_display);
        button_distance_titre = findViewById(R.id.button_distance);
        button_rythme= findViewById(R.id.button_pace_display);
        button_cal = findViewById(R.id.button_cal_display);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        current_location = new Location("current_location");
        current_location.setLatitude(40);
        current_location.setLongitude(12);
        last_location = new Location("last_location");
        distance = 0.90;
        isLaunch = false;
        df = new java.text.DecimalFormat("0.#");
        df2 =new java.text.DecimalFormat("0.##");
        simpleChronometer = findViewById(R.id.textView_MIN_DISPLAY);




        play_and_stop = findViewById(R.id.imageView_start_and_stop);
        currentContext = this;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //last_location.setLatitude(location.getLatitude());
                //last_location.setLongitude(location.getLongitude());
                last_location.setLatitude(current_location.getLatitude());
                last_location.setLongitude(current_location.getLongitude());
                current_location.setLatitude(location.getLatitude());
                current_location.setLongitude(location.getLongitude());
                //distance = distance + current_location.distanceTo(last_location)/1000;
                //distance = current_location.ge* distance;
                distance += meterDistanceBetweenPoints((float) current_location.getLatitude(), (float) current_location.getLongitude(), (float) last_location.getLatitude(),(float) last_location.getLongitude())/1000;

                //distance = distance*1.01;

                if(distance*1000 > 1000){
                    button_distance_titre.setText("Distance(km)");
                    button_distance.setText(""+df2.format(distance));
                }else{
                    button_distance.setText(""+df.format(distance*1000));
                }
                calculateAndDisplaySpeed();
                calcul_kcak();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
    }




    void configureLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 500, 0, locationListener);
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    private void calculateAndDisplaySpeed(){
        //int speed = distance/simpleChronometer.getBase();
        double meter = distance *1000;
        long elapsedMillis = SystemClock.elapsedRealtime() - simpleChronometer.getBase();
        double second = elapsedMillis/1000;
        button_rythme.setText(""+df.format(meter/second));
    }


    public void do_click_tracking(View v) {


        //finish();
        if(isLaunch == false) {
            do_click_for_play_tracking();
        }else{
            do_click_for_stop_tracking();
        }

    }

    public void do_click_for_play_tracking(){
        /*Context context = getApplicationContext();
        CharSequence text = "clique play!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/
        isLaunch = true;
       play_and_stop.setImageResource(R.mipmap.ic_stop_foreground);
       //play_and_stop.setBackgroundColor(Color.rgb(219, 45, 45));

       simpleChronometer.setBase(SystemClock.elapsedRealtime());
       simpleChronometer.start(); // start a chronometer
        configureLocation();
        sendNotification();
    }

    BottomSheetDialog mBottomSheetDialog;
    public void do_click_for_stop_tracking(){
        /*
        Context context = getApplicationContext();
        CharSequence text = "clique stop!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        finish();
        */

        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = this.getLayoutInflater().inflate(R.layout.popup_after_tracking, null);

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
        mNotificationManager.cancelAll();

        Date currentTime = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println( sdf.format(cal.getTime()) );

        date = sdf.format(cal.getTime());
        cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println( sdf.format(cal.getTime()) );
        heure =sdf.format(cal.getTime());
        //#dev
        distancee = button_distance.getText().toString();
        duree =  String.valueOf((SystemClock.elapsedRealtime() - simpleChronometer.getBase())/1000 );
        rythme = button_rythme.getText().toString();
        calories = button_cal.getText().toString();
        RunningStatistics statistics = new RunningStatistics(date, heure, distancee, duree, rythme,calories);


        //ET_NAME.getText().toString();
        //user_name = ET_USER_NAME.getText().toString();
        //user_pass =ET_USER_PASS.getText().toString();
        String method = "ajout";
        PushStatsBackgroundTask backgroundTask = new PushStatsBackgroundTask(this);
        backgroundTask.execute(method,date,heure,distancee,duree,rythme,calories);
        //finish();

    }

    public void do_click_for_share(View v){
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = this.getLayoutInflater().inflate(R.layout.popup_share, null);

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();




    }

   /* public void userReg(View view)
    {

        date = "hello";
        heure = "hello";
        distancee = "hello";
        duree = "hello";
        rythme = "hello";
        calories = "hello";//ET_NAME.getText().toString();
        //user_name = ET_USER_NAME.getText().toString();
        //user_pass =ET_USER_PASS.getText().toString();
        String method = "ajout";
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute(method,date,heure,distancee,duree,rythme,calories);
        finish();

    }*/

    public void cancel_share(View v){

    }

    public void back_to_homeactivity(View v){
        finish();
    }

    public void calcul_kcak(){
        //Nombre de kcal dépensées = votre Poids (kg) x Distance (km)
        //distance parcourue x poid x 1.036
        button_cal.setText(""+df.format(distance*70*1.036));
    }


    public void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);

        mBuilder.setOngoing(true);

        //Create the intent that’ll fire when the user taps the notification//

        //Intent notificationIntent = new Intent(this, RunningActivity.class);
        //
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
         //       | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Not.addFlags(Intent.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR);
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        Intent notificationIntent = new Intent(currentContext, RunningActivity.currentContext.getClass() );
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setAction("android.intent.action.MAIN");
        notificationIntent.addCategory("android.intent.category.LAUNCHER");
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);




        mBuilder.setContentIntent(contentIntent);



        mBuilder.setSmallIcon(R.mipmap.ic_user);
        mBuilder.setContentTitle("Durée de la session");
        //mBuilder.setContentText("00:00");
        mBuilder.setUsesChronometer(true);
        mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());




    }

    public static Context currentContext;
    NotificationManager mNotificationManager;


}
