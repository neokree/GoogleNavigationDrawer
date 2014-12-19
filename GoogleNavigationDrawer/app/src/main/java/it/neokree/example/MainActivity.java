package it.neokree.example;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import it.neokree.googlenavigationdrawer.GAccount;
import it.neokree.googlenavigationdrawer.GAccountListener;
import it.neokree.googlenavigationdrawer.GSection;
import it.neokree.googlenavigationdrawer.GoogleNavigationDrawer;

/**
 * Created by neokree on 17/12/14.
 */
public class MainActivity extends GoogleNavigationDrawer implements GAccountListener{

    GAccount account;
    GSection section1, section2, recorder,night,last,settingsSection;

    @Override
    public void init(Bundle savedInstanceState) {

        account = new GAccount("NeoKree","neokree@gmail.com",new ColorDrawable(Color.parseColor("#9e9e9e")),this.getResources().getDrawable(R.drawable.bamboo));
        this.addAccount(account);

        this.setAccountListener(this);

        // create sections
        section1 = this.newSection("Section 1",new FragmentIndex());
        section2 = this.newSection("Section 2",new FragmentIndex());
        // recorder section with icon and 10 notifications
        recorder = this.newSection("Recorder",this.getResources().getDrawable(R.drawable.ic_mic_white_24dp),new FragmentIndex()).setNotifications(10);
        // night section with icon, section color and notifications
        night = this.newSection("Night Section", this.getResources().getDrawable(R.drawable.ic_hotel_grey600_24dp), new FragmentIndex())
                .setSectionColor(Color.parseColor("#2196f3")).setNotifications(150);
        // night section with section color
        last = this.newSection("Last Section", new FragmentIndex()).setSectionColor((Color.parseColor("#ff9800")));

        Intent i = new Intent(this,Settings.class);
        settingsSection = this.newSection("Settings",this.getResources().getDrawable(R.drawable.ic_settings_black_24dp),i);

        // add your sections to the drawer
        this.addSection(section1);
        this.addSection(section2);
        this.addDivisor();
        this.addSection(recorder);
        this.addSection(night);
        this.addDivisor();
        this.addSection(last);
        this.addBottomSection(settingsSection);

        // start thread
        t.start();

    }


    @Override
    public void onAccountOpening(GAccount account) {
        // open account activity or do what you want
    }

    // after 5 second (async task loading photo from website) change user photo
    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                account.setPhoto(getResources().getDrawable(R.drawable.photo));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyAccountDataChanged();
                        Toast.makeText(getApplicationContext(), "Loaded 'from web' user image", Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.w("PHOTO","user account photo setted");


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
}
