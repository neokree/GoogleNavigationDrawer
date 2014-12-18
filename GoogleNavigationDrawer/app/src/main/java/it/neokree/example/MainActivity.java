package it.neokree.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import it.neokree.googlenavigationdrawer.GAccount;
import it.neokree.googlenavigationdrawer.GAccountListener;
import it.neokree.googlenavigationdrawer.GoogleNavigationDrawer;

/**
 * Created by neokree on 17/12/14.
 */
public class MainActivity extends GoogleNavigationDrawer implements GAccountListener{


    @Override
    public void init(Bundle savedInstanceState) {

        this.addAccount(new GAccount("NeoKree","neokree@gmail.com",this.getResources().getDrawable(R.drawable.photo),this.getResources().getDrawable(R.drawable.mat1)));

        this.addSection(this.newSection("Sezione",new Fragment()));
        this.addSection(this.newSection("Sezione 2",new Fragment()));
        this.addDivisor();
        this.addSection(this.newSection("Sezione 3",new Fragment()).setSectionColor(Color.BLUE));
    }

    @Override
    public void onAccountOpening(GAccount account) {

    }
}
