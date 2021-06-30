package laas.rcayre.radiosploit;

import android.os.Bundle;

import com.example.radiosploit.R;

import laas.rcayre.radiosploit.dissectors.Dissector;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    /* This is the main activity. It instantiates the HCI interface and setup the views. */
    private HciInterface hciInterface = new HciInterface();

    public HciInterface getHciInterface() {
        /* Getter allowing to easily get HCI Interface instance */
        return hciInterface;
    }

    private void showPatchFragment() {
        /* This method shows a pop up indicating that the patches have not been found */
        FragmentManager fm = getSupportFragmentManager();
        PatchFragment patchFragment = PatchFragment.newInstance("Patches not found");
        patchFragment.show(fm, "PatchFragment");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We configure the views / UI
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_zigbee, R.id.navigation_mosart, R.id.navigation_esb)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // We runs the HCI Interface thread (to extract HCI event)
        Thread hciInterfaceThread = new Thread(hciInterface);
        hciInterfaceThread.start();

        /*
        Here, we have to check that our patches are installed on the controller.
        We check if the string "RadioSploit" (in hex: 526164696F53706C6F6974) is present at address 0x2106cc in controller's RAM.
        If it is not, the patches are not installed, and we show a dialog to inform the user.
        */
        byte[] patchString = hciInterface.readRam(0x2106cc,11);
        if (!Dissector.bytesToHex(patchString).equals("526164696F53706C6F6974")) {
            this.showPatchFragment();
        }


    }

}