package it.neokree.googlenavigationdrawer;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



import java.util.LinkedList;
import java.util.List;

/**
 * Activity that implements ActionBarActivity with a Navigation Drawer with Material Design style
 *
 * @author created by neokree
 */
@SuppressLint("InflateParams")
public abstract class GoogleNavigationDrawer<Fragment> extends ActionBarActivity implements GSectionListener {

    public static final int BOTTOM_SECTION_START = 100;

    private DrawerLayout layout;
    private ActionBar actionBar;
    private ActionBarDrawerToggle pulsante;
    private ImageView statusBar;
    private Toolbar toolbar;
    private RelativeLayout drawer;
    private ImageView userphoto;
    private ImageView usercover;
    private TextView username;
    private TextView usermail;
    private LinearLayout sections;
    private LinearLayout bottomSections;

    private List<GSection> sectionList;
    private List<GSection> bottomSectionList;
    private List<GAccount> accountManager;
    private GSection currentSection;
    private GAccount currentAccount;

    private CharSequence title;
    private static int indexFragment = 0;
    private float density;
    private int primaryColor;

    private View.OnClickListener currentAccountListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // enter into account properties

            if(accountListener != null) {
                accountListener.onAccountOpening(currentAccount);

                // close drawer
                layout.closeDrawer(drawer);
            }

        }
    };

    private GAccountListener accountListener;

    @Override
    /**
     * Do not Override this method!!! <br>
     * Use init() instead
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_navigation_drawer);
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // init toolbar & status bar
        statusBar = (ImageView) findViewById(R.id.statusBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // init drawer components
        drawer = (RelativeLayout) this.findViewById(R.id.drawer);
        username = (TextView) this.findViewById(R.id.user_nome);
        usermail = (TextView) this.findViewById(R.id.user_email);
        userphoto = (ImageView) this.findViewById(R.id.user_photo);
        usercover = (ImageView) this.findViewById(R.id.user_cover);
        sections = (LinearLayout) this.findViewById(R.id.sections);
        bottomSections = (LinearLayout) this.findViewById(R.id.bottom_sections);

        // init lists
        sectionList = new LinkedList<>();
        bottomSectionList = new LinkedList<>();
        accountManager = new LinkedList<>();

        // init listeners
        userphoto.setOnClickListener(currentAccountListener);
        usercover.setOnClickListener(currentAccountListener);

        //get density
        density = this.getResources().getDisplayMetrics().density;

        // get primary color
        Resources.Theme theme = this.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;
        // set darker status bar if device is kitkat
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(primaryColor)));

        // INIT ACTION BAR
        this.setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        init(savedInstanceState);

        if(sectionList.size() == 0) {
            throw new RuntimeException("You must add at least one Section to top list.");
        }

        // Si preleva il titolo dell'activity
        title = sectionList.get(indexFragment).getTitle();

        // si collega il DrawerLayout al codice e gli si setta l'ombra all'apertura
        layout = (DrawerLayout) this.findViewById(R.id.drawer_layout);

        pulsante = new ActionBarDrawerToggle(this,layout,toolbar,R.string.nothing,R.string.nothing) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // termina il comando
            }

            public void onDrawerOpened(View drawerView) {
                //actionBar.setTitle(getCurrentTitle(-1));
                invalidateOptionsMenu(); // termina il comando
            }

        };

        layout.setDrawerListener(pulsante);

        // init account views
        if(accountManager.size() > 0) {
            currentAccount = accountManager.get(0);
            notifyAccountDataChanged();
        }

        // init section
        GSection section = sectionList.get(0);
        currentSection = section;
        section.select();
        setFragment((Fragment) section.getTargetFragment(),section.getTitle(),null);
    }



    // Gestione dei Menu -----------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return super.onCreateOptionsMenu(menu);
    }

    /* Chiamata dopo l'invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Se dal drawer si seleziona un oggetto
        if (pulsante.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        pulsante.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {// al cambio di orientamento dello schermo
        super.onConfigurationChanged(newConfig);

        // Passa tutte le configurazioni al drawer
        pulsante.onConfigurationChanged(newConfig);

    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        this.getSupportActionBar().setTitle(title);
    }

    private void setFragment(Fragment fragment,String title,Fragment oldFragment) {
        // si setta il titolo
        setTitle(title);

        // change for default Fragment / support Fragment
        if(fragment instanceof android.app.Fragment) {
            if(oldFragment instanceof android.support.v4.app.Fragment)
                throw new RuntimeException("You should use only one type of Fragment");


            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if(oldFragment != null && fragment != oldFragment)
                ft.remove((android.app.Fragment) oldFragment);

            ft.replace(R.id.frame_container, (android.app.Fragment) fragment).commit();
        }
        else if(fragment instanceof android.support.v4.app.Fragment) {
            if(oldFragment instanceof android.app.Fragment)
                throw new RuntimeException("You should use only one type of Fragment");

            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if(oldFragment != null && oldFragment != fragment)
                ft.remove((android.support.v4.app.Fragment) oldFragment);

            ft.replace(R.id.frame_container, (android.support.v4.app.Fragment) fragment).commit();
        }
        else
            throw new RuntimeException("Fragment must be android.app.Fragment or android.support.v4.app.Fragment");

        // si chiude il drawer
        layout.closeDrawer(drawer);
    }

    /*
    private void switchAccounts( GAccount newAccount ) {
        // new account will be currentAccount

        // animate
        //zoomUserImage(newAccount);

        // switch numbers
        currentAccount.setAccountNumber(newAccount.getAccountNumber());
        newAccount.setAccountNumber(GAccount.FIRST_ACCOUNT);

        // change pointer to newAccount
        currentAccount = newAccount;

        // refresh views
        notifyAccountDataChanged();


    }*/

    private void setUserEmail(String email) {
        this.usermail.setText(email);
    }

    private void setUsername(String username) {
        this.username.setText(username);
    }

    private void setFirstAccountPhoto(Bitmap photo) {
        userphoto.setImageBitmap(photo);
    }

    private void setDrawerBackground(Bitmap background) {
        usercover.setImageBitmap(background);
    }

    protected int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onClick(GSection section) {


        if(section.getTarget() == GSection.TARGET_FRAGMENT)
        {
            setFragment((Fragment)section.getTargetFragment(),section.getTitle(),(Fragment)currentSection.getTargetFragment());

            // setting toolbar color if is setted
            if(section.hasSectionColor()) {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
                    this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(section.getSectionColor())));
                else
                    this.statusBar.setImageDrawable(new ColorDrawable(section.getSectionColor()));
                this.getToolbar().setBackgroundColor(section.getSectionColor());
            }
            else {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
                    this.statusBar.setImageDrawable(new ColorDrawable(darkenColor(primaryColor)));
                else
                    this.statusBar.setImageDrawable(new ColorDrawable(primaryColor));
                this.getToolbar().setBackgroundColor(primaryColor);
            }
        }
        else {
            this.startActivity(section.getTargetIntent());
        }

        int position = section.getPosition();

        for(GSection mySection : sectionList) {
            if(position != mySection.getPosition())
                mySection.unSelect();
        }
        for(GSection mySection : bottomSectionList) {
            if(position != mySection.getPosition())
                mySection.unSelect();
        }

        currentSection = section;

    }

    public void setAccountListener(GAccountListener listener) {
        this.accountListener = listener;
    }

    // Method used for customize layout

    public void addSection(GSection section) {
        section.setPosition(sectionList.size());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(48 * density));
        sectionList.add(section);
        sections.addView(section.getView(),params);
    }

    public void addBottomSection(GSection section) {
        section.setPosition(BOTTOM_SECTION_START + bottomSectionList.size());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)(48 * density));
        bottomSectionList.add(section);
        bottomSections.addView(section.getView(),params);
    }

    public void addDivisor() {
        View view = new View(this);
        view.setBackgroundColor(Color.parseColor("#e0e0e0"));
        // height 1 px
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        params.setMargins(0,(int) (8 * density), 0 , (int) (8 * density));

        sections.addView(view, params);
    }

    public void addAccount(GAccount account) {
        if (accountManager.size() == 1)
            throw new RuntimeException("Currently are supported only one account");

        account.setAccountNumber(accountManager.size());
        accountManager.add(account);
    }


    /**
     * Reload Application data from Account Information
     */
    public void notifyAccountDataChanged() {
        switch(accountManager.size()) {
            case 1:
                this.setFirstAccountPhoto(currentAccount.getCircularPhoto());
                this.setDrawerBackground(currentAccount.getBackground());
                this.setUsername(currentAccount.getTitle());
                this.setUserEmail(currentAccount.getSubTitle());
            default:
        }
    }

    // create sections

    public GSection newSection(String title, Drawable icon, Fragment target) {
        GSection section = new GSection<Fragment>(this,true, GSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public GSection newSection(String title, Drawable icon, Intent target) {
        GSection section = new GSection<Fragment>(this,true, GSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public GSection newSection(String title, Bitmap icon,Fragment target) {
        GSection section = new GSection<Fragment>(this,true, GSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public GSection newSection(String title, Bitmap icon,Intent target) {
        GSection section = new GSection<Fragment>(this,true, GSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setIcon(icon);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public GSection newSection(String title,Fragment target) {
        GSection section = new GSection<Fragment>(this,false, GSection.TARGET_FRAGMENT);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    public GSection newSection(String title,Intent target) {
        GSection section = new GSection<Fragment>(this,false, GSection.TARGET_ACTIVITY);
        section.setOnClickListener(this);
        section.setTitle(title);
        section.setTarget(target);

        return section;
    }

    // abstract methods

    public abstract void init(Bundle savedInstanceState);

    // get methods

    public Toolbar getToolbar() {
        return toolbar;
    }

    public GSection getCurrentSection() {
        return currentSection;
    }

    public GAccount getCurrentAccount() {
        return currentAccount;
    }

    /*
    public GAccount getAccountAtCurrentPosition(int position) {

        if (position < 0 || position >= accountManager.size())
            throw  new RuntimeException("Account Index Overflow");

        return findAccountNumber(position);
    }*/
}