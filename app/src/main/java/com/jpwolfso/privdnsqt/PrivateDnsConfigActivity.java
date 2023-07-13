package com.jpwolfso.privdnsqt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

public class PrivateDnsConfigActivity extends Activity {

    private SharedPreferences toggleStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_dns_config);

        toggleStates = getSharedPreferences("togglestates", Context.MODE_PRIVATE);

        final CheckBox checkOff = findViewById(R.id.check_off);
        final CheckBox checkAuto = findViewById(R.id.check_auto);
        final CheckBox checkOn = findViewById(R.id.check_on);

        final EditText textHostname = findViewById(R.id.text_hostname);

        final Button okButton = findViewById(R.id.button_ok);

        if ((!hasPermission()) || toggleStates.getBoolean("first_run", true)) {
            HelpMenu();
            toggleStates.edit().putBoolean("first_run", false).apply();
        }

        if (toggleStates.getBoolean("toggle_off", true)) {
            checkOff.setChecked(true);
        }

        if (toggleStates.getBoolean("toggle_auto", true)) {
            checkAuto.setChecked(true);
        }

        if (toggleStates.getBoolean("toggle_on", true)) {
            checkOn.setChecked(true);
            textHostname.setEnabled(true);
        } else {
            textHostname.setEnabled(false);
        }

        if (toggleStates.getBoolean("hide_icon", false)) {
            checkHideIcon.setChecked(true);
        }

        String dnsProvider = Settings.Global.getString(getContentResolver(), "private_dns_specifier");
        if (dnsProvider != null) {
            textHostname.setText(dnsProvider);
        }

        checkOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleStates.edit().putBoolean("toggle_off", isChecked).apply();
            }
        });

        checkAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleStates.edit().putBoolean("toggle_auto", isChecked).apply();
            }
        });

        checkOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleStates.edit().putBoolean("toggle_on", isChecked).apply();
                textHostname.setEnabled(isChecked);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission()) {
                    if (checkOn.isChecked()) {
                        if (textHostname.getText().toString().isEmpty()) {
                            Toast.makeText(PrivateDnsConfigActivity.this, R.string.toast_no_dns, Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Settings.Global.putString(getContentResolver(), "private_dns_specifier", textHostname.getText().toString());
                        }
                    }

                    toggleAppIcon(checkHideIcon.isChecked());

                    toggleStates.edit().apply();
                    Toast.makeText(PrivateDnsConfigActivity.this, R.string.toast_changes_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PrivateDnsConfigActivity.this, getString(R.string.toast_no_permission), Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        ImageButton kebabMenuButton = findViewById(R.id.button_kebab_menu);
        kebabMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(PrivateDnsConfigActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_kebab, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_hide_app_icon) {
                            // Handle hide app icon option here
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }   
        });
        );
    }

    public boolean hasPermission() {
        return checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_DENIED;
    }

    public void toggleAppIcon(boolean hideIcon) {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, PrivateDnsConfigActivity.class);
        int newState = hideIcon ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        packageManager.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_hide_app_icon) {
            CheckBox hideIconCheckbox = item.getActionView().findViewById(R.id.checkbox_hide_app_icon);
            boolean hideIcon = hideIconCheckbox.isChecked();
            toggleAppIcon(hideIcon);
            Toast.makeText(this, "App icon hidden: " + hideIcon, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_appinfo) {
            // Handle app info action
        } else if (id == R.id.action_fdroid) {
            // Handle view on F-Droid action
        } else if (id == R.id.action_help) {
            // Handle help action
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void HelpMenu() {
        LayoutInflater layoutInflater = LayoutInflater.from(PrivateDnsConfigActivity.this);
        View helpView = layoutInflater.inflate(R.layout.dialog_help, null);

        VideoView videoView = helpView.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.terminal));
        videoView.start();

        AlertDialog helpDialog = new AlertDialog
                .Builder(PrivateDnsConfigActivity.this)
                .setMessage(R.string.message_help)
                .setPositiveButton(android.R.string.ok, null)
                .setView(helpView)
                .create();
        helpDialog.show();
    }
}
