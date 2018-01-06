package com.doffs.skorsten;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Range;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import doff.chimney.BatteryTest;
import doff.chimney.Chimney;
import doff.chimney.ChimneyManager;
import doff.chimney.HouseProperty;
import doff.chimney.ManualControl;
import doff.chimney.PressureTest;
import doff.chimney.Sensors;
import doff.file.FileManager;
import doff.file.PdfRender;
import doff.file.Settings;
import doff.gps.GPS;
import doff.bt.Bluetooth;
import doff.email.GMailSend;
import doff.file.Pdf;

/**
 * @todo Start av Skorsten då ej GPS är på. Får upp menydialogen hela tiden. Lösning återskapa LocationTracker-objektet.
 */

public class MainActivity extends AppCompatActivity implements Bluetooth.CommunicationCallback , FragmentFirst.OnFragmentInteractionListener {

    public static MainActivity self = null;

    private static int PageMain = 0;
    private static int PageLeakageRectangle = 1;
    private static int PageLeakageCircle = 2;
    private static int PageLeakageMeasurement = 3;
    private static int PageSettings = 4;
    private static int PageInfo = 5;
    private static int PageBattery = 6;
    private static int PagePressure = 7;
    private static int PageManual = 8;
    private static int PageHouseProperty = 9;
    private static int PageExtra = 10;
    private static int PageGPS = 11;
    private static int PageBluetoothPaired = 12;
    private static int PageBluetoothCom = 13;
    private static int PagePdf = 14;
    private static int PageEmail = 15;
    private static int PageFiles = 16;

    private ChimneyManager chimneyManager = new ChimneyManager(this);
    private ChimneyThread chimneyThread = null;

    private final static int MESSAGE_UPDATE_EDIT_TEXT_RECEIVE = 1;
    private final static int MESSAGE_FRAGMENT = 2;

    private Vector<Fragment> vectorFragment = new Vector();

    private FragmentBluetoothSerialCommunication fragmentFBSC = null;
    private final Handler handlerFBSC = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(MainActivity.self.fragmentFBSC != null && msg.what==MainActivity.MESSAGE_UPDATE_EDIT_TEXT_RECEIVE) {
                String old = MainActivity.self.fragmentFBSC.editTextRecive.getText().toString();
                String str = (String) msg.obj + "\n" + old;
                MainActivity.self.fragmentFBSC.editTextRecive.setText(str, TextView.BufferType.EDITABLE);
            }
            super.handleMessage(msg);
        }
    };
    private final Handler handlerBluetoothSendError = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Toast toast = Toast.makeText(MainActivity.self, "Blåtandsproblem vid sändning", Toast.LENGTH_SHORT);
            toast.show();
            super.handleMessage(msg);
        }
    };
    private FragmentBattery fragmentBattery = null;
    private final Handler handlerBatteryTest = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(MainActivity.self.fragmentBattery != null ) {
                //String old = MainActivity.self.fragmentBattery.textViewResult.getText().toString();
                SpannableStringBuilder ssb;
                String str;
                str = MainActivity.self.chimneyManager.sensors.battery + "("+MainActivity.self.chimneyManager.sensors.batteryCounter  + ")";
                if ( MainActivity.self.chimneyManager.sensors.battery == 0)
                    ssb = MainActivity.self.chimneyManager.batteryTest.BuildTextBatteryNotOK();
                else
                    ssb = MainActivity.self.chimneyManager.batteryTest.BuildTextBatteryOK();
                MainActivity.self.fragmentBattery.textViewResult.setText(ssb);
            }
            super.handleMessage(msg);
        }
    };
    private FragmentManual fragmentManual = null;
    private final Handler handlerManual = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(MainActivity.self.fragmentManual != null ) {
                Sensors sensors = MainActivity.self.chimneyManager.sensors;
                ManualControl mc = MainActivity.self.chimneyManager.manualControl;
                SpannableStringBuilder ssb;
                ssb = mc.BuildTextPressure(sensors.pressure);
                MainActivity.self.fragmentManual.textViewPressure.setText(ssb);
                ssb = mc.BuildTextFlow(sensors.flow);
                MainActivity.self.fragmentManual.textViewFlow.setText(ssb);
            }
            super.handleMessage(msg);
        }
    };
    //@todo Lägg till handler för pressureTest

    private FragmentPDF fragmentPDF = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private final Handler handlerFragments = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MainActivity.MESSAGE_FRAGMENT) {
                setFragmentTitle();
            }
            super.handleMessage(msg);
        }
    };
    public void setFragmentTitle() {
        String msg = "";
        int curFragNo = this.mViewPager.getCurrentItem();
        Fragment fragment = vectorFragment.get(curFragNo);
        String title = "";
        if (fragment instanceof IChimney) {
            title = ((IChimney) fragment).title();
            ((IChimney) fragment).init();
        }
        getSupportActionBar().setTitle(title);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private FloatingActionButton fabCompass = null;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chimneyManager.file2settings();
        chimneyManager.initAfterReadingSettings();

        for (int i=0; i<30; i++) {
            vectorFragment.add(null);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivity.self = this; //20171104

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        fabCompass = (FloatingActionButton) findViewById(R.id.fab_compass);
        fabCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                mViewPager.setCurrentItem(0);
            }
        });

        chimneyThread = new ChimneyThread();
        chimneyThread.start();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if ( id == R.id.gps_action ) {
            mViewPager.setCurrentItem(0);
            return true;
        } else if ( id == R.id.bt_action ) {
            mViewPager.setCurrentItem(1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override public void onResume() {
        super.onResume();
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setIcon(R.mipmap.ic_launcher);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do whatever you need for the hardware 'back' button
            if ( mViewPager.getCurrentItem() != 0 )
            {
                mViewPager.setCurrentItem(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override public void onConnect(BluetoothDevice device) {

    }
    @Override public void onDisconnect(BluetoothDevice device, String message) {

    }
    @Override public void onMessage(String message) { //Anropas av en Bluetooth-tråd
        Log.d("doff-bt","onMessage: "+message);
        if (handlerFBSC != null) {
            Message msg = handlerFBSC.obtainMessage();
            msg.what = MainActivity.MESSAGE_UPDATE_EDIT_TEXT_RECEIVE;
            msg.obj = message;
            chimneyManager.protocol.decode(message);
            handlerFBSC.sendMessage(msg); //Skickas till UI-tråden
        }
    }
    @Override public void onError(String message) {

    }
    @Override public void onConnectError(BluetoothDevice device, String message) {

    }

    @Override public void onFragmentInteraction(Uri uri) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public void setPrimaryItem (ViewGroup container, int position, Object object) {
            Log.d("doff-main","SectionsPagerAdapter.setPrimaryItem "+position);
            Message msg = handlerFragments.obtainMessage();
            msg.what = MainActivity.MESSAGE_FRAGMENT;
            msg.obj = "title";
            handlerFragments.sendMessage(msg); //Skickas till UI-tråden
        }
        @Override public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Log.d("doff-main","SectionsPagerAdapter.getItem "+position);

            Fragment fragment = null;
            int p=0;
            if (position == p++) {
                fragment = FragmentMain.newInstance(position + 1); //0
            }
            else if (position == p++) {
                fragment = FragmentLeaksRectangle.newInstance(position + 1); // 1
            }
            else if (position == p++)
                fragment = FragmentLeaksCircle.newInstance(position + 1); // 2
            else if (position == p++)
                fragment = FragmentLeaksMeasurement.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentSettings.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentInfo.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentBattery.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentPressure.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentManual.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentHouseProperty.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentExtra.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentGPS.newInstance(position + 1);
            else if (position == p++)
                fragment = FragmentBluetoothPairedDevices.newInstance(position + 1, MainActivity.self.chimneyManager.bluetooth);
            else if (position == p++)
                fragment = FragmentBluetoothSerialCommunication.newInstance(position + 1,MainActivity.self.chimneyManager.bluetooth);
            else if (position == p++)
                fragment = FragmentPDF.newInstance(position + 1);
            else  if (position == p++)
                fragment = FragmentEmail.newInstance(position + 1);
            else  if (position == p++)
                fragment = FragmentFiles.newInstance(position + 1);
            else
                return null;

            vectorFragment.set(position, fragment);
            return fragment;
        }
        @Override public int getCount() {
            // Show 3 total pages.
            return 17;
        }
    }
    public void restartApp() {
        Intent mStartActivity = new Intent(this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    interface IChimney {
        String title();
        void init();
    }
    public static class FragmentMain extends Fragment implements IChimney {

        public String title() { return getString(R.string.main_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button button11 = null;
        private Button button12 = null;
        private Button button21 = null;
        private Button button22 = null;
        private Button button31 = null;
        private Button button32 = null;
        private Button button41 = null;
        private Button button42 = null;

        public FragmentMain() {}

        public static FragmentMain newInstance(int sectionNumber) {
            Log.d("doff-pdf","New fragment.");
            final FragmentMain fragment = new FragmentMain();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main3, container, false);

            button11 = (Button) rootView.findViewById(R.id.main_button11);
            if (button11 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button11");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button11.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageLeakageRectangle);
                        //Toast toast = Toast.makeText(MainActivity.self, "button11", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button12 = (Button) rootView.findViewById(R.id.main_button12);
            if (button12 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button12");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button12.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageLeakageCircle);
                        //Toast toast = Toast.makeText(MainActivity.self, "button12", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button21 = (Button) rootView.findViewById(R.id.main_button21);
            if (button21 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button21");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button21.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PagePressure);
                        //Toast toast = Toast.makeText(MainActivity.self, "button21", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button22 = (Button) rootView.findViewById(R.id.main_button22);
            if (button22 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button22");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button22.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageManual);
                        //Toast toast = Toast.makeText(MainActivity.self, "button22", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button31 = (Button) rootView.findViewById(R.id.main_button31);
            if (button31 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button31");

                button31.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageBattery);
//                        Toast toast = Toast.makeText(MainActivity.self, "button31", Toast.LENGTH_SHORT);
//                        toast.show();
                    }
                });
            }

            button32 = (Button) rootView.findViewById(R.id.main_button32);
            if (button32 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button32");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button32.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageHouseProperty);
                        //Toast toast = Toast.makeText(MainActivity.self, "button32", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button41 = (Button) rootView.findViewById(R.id.main_button41);
            if (button41 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button31");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button41.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageInfo);
                        //Toast toast = Toast.makeText(MainActivity.self, "button41", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button42 = (Button) rootView.findViewById(R.id.main_button42);
            if (button42 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button42");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button42.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.chimneyManager.settings2file();
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageExtra);
                        //Toast toast = Toast.makeText(MainActivity.self, "button42", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }
            return rootView;
        }
    }
    public static class FragmentLeaksRectangle extends Fragment implements IChimney {
        public String title() { return getString(R.string.leaks_rect_title); }
        private static final String ARG_SECTION_NUMBER = "section_number";
        private TextView textViewPressure = null;
        private SeekBar  seekBarPressure  = null;
        private TextView textViewSideA = null;
        private SeekBar  seekBarSideA  = null;
        private TextView textViewSideB = null;
        private SeekBar  seekBarSideB  = null;
        private TextView textViewLength = null;
        private SeekBar  seekBarLength  = null;
        private Button buttonToLeaksMeasure = null;

        public void init()  {
            if (textViewPressure != null) {
                int v = MainActivity.self.chimneyManager.chimney.pressure;
                Range r = MainActivity.self.chimneyManager.chimney.rangePressure;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarPressure.setProgress(sbar);
                textViewPressure.setText(getString(R.string.leaks_rect_pressure)+ v + " Pa");
            }

            if (textViewSideA != null) {
                int v = MainActivity.self.chimneyManager.chimney.sideA;
                Range r = MainActivity.self.chimneyManager.chimney.rangeSide;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarSideA.setProgress(sbar);
                textViewSideA.setText(getString(R.string.leaks_rect_side_a)+ v + " mmv");
            }

            if (textViewSideB != null) {
                int v = MainActivity.self.chimneyManager.chimney.sideB;
                Range r = MainActivity.self.chimneyManager.chimney.rangeSide;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarSideB.setProgress(sbar);
                textViewSideB.setText(getString(R.string.leaks_rect_side_b)+ v + " mmv");
            }

            if (textViewLength != null) {
                double v = MainActivity.self.chimneyManager.chimney.lengthR;
                Range r = MainActivity.self.chimneyManager.chimney.rangeLengthR;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarLength.setProgress(sbar);
                String value_str = String.format("%.1f", v);
                textViewLength.setText(getString(R.string.leaks_circle_length)+ value_str + " m");
            }
        }
        public FragmentLeaksRectangle() {}

        public static FragmentLeaksRectangle newInstance(int sectionNumber) {
            FragmentLeaksRectangle fragment = new FragmentLeaksRectangle();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_leaks_rect, container, false);

            textViewPressure = (TextView) rootView.findViewById(R.id.textViewPressure);
            seekBarPressure = (SeekBar) rootView.findViewById(R.id.seekBarPressure);
            seekBarPressure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    int value = MainActivity.self.chimneyManager.chimney.setPressure(progress);
                    textViewPressure.setText(getString(R.string.leaks_rect_pressure)+ value + "Pa");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            textViewSideA = (TextView) rootView.findViewById(R.id.textViewSideA);
            seekBarSideA = (SeekBar) rootView.findViewById(R.id.seekBarSideA);
            seekBarSideA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    int value = MainActivity.self.chimneyManager.chimney.setSideA(progress);
                    textViewSideA.setText(getString(R.string.leaks_rect_side_a)+ value + " mmv");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            textViewSideB = (TextView) rootView.findViewById(R.id.textViewSideB);
            seekBarSideB = (SeekBar) rootView.findViewById(R.id.seekBarSideB);
            seekBarSideB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    int value = MainActivity.self.chimneyManager.chimney.setSideB(progress);
                    textViewSideB.setText(getString(R.string.leaks_rect_side_b)+ value + " mmv");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            textViewLength = (TextView) rootView.findViewById(R.id.textViewLengthR);
            seekBarLength = (SeekBar) rootView.findViewById(R.id.seekBarLengthR);
            seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    double value = MainActivity.self.chimneyManager.chimney.setLengthR(progress);
                    String value_str = String.format("%.1f", value);
                    textViewLength.setText(getString(R.string.leaks_circle_length)+ value_str + " m");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            buttonToLeaksMeasure = (Button) rootView.findViewById(R.id.buttonLeaksStartMeasure);

            buttonToLeaksMeasure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageLeakageMeasurement);
                        //Toast toast = Toast.makeText(MainActivity.self, "button11", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });


            init();
            return rootView;
        }
    }
    public static class FragmentLeaksCircle extends Fragment implements IChimney {
        public String title() { return getString(R.string.leaks_circle_title); }
        private static final String ARG_SECTION_NUMBER = "section_number";
        private TextView textViewPressure = null;
        private SeekBar  seekBarPressure  = null;
        private TextView textViewRadius = null;
        private SeekBar  seekBarRadius  = null;
        private TextView textViewLength = null;
        private SeekBar  seekBarLength  = null;
        private Button buttonToLeaksMeasure = null;

        public void init()  {
            if (textViewPressure != null) {
                int v = MainActivity.self.chimneyManager.chimney.pressure;
                Range r = MainActivity.self.chimneyManager.chimney.rangePressure;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarPressure.setProgress(sbar);
                textViewPressure.setText(getString(R.string.leaks_rect_pressure)+ v + " Pa");
            }

            if (textViewRadius != null) {
                int v = MainActivity.self.chimneyManager.chimney.radius;
                Range r = MainActivity.self.chimneyManager.chimney.rangeSide;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarRadius.setProgress(sbar);
                textViewRadius.setText(getString(R.string.leaks_circle_radius)+ v + " mmv");
            }

            if (textViewLength != null) {
                double v = MainActivity.self.chimneyManager.chimney.lengthC;
                Range r = MainActivity.self.chimneyManager.chimney.rangeLengthC;
                int sbar = MainActivity.self.chimneyManager.chimney.value2seekBar(r,v);
                seekBarLength.setProgress(sbar);
                String value_str = String.format("%.1f", v);
                textViewLength.setText(getString(R.string.leaks_circle_length)+ value_str + " m");
            }
        }
        public FragmentLeaksCircle() {}

        public static FragmentLeaksCircle newInstance(int sectionNumber) {
            FragmentLeaksCircle fragment = new FragmentLeaksCircle();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_leaks_circle, container, false);

            textViewPressure = (TextView) rootView.findViewById(R.id.textViewPressure);
            seekBarPressure = (SeekBar) rootView.findViewById(R.id.seekBarPressure);
            seekBarPressure.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    int value = MainActivity.self.chimneyManager.chimney.setPressure(progress);
                    textViewPressure.setText(getString(R.string.leaks_rect_pressure)+ value + "Pa");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            textViewRadius = (TextView) rootView.findViewById(R.id.textViewRadius);
            seekBarRadius = (SeekBar) rootView.findViewById(R.id.seekBarRadius);
            seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    int value = MainActivity.self.chimneyManager.chimney.setRadius(progress);
                    textViewRadius.setText(getString(R.string.leaks_circle_radius)+ value + " mmv");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            textViewLength = (TextView) rootView.findViewById(R.id.textViewLengthC);
            seekBarLength = (SeekBar) rootView.findViewById(R.id.seekBarLengthC);
            seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    double value = MainActivity.self.chimneyManager.chimney.setLengthC(progress);
                    String value_str = String.format("%.1f", value);
                    textViewLength.setText(getString(R.string.leaks_circle_length)+ value_str + " m");
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            buttonToLeaksMeasure = (Button) rootView.findViewById(R.id.buttonLeaksStartMeasureC);

            buttonToLeaksMeasure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageLeakageMeasurement);
                    //Toast toast = Toast.makeText(MainActivity.self, "button11", Toast.LENGTH_SHORT);
                    //toast.show();
                }
            });

            init();

            return rootView;
        }
    }
    public static class FragmentLeaksMeasurement extends Fragment implements IChimney {
        /**
         * @todo Tryckknappen skall representera 3 tillstånd START, Mätning pågår (inaktiv)
         * @todo Tryckknapp som framträder då en mätning ahr gjorts Spara
         */
        public String title() { return getString(R.string.leaks_measure_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        private ImageButton toggleButtonRect = null;
        private ImageButton toggleButtonCircle = null;
        private ImageButton toggleButtonRectCircle = null;
        private Button buttonStart = null;
        private Button buttonSave = null;
        private Button buttonEmail = null;
        private TextView textViewMeasurementOn=null;
        private CountDownTimer countDownTimer = null;

        public FragmentLeaksMeasurement() {}

        public static FragmentLeaksMeasurement newInstance(int sectionNumber) {
            FragmentLeaksMeasurement fragment = new FragmentLeaksMeasurement();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_leaks_measurement, container, false);

            toggleButtonRect = (ImageButton) rootView.findViewById(R.id.btn_measure_rect);
            toggleButtonRectCircle = (ImageButton) rootView.findViewById(R.id.btn_measure_rect_circle);
            toggleButtonCircle = (ImageButton) rootView.findViewById(R.id.btn_measure_circle);
            buttonStart = (Button) rootView.findViewById(R.id.buttonLeaksStartMeasure);
            buttonSave = (Button) rootView.findViewById(R.id.btn_measure_save);
            buttonEmail = (Button) rootView.findViewById(R.id.btn_measure_email);
            textViewMeasurementOn = (TextView) rootView.findViewById(R.id.textViewLeakageMeasurementOn);

            if ( buttonSave != null ) {
                buttonSave.setVisibility(View.INVISIBLE);
                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ( MainActivity.self.chimneyManager.chimney.SaveMeasurementResult() ) {
                            String fileName = MainActivity.self.chimneyManager.settings.HousePropertyId+".pdf";
                            Pdf.lmd2pdf(MainActivity.self, fileName, MainActivity.self.chimneyManager.chimney.lmd);
                            Toast toast = Toast.makeText(MainActivity.self, "Save measurement result", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(MainActivity.self, "Already saved", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }

            if ( buttonEmail != null ) {
                buttonEmail.setVisibility(View.INVISIBLE);
                buttonEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.self.chimneyManager.chimney.EmailMeasurementResult();
                        Toast toast = Toast.makeText(MainActivity.self, "Email measurement result", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            if ( buttonStart != null ) {
                buttonStart.setOnClickListener(new View.OnClickListener() {
                    private int countTime = 0;
                    private long endTime = 30000;
                    private long deltaTime = 1000;
                    @Override
                    public void onClick(View view) {
                        ChimneyManager cm = MainActivity.self.chimneyManager;

                        if (countDownTimer == null) {
                            buttonSave.setVisibility(View.INVISIBLE);
                            buttonEmail.setVisibility(View.INVISIBLE);
                            buttonStart.setText(getText(R.string.leaks_measure_measure_underway));
                            buttonStart.setEnabled(false);
                            buttonStart.setBackgroundColor(getResources().getColor(R.color.greySlate1,null));
                            cm.chimney.leakLmdInit();
                            int pressure = cm.chimney.pressure;
                            cm.protocol.txLeakageMeasure(pressure);
                            countDownTimer = new CountDownTimer(endTime, deltaTime) {
                                @Override
                                public void onTick(long l) {
                                    countTime += 1;
                                    textViewMeasurementOn.setText(MainActivity.self.chimneyManager.chimney.BuildLeakageMeasureOn(false));
                                    MainActivity.self.chimneyManager.protocol.txGetSensorSignals();
                                    //Toast toast = Toast.makeText(MainActivity.self, ""+countTime+" , "+(l/1000), Toast.LENGTH_SHORT);
                                    //toast.show();
                                }

                                @Override
                                public void onFinish() {
                                    buttonSave.setVisibility(View.VISIBLE);
                                    buttonEmail.setVisibility(View.VISIBLE);
                                    countDownTimer.cancel();
                                    countDownTimer = null;
                                    countTime=0;
                                    buttonStart.setText(getText(R.string.leaks_measure_start));
                                    buttonStart.setEnabled(true);
                                    buttonStart.setBackgroundColor(getResources().getColor(R.color.blueNavy,null));
                                    textViewMeasurementOn.setText(MainActivity.self.chimneyManager.chimney.BuildLeakageMeasureOn(true));
                                    Toast toast = Toast.makeText(MainActivity.self, "Läckagemätning klar", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            };
                            countDownTimer.start();
                            Toast toast = Toast.makeText(MainActivity.self, "Läckagmätning startar", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            countDownTimer.cancel();
                            countDownTimer = null;
                            countTime=0;
                            Toast toast = Toast.makeText(MainActivity.self, "Läckagmätning avslutas", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }

            if( toggleButtonRect != null )
            {
                toggleButtonRect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.self.chimneyManager.chimney.chimneyChannel= Chimney.ChimneyChannel.Rectangular;
                        toggleButtonRect.setBackgroundColor(getResources().getColor(R.color.blueRoyal));
                        toggleButtonCircle.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        toggleButtonRectCircle.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        String channel = getString(R.string.leaks_measure_rchannel);
                        Toast toast = Toast.makeText(MainActivity.self, channel, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            if( toggleButtonRectCircle != null )
            {
                toggleButtonRectCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.self.chimneyManager.chimney.chimneyChannel= Chimney.ChimneyChannel.BothCircularAndRectanguar;
                        toggleButtonRect.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        toggleButtonCircle.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        toggleButtonRectCircle.setBackgroundColor(getResources().getColor(R.color.blueRoyal));
                        String channel = getString(R.string.leaks_measure_rcchannel);
                        Toast toast = Toast.makeText(MainActivity.self, channel, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            if( toggleButtonCircle != null )
            {
                toggleButtonCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.self.chimneyManager.chimney.chimneyChannel= Chimney.ChimneyChannel.Circular;
                        toggleButtonRect.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        toggleButtonCircle.setBackgroundColor(getResources().getColor(R.color.blueRoyal));
                        toggleButtonRectCircle.setBackgroundColor(getResources().getColor(R.color.greySlate4));
                        String channel = getString(R.string.leaks_measure_cchannel);
                        Toast toast = Toast.makeText(MainActivity.self, channel, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            Chimney chimney = MainActivity.self.chimneyManager.chimney;
            switch(chimney.chimneyChannel)
            {
                case Circular: toggleButtonCircle.setBackgroundColor(getResources().getColor(R.color.blueRoyal)); break;
                case Rectangular: toggleButtonRect.setBackgroundColor(getResources().getColor(R.color.blueRoyal)); break;
                case BothCircularAndRectanguar: toggleButtonRectCircle.setBackgroundColor(getResources().getColor(R.color.blueRoyal)); break;
            }

            return rootView;
        }
    }
    public static class FragmentBattery extends Fragment implements IChimney {
        public String title() { return getString(R.string.battery_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        private View rootView = null;
        private Button buttonStart = null;
        private TextView textViewPreText = null;
        private TextView textViewResult = null;
        private Snackbar snackbar = null;

        public void showSnackbar(View view, String message)
        {
            // Create snackbar
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

            // Set an action on it, and a handler
            snackbar.setAction(MainActivity.self.getString(R.string.battery_button_snackbar_stop), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChimneyManager cm =  MainActivity.self.chimneyManager;
                    snackbar.dismiss();
                    if ( cm.isStateBatteryTest() ) {
                        cm.setStateIdle();
                        buttonStart.setText(R.string.battery_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal,null));
                    }
                }
            });

            snackbar.show();
        }

        public FragmentBattery() {}

        public static FragmentBattery newInstance(int sectionNumber) {
            FragmentBattery fragment = new FragmentBattery();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            MainActivity.self.fragmentBattery = this;
            rootView = inflater.inflate(R.layout.fragment_battery, container, false);
            final Activity activity = MainActivity.self;
            BatteryTest batteryTest = MainActivity.self.chimneyManager.batteryTest;

            buttonStart = (Button) rootView.findViewById(R.id.batteryTestButtonStart);
            textViewPreText = (TextView) rootView.findViewById(R.id.textViewBattteryTestPre);
            textViewPreText.setText(batteryTest.BuildText());
            textViewResult = (TextView) rootView.findViewById(R.id.textViewBatteryTestResult);

            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ( MainActivity.self.chimneyManager.isStateIdle() ) {
                        MainActivity.self.chimneyManager.setStateBatteryTest();
                        buttonStart.setText(R.string.battery_button_stop);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.greySlate1,null));
                        showSnackbar(rootView, activity.getString(R.string.battery_button_snackbar_text));
                    } else
                    {
                        MainActivity.self.chimneyManager.setStateIdle();
                        buttonStart.setText(R.string.battery_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal,null));
                        snackbar.dismiss();
                        //@todo Toast, i annat tillstånd.
                    }
                }
            });

            return rootView;
        }
    }
    public static class FragmentExtra extends Fragment implements IChimney {

        public String title() { return getString(R.string.extra_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";


        private Button button11 = null;
        private Button button12 = null;
        private Button button21 = null;
        private Button button22 = null;
        private Button button31 = null;
        private Button button32 = null;
        private Button button41 = null;
        private Button button42 = null;

        public FragmentExtra() {}

        public static FragmentExtra newInstance(int sectionNumber) {
            Log.d("doff-extra","New fragment.");
            final FragmentExtra fragment = new FragmentExtra();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_extra, container, false);

            button11 = (Button) rootView.findViewById(R.id.extra_button11);
            if (button11 != null) {
                button11.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageSettings);
                        //Toast toast = Toast.makeText(MainActivity.self, "button11", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button12 = (Button) rootView.findViewById(R.id.extra_button12);
            if (button12 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button12");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button12.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageBluetoothPaired);
                        //Toast toast = Toast.makeText(MainActivity.self, "button12", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button21 = (Button) rootView.findViewById(R.id.extra_button21);
            if (button21 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button21");
                button21.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageEmail);
                        //Toast toast = Toast.makeText(MainActivity.self, "button21", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button22 = (Button) rootView.findViewById(R.id.extra_button22);
            if (button22 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button22");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button22.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageGPS);
                        //Toast toast = Toast.makeText(MainActivity.self, "button22", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button31 = (Button) rootView.findViewById(R.id.extra_button31);
            if (button31 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button31");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button31.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PagePdf);
                        //Toast toast = Toast.makeText(MainActivity.self, "button31", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }

            button32 = (Button) rootView.findViewById(R.id.extra_button32);
            if (button32 != null) {
                Log.d("doff-main","Create button listener: FragmentMain button32");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                button32.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageFiles);
                        //Toast toast = Toast.makeText(MainActivity.self, "button32", Toast.LENGTH_SHORT);
                        //toast.show();
                    }
                });
            }
            return rootView;
        }
    }
    public static class FragmentSettings extends Fragment implements IChimney {
        public String title() { return getString(R.string.settings_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button buttonSave = null;
        private ListViewAdapterSettings listViewAdapterSettings = null;
        private ListView listView = null;

        public FragmentSettings() {}

        public static FragmentSettings newInstance(int sectionNumber) {
            FragmentSettings fragment = new FragmentSettings();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

            arrKey = MainActivity.self.chimneyManager.settings.key();
            arrTemp = MainActivity.self.chimneyManager.settings.value();
            listViewAdapterSettings = new ListViewAdapterSettings();
            listView = (ListView) rootView.findViewById(R.id.listViewSettings);
            listView.setAdapter(listViewAdapterSettings);

            buttonSave = (Button) rootView.findViewById(R.id.buttonSaveSettings);
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.self.chimneyManager.settings.view2settings(arrKey, arrTemp);
                    MainActivity.self.chimneyManager.settings2file();
                }
            });
            return rootView;
        }


        private static String[] arrKey =
                new String[]{"Text1","Text2","Text3","Text4"
                        ,"Text5","Text6","Text7","Text8","Text9","Text10"
                        ,"Text11","Text12","Text13","Text14","Text15"
                        ,"Text16","Text17","Text18","Text19","Text20"
                        ,"Text21","Text22","Text23","Text24"};
        //private static String[] arrayColumn1;
        private static String[] arrTemp =
                new String[]{"Text1","Text2","Text3","Text4"
                        ,"Text5","Text6","Text7","Text8","Text9","Text10"
                        ,"Text11","Text12","Text13","Text14","Text15"
                        ,"Text16","Text17","Text18","Text19","Text20"
                        ,"Text21","Text22","Text23","Text24"};

        private static class ListViewAdapterSettings extends BaseAdapter {

            @Override public int getCount() {
                // TODO Auto-generated method stub
                if(arrKey != null && arrKey.length != 0){
                    return arrKey.length;
                }
                return 0;
            }
            @Override public Object getItem(int position) {
                // TODO Auto-generated method stub
                return arrKey[position];
            }
            @Override public long getItemId(int position) {
                // TODO Auto-generated method stub
                return position;
            }
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                //ViewHolder holder = null;
                final ViewHolder holder;
                if (convertView == null) {

                    holder = new ViewHolder();
                    LayoutInflater inflater = MainActivity.self.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_item_textview_edittext, null);
                    holder.textView1 = (TextView) convertView.findViewById(R.id.textViewSettings);
                    holder.editText1 = (EditText) convertView.findViewById(R.id.editTextSettings);

                    convertView.setTag(holder);

                } else {

                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;

                holder.textView1.setText(arrKey[position]);
                holder.editText1.setText(arrTemp[position]);
                holder.editText1.addTextChangedListener(new TextWatcher() {
                    @Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                        // TODO Auto-generated method stub

                    }
                    @Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                        // TODO Auto-generated method stub

                    }
                    @Override public void afterTextChanged(Editable arg0) {
                        // TODO Auto-generated method stub
                        arrTemp[holder.ref] = arg0.toString();
                    }
                });

                return convertView;
            }

            private class ViewHolder {
                TextView textView1;
                EditText editText1;
                int ref;
            }


        }
    }
    public static class FragmentInfo extends Fragment implements IChimney {
        public String title() { return getString(R.string.info_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        TextView textViewInfo = null;

        public FragmentInfo() {}

        public static FragmentInfo newInstance(int sectionNumber) {
            FragmentInfo fragment = new FragmentInfo();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);

            textViewInfo = (TextView) rootView.findViewById(R.id.textViewInfo);
            textViewInfo.setText(MainActivity.self.chimneyManager.information());
            return rootView;
        }
    }
    public static class FragmentManual extends Fragment implements IChimney {
        public String title() { return getString(R.string.manual_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        View rootView;
        private Button buttonStart = null;
        private TextView textViewFanMotor = null;
        private SeekBar  seekBarFanMotor  = null;
        private TextView textViewPressure = null;
        private TextView textViewFlow = null;
        private Snackbar snackbar = null;

        public void showSnackbar(View view, String message)
        {
            // Create snackbar
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

            // Set an action on it, and a handler
            snackbar.setAction(MainActivity.self.getString(R.string.manual_button_snackbar_stop), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChimneyManager cm =  MainActivity.self.chimneyManager;
                    snackbar.dismiss();
                    if ( cm.isStateManual() ) {
                        cm.setStateIdle();
                        buttonStart.setText(R.string.manual_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal,null));
                    }
                }
            });

            snackbar.show();
        }

        public FragmentManual() {}

        public static FragmentManual newInstance(int sectionNumber) {
            FragmentManual fragment = new FragmentManual();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            MainActivity.self.fragmentManual = this;
            rootView = inflater.inflate(R.layout.fragment_manual, container, false);
            ManualControl manualControl = MainActivity.self.chimneyManager.manualControl;

            buttonStart = (Button) rootView.findViewById(R.id.buttonManualStart);
            buttonStart.setText(R.string.manual_button_start);
            buttonStart.setBackgroundColor(getResources().getColor(R.color.blueNavy, null));

            textViewFanMotor = (TextView) rootView.findViewById(R.id.textViewManulFanMotor);
            textViewFanMotor.setText(manualControl.BuildTextFanMotor());

            textViewPressure = (TextView) rootView.findViewById(R.id.textViewManualPressure);
            textViewPressure.setText(manualControl.BuildTextPressure(0.0));

            textViewFlow = (TextView) rootView.findViewById(R.id.textViewManualFlow);
            textViewFlow.setText(manualControl.BuildTextFlow(0.0));

            seekBarFanMotor = (SeekBar) rootView.findViewById(R.id.seekBarFanMotor);
            seekBarFanMotor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    double value = MainActivity.self.chimneyManager.manualControl.setFanMotor(progress);
                    textViewFanMotor.setText(MainActivity.self.chimneyManager.manualControl.BuildTextFanMotor());
                    if ( MainActivity.self.chimneyManager.isStateManual() ) {
                        MainActivity.self.chimneyManager.protocol.txSetFanMotorSignal((int) value);
                    }
                    //Toast.makeText((MainActivity.self, String.valueOf(progress),Toast.LENGTH_LONG).show();

                }
            });

            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ( MainActivity.self.chimneyManager.isStateIdle() ) {
                        MainActivity.self.chimneyManager.setStateManual();
                        buttonStart.setText(R.string.manual_button_stop);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.greySlate1,null));
                        showSnackbar(rootView, MainActivity.self.getString(R.string.manual_button_snackbar_text));
                    } else
                    {
                        MainActivity.self.chimneyManager.setStateIdle();
                        buttonStart.setText(R.string.manual_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal,null));
                        snackbar.dismiss();
                        //@todo Toast, i annat tillstånd.
                    }
                }
            });

            return rootView;
        }
    }
    public static class FragmentPressure extends Fragment implements IChimney {
        public String title() { return getString(R.string.pressure_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        private View rootView;
        private ChimneyManager chimneyManager = MainActivity.self.chimneyManager;
        private PressureTest pressureTest = MainActivity.self.chimneyManager.pressureTest;
        private Button buttonStart = null;
        private TextView textViewLength= null;
        private SeekBar  seekBarLength  = null;
        private TextView textViewPressure = null;
        private TextView textViewFanMotor = null;
        private Snackbar snackbar = null;

        public FragmentPressure() {}
        public void showSnackbar(View view, String message)  {
            // Create snackbar
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

            // Set an action on it, and a handler
            snackbar.setAction(MainActivity.self.getString(R.string.pressure_button_snackbar_stop), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChimneyManager cm =  MainActivity.self.chimneyManager;
                    snackbar.dismiss();
                    if ( cm.isPressureTest() ) {
                        cm.setStateIdle();
                        buttonStart.setText(R.string.pressure_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal,null));
                    }
                }
            });

            snackbar.show();
        }
        public static FragmentPressure newInstance(int sectionNumber) {
            FragmentPressure fragment = new FragmentPressure();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.rootView = inflater.inflate(R.layout.fragment_pressure, container, false);

            textViewLength = (TextView) rootView.findViewById(R.id.textViewPressureLength);
            textViewLength.setText(pressureTest.BuildTextLength());

            seekBarLength = (SeekBar) rootView.findViewById(R.id.seekBarPressureLength);
            seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
                @Override public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    // TODO Auto-generated method stub
                    double value = pressureTest.setLength(progress);
                    textViewLength.setText(pressureTest.BuildTextLength());
                    if ( MainActivity.self.chimneyManager.isPressureTest() ) {
                        //MainActivity.self.chimneyManager.protocol.txPessureTest((int) value);
                    }
                }
            });

            textViewPressure = (TextView) rootView.findViewById(R.id.textViewPressurePressure);
            textViewPressure.setText(pressureTest.BuildTextPressure(0.0));

            textViewFanMotor = (TextView) rootView.findViewById(R.id.textViewPressureFanMotor);
            textViewFanMotor.setText(pressureTest.BuildTextFanMotor(0));

            buttonStart = (Button) rootView.findViewById(R.id.buttonPressureStart);
            buttonStart.setText(R.string.pressure_button_start);
            buttonStart.setBackgroundColor(getResources().getColor(R.color.blueNavy, null));
            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (MainActivity.self.chimneyManager.isStateIdle()) {
                        MainActivity.self.chimneyManager.setStateManual();
                        buttonStart.setText(R.string.manual_button_stop);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.greySlate1, null));
                        chimneyManager.setStatePressureTest();
                        showSnackbar(rootView, MainActivity.self.getString(R.string.pressure_button_snackbar_text));
                    } else {
                        MainActivity.self.chimneyManager.setStateIdle();
                        buttonStart.setText(R.string.manual_button_start);
                        buttonStart.setBackgroundColor(getResources().getColor(R.color.blueRoyal, null));
                        snackbar.dismiss();
                    }
                }
            });
            return this.rootView;
        }
    }
    public static class FragmentHouseProperty extends Fragment implements IChimney {
        public String title() { return getString(R.string.house_property_title); }
        public void init() {}
        private static final String ARG_SECTION_NUMBER = "section_number";

        TextView textViewSweeper = null;
        EditText editTextSweeper = null;

        TextView textViewId = null;
        EditText editTextId = null;

        public FragmentHouseProperty() {}

        public static FragmentHouseProperty newInstance(int sectionNumber) {
            FragmentHouseProperty fragment = new FragmentHouseProperty();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_house_property, container, false);

            HouseProperty hp = MainActivity.self.chimneyManager.houseProperty;
            final Settings settings = MainActivity.self.chimneyManager.settings;

            textViewSweeper = (TextView) rootView.findViewById(R.id.textViewHousePropertySweeper);
            textViewSweeper.setText(hp.BuildTextSweeper());

            editTextSweeper = (EditText) rootView.findViewById(R.id.editTextHousePropertySweeper);
            editTextSweeper.setText(settings.Sweeper);
            editTextSweeper.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    settings.Sweeper = editable.toString();
                }
            });

            textViewId = (TextView) rootView.findViewById(R.id.textViewHousePropertyId);
            textViewId.setText(hp.BuildTextId());

            editTextId = (EditText) rootView.findViewById(R.id.editTextHousePropertyId);
            editTextId.setText(settings.HousePropertyId);
            editTextId.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    settings.HousePropertyId = editable.toString();
                }
            });

            return rootView;
        }
    }
    public static class FragmentBluetoothPairedDevices extends Fragment  implements IChimney {
        public String title() { return getString(R.string.bt_title); }
        public void init() {}

        private Bluetooth bluetooth = null;
        public static FragmentBluetoothPairedDevices newInstance(int sectionNumber, Bluetooth bluetooth) {
            FragmentBluetoothPairedDevices fragment = new FragmentBluetoothPairedDevices();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragment.bluetooth = bluetooth;
            return fragment;
        }
        private static final String ARG_SECTION_NUMBER = "section_number";

        private List<BluetoothDevice> btPairedDevicesList = null;
        private List<String> btPairedDevicesStrings = new ArrayList<>(10);

        private  Button buttonPairedDevices = null;
        private  ListView listViewPairedDevices = null;
        private  ArrayAdapter<String> itemsAdapter = null;
        private  Button buttonConnectBT = null;

        public FragmentBluetoothPairedDevices() {

        }
        private void onClickButtonPairedDevices() {

            Log.d("doff-bt","onClickButtonPairedDevices");

            btPairedDevicesList = MainActivity.self.chimneyManager.bluetooth.getPairedDevices();
            Log.d("doff-bt", "btPairedDevicesList:"+btPairedDevicesList.toString());

            itemsAdapter.clear();
            for (BluetoothDevice bd : btPairedDevicesList) {
                String deviceName = bd.getName();
                String deviceMacAddress = bd.getAddress();
                String item = String.format("%20s\n%20s", deviceName, deviceMacAddress);
                btPairedDevicesStrings.add(item);
                listViewPairedDevices.setAdapter(itemsAdapter);
            }
        }

        private long positionInListView = -1;
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bluetooth_paired_devices, container, false);

            itemsAdapter = new ArrayAdapter<String>((Context) MainActivity.self, android.R.layout.simple_list_item_1, (List<String>) btPairedDevicesStrings);
            listViewPairedDevices = (ListView) rootView.findViewById(R.id.listview_bt_paired_devices);
            listViewPairedDevices.setAdapter(itemsAdapter);
            listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Log.d("doff-bt",String.format("%-4d%-8d", position,id));
                    view.setSelected(true);
                    buttonConnectBT.setVisibility(View.VISIBLE);
                    positionInListView = id;
                    Log.d("doff-bt","onItemClick: " + id  );
                }
            });

            buttonPairedDevices = (Button) rootView.findViewById(R.id.button_bt_paired_devices);
            if (buttonPairedDevices != null) {
                Log.d("doff-bt","Create button listener.");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                buttonPairedDevices.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButtonPairedDevices();
                    }
                });
            }

            //buttonName.setVisibility(View.VISIBLE);
            buttonConnectBT = (Button) rootView.findViewById(R.id.button_bt_connect);
            if (buttonConnectBT != null) {
                Log.d("doff-bt","Create button listener.");
                buttonConnectBT.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("doff-bt","buttonConnectBT onClick: " + btPairedDevicesList.get((int)positionInListView).toString() );

                        try {
                            bluetooth.connectToDevice(btPairedDevicesList.get((int)positionInListView));
                            MainActivity.self.chimneyManager.settings.Bluetooth_Mac = btPairedDevicesList.get((int)positionInListView).getAddress();
                            MainActivity.self.chimneyManager.settings2file();
                            Log.d("doff-bt", "address: "+btPairedDevicesList.get((int)positionInListView).getAddress());
                            Log.d("doff-bt", "name: "+btPairedDevicesList.get((int)positionInListView).getName());
                            Toast toast = Toast.makeText(MainActivity.self, "Bluetooth Pairing Done", Toast.LENGTH_SHORT);
                            toast.show();

                        } catch (Exception e) {
                            Log.e("doff-bt",e.getMessage());
                            Toast toast = Toast.makeText(MainActivity.self, "Bluetooth Pairing ERROR", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        MainActivity.self.mViewPager.setCurrentItem(MainActivity.PageMain);
                        Log.d("doff-bt","buttonConnectBT onClick: " + bluetooth.toString());
                    }
                });
                buttonConnectBT.setVisibility(Button.INVISIBLE);

            }
            return rootView;
        }
    }
    public static class FragmentBluetoothSerialCommunication extends Fragment  implements IChimney {
        public String title() { return getString(R.string.btc_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private Bluetooth bluetooth = null;
        private  Button buttonSend = null;
        private EditText editTextSend = null;
        private EditText editTextRecive = null;

        public FragmentBluetoothSerialCommunication() {
        }
        public static FragmentBluetoothSerialCommunication newInstance(int sectionNumber, Bluetooth bluetooth) {
            Log.d("doff-bt","New fragment.");
            final FragmentBluetoothSerialCommunication fragment = new FragmentBluetoothSerialCommunication();
            fragment.bluetooth = bluetooth;

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bluetooth_serial_communication, container, false);

            this.editTextSend = (EditText) rootView.findViewById(R.id.edittext_bt_send);
            if (this.editTextSend != null) {
                this.editTextSend.setText("", TextView.BufferType.EDITABLE);
            }

            this.buttonSend = (Button) rootView.findViewById(R.id.button_bt_communication_tx);
            if ( buttonSend != null ) {
                Log.d("doff-bt","Create button listener.");
                //buttonReadGPS.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                buttonSend.setBackgroundColor(0xFF0000FF);
                buttonSend.setTextColor(0xFFFFFFFF);
                buttonSend.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = editTextSend.getText().toString();
                        if ( str.compareTo("") != 0)
                            bluetooth.send(str+"\n\r");
                        else {
                            Toast toast = Toast.makeText(MainActivity.self, "Tom sträng", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                });
            }

            this.editTextRecive = (EditText) rootView.findViewById(R.id.edittext_bt_received);
            if (this.editTextRecive != null) {
                this.editTextRecive.setText("", TextView.BufferType.EDITABLE);
            }
            MainActivity.self.fragmentFBSC = this;
            return rootView;
        }
    }
    public static class FragmentPDF extends Fragment  implements IChimney {
        public String title() { return getString(R.string.pdf_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private String[] fileNames = null;
        int index = 0;
        private Button buttonPlus = null;
        private Button buttonMinus = null;
        private Button buttonHome = null;
        private TextView textViewFileName = null;
        private ImageView imageView = null;

        public FragmentPDF() {
            MainActivity.self.fragmentPDF = this;
        }

        public static FragmentPDF newInstance(int sectionNumber) {
            Log.d("doff-pdf","New fragment.");
            final FragmentPDF fragment = new FragmentPDF();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create_pdf, container, false);
            fileNames = FileManager.listAllPdfFileNamesInExternalDirectory(MainActivity.self);

            textViewFileName = (TextView) rootView.findViewById(R.id.textViewPdfFileName);

            buttonPlus = (Button) rootView.findViewById(R.id.button_pdf_plus);
            if (buttonPlus != null) {
                Log.d("doff-pdf","Create button listener.");

                buttonPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       index = ++index >= fileNames.length ? index=0 : index;
                        if ( fileNames.length > 0 ) {
                            PdfRender pdfRender = new PdfRender(MainActivity.self, imageView);
                            pdfRender.render(fileNames[index]);
                            textViewFileName.setText(Pdf.SpannableStringFileName(fileNames[index]));
                        }
                    }
                });
            }

            buttonMinus = (Button) rootView.findViewById(R.id.button_pdf_minus);
            if (buttonMinus != null) {
                Log.d("doff-pdf","Create button listener.");

                buttonMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( fileNames.length > 0 ) {
                            index = --index < 0 ? index=fileNames.length-1 : index;
                            PdfRender pdfRender = new PdfRender(MainActivity.self, imageView);
                            pdfRender.render(fileNames[index]);
                            textViewFileName.setText(Pdf.SpannableStringFileName(fileNames[index]));
                        }
                    }
                });
            }

            buttonHome = (Button) rootView.findViewById(R.id.button_pdf_home);
            if (buttonHome != null) {
                Log.d("doff-pdf","Create button listener.");

                buttonHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( fileNames.length > 0 ) {
                            index = 0;
                            PdfRender pdfRender = new PdfRender(MainActivity.self, imageView);
                            pdfRender.render(fileNames[index]);
                            textViewFileName.setText(Pdf.SpannableStringFileName(fileNames[index]));
                        }
                    }
                });
            }

            imageView = (ImageView) rootView.findViewById(R.id.imageViewPdf);

            return rootView;
        }
    }
    public static class FragmentEmail extends Fragment  implements IChimney {
        public String title() { return getString(R.string.email_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private  Button buttonSend = null;

        public FragmentEmail() {
        }

        public static FragmentEmail newInstance(int sectionNumber) {
            Log.d("doff-email","New fragment.");
            final FragmentEmail fragment = new FragmentEmail();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_email, container, false);

            this.buttonSend = (Button) rootView.findViewById(R.id.button_email_send);
            if (this.buttonSend != null ) {
                buttonSend.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GMailSend.testWithAttachment();
                    }
                });
            }
            return rootView;
        }
    }
    public static class FragmentGPS extends Fragment  implements IChimney {
        public String title() { return getString(R.string.gps_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private  Button buttonReadGPS = null;
        private boolean doRestart = false;
        private  TextView textViewLatitude = null;
        private  TextView textViewLongitude = null;
        private  TextView textViewPlace = null;

        public FragmentGPS() {
        }

        public static FragmentGPS newInstance(int sectionNumber) {
            Log.d("doff-main","New fragment.");
            final FragmentGPS fragment = new FragmentGPS();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void onClickButtonReadGPS()
        {
            GPS gps = MainActivity.self.chimneyManager.gps;

            Log.d("doff-main","askToOnLocation: " + gps.tracker.isLocationEnabled());

            if ( doRestart )
                MainActivity.self.restartApp();

            if( !gps.tracker.isLocationEnabled() )
            {
                Log.d("doff-main","askToOnLocation");
                gps.tracker.askToOnLocation();
                doRestart = true;
                buttonReadGPS.setText("Restart App");
                return;
            }
            double latitude = gps.tracker.getLatitude();
            double longitude =gps.tracker.getLongitude();
            gps.latitudeLongitude = String.format("[%f,%f]", latitude,longitude);
            this.textViewLatitude.setText(gps.BuildTextLongitudeLatitude(false, false, latitude));
            this.textViewLongitude.setText(gps.BuildTextLongitudeLatitude(false, true, longitude));
            Log.d("doff-main","Update gps");
            String addres= gps.tracker.getCompleteAddressString(latitude,longitude).replace(',', '\n');
            textViewPlace.setText(gps.BuildTextPlace( addres ));
            /*
            {
            double latitude=tracker.getLatitude();
            double longitude=tracker.getLongitude();
            location.setText("Your Location is Latitude= " + latitude + " Longitude= " + longitude);
            String addres= getCompleteAddressString(latitude,longitude);
            address.setText(addres);
        }
        else
             */
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gps, container, false);
            GPS gps = MainActivity.self.chimneyManager.gps;

            buttonReadGPS = (Button) rootView.findViewById(R.id.button_read_gps);
            if (buttonReadGPS != null) {
                Log.d("doff-main","Create button listener.");
                buttonReadGPS.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButtonReadGPS();
                    }
                });
            }

            textViewLongitude = (TextView) rootView.findViewById(R.id.gps_textview_longitude);
            textViewLongitude.setText(gps.BuildTextLongitudeLatitude(true, true, 0));

            textViewLatitude = (TextView) rootView.findViewById(R.id.gps_textview_latitude);
            textViewLatitude.setText(gps.BuildTextLongitudeLatitude(true, false, 0));

            textViewPlace = (TextView) rootView.findViewById(R.id.gps_textview_place);
            textViewPlace.setText(gps.BuildTextPlace( "" ));

            return rootView;
        }
    }
    public static class FragmentFiles extends Fragment  implements IChimney {
        public String title() { return getString(R.string.files_title); }
        public void init() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        private View rootView = null;
        private ListViewAdapter_ listViewAdapter = null;
        private ListView listView = null;
        private Button buttonDelete = null;
        private Button buttonMail = null;

        public FragmentFiles() {
        }

        public static FragmentFiles newInstance(int sectionNumber) {
            Log.d("doff-main","New fragment.");
            final FragmentFiles fragment = new FragmentFiles();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_files, container, false);

            buttonDelete = (Button) rootView.findViewById(R.id.buttonFilesDelete) ;
            if ( buttonDelete != null ) {
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        for (int i = 0; i < arrayColumn1.length; i++) {
                            if ( arrayColumn1[i] == true ) {
                                FileManager.deleteExternalFile(MainActivity.self, arrayColumn2[i]);
                            }
                        }
                        fillListView();
                        listViewAdapter = new ListViewAdapter_();
                        listView.setAdapter(listViewAdapter);
                    }
                });
            }

            buttonMail = (Button) rootView.findViewById(R.id.buttonFilesMail) ;
            if ( buttonMail != null ) {
                buttonMail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        for (int i = 0; i < arrayColumn1.length; i++) {
                            if ( arrayColumn1[i] == true ) {
                                //FileManager.deleteExternalFile(MainActivity.self, arrayColumn2[i]);
                            }
                        }
                    }
                });
            }

            fillListView();
            listViewAdapter = new ListViewAdapter_();
            listView = (ListView) rootView.findViewById(R.id.listViewFiles);
            listView.setAdapter(listViewAdapter);

            return rootView;
        }

        private void fillListView() {
            arrayColumn2 = FileManager.listAllFileNamesInExternalDirectory(MainActivity.self);
            arrayColumn1 = new Boolean[arrayColumn2.length];
            for (int i = 0; i < arrayColumn1.length; i++) {
                arrayColumn1[i]=false;
            }
        }
        private static String[] arrayColumn2 =
                new String[]{"Text1","Text2","Text3","Text4"
                        ,"Text5","Text6","Text7","Text8","Text9","Text10"
                        ,"Text11","Text12","Text13","Text14","Text15"
                        ,"Text16","Text17","Text18","Text19","Text20"
                        ,"Text21","Text22","Text23","Text24"};
        private static Boolean[] arrayColumn1 =
                new Boolean[]{false,false,false,false
                        ,false,false,false,false,false,false
                        ,false,false,false,false,false
                        ,false,false,false,false,false
                        ,false,false,false,false};
        private static class ListViewAdapter_ extends BaseAdapter {

            @Override public int getCount() {
                // TODO Auto-generated method stub
                if(arrayColumn2 != null && arrayColumn2.length != 0){
                    return arrayColumn2.length;
                }
                return 0;
            }
            @Override public Object getItem(int position) {
                // TODO Auto-generated method stub
                return arrayColumn2[position];
            }
            @Override public long getItemId(int position) {
                // TODO Auto-generated method stub
                return position;
            }
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                //ViewHolder holder = null;
                final ViewHolder holder;
                if (convertView == null) {

                    holder = new ViewHolder();
                    LayoutInflater inflater = MainActivity.self.getLayoutInflater();
                    convertView = inflater.inflate(R.layout.listview_item_files, null);
                    holder.textView1 = (TextView) convertView.findViewById(R.id.textViewFiles);
                    holder.checkBox1 = (CheckBox) convertView.findViewById(R.id.checkBoxFiles);
                    holder.checkBox1.setText("");
                    convertView.setTag(holder);

                } else {

                    holder = (ViewHolder) convertView.getTag();
                }

                holder.ref = position;

                holder.textView1.setText(arrayColumn2[position]);
                holder.checkBox1.setChecked(arrayColumn1[position]);
                holder.checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        arrayColumn1[holder.ref] = b;
                    }
                });

                return convertView;
            }

            private class ViewHolder {
                TextView textView1;
                CheckBox checkBox1;
                int ref;
            }


        }
    }

    class ChimneyThread extends Thread {

        ChimneyThread() {
            Log.d("doff-chimney", "ChimneyThread constructor");
        }

        public void run() {
            ChimneyManager cm = MainActivity.self.chimneyManager;


            try {
                while ( true ) {
                    boolean isBluetoothSendOK = true;
                    Log.d("doff-chimney", "ChimneyThread");
                    switch (cm.state) {
                        case Idle:
                            Log.d("doff-chimney", "State: Idle");
                            break;
                        case Leakage:
                            Log.d("doff-chimney", "State: Leakage");
                            break;
                        case Manual:
                            Log.d("doff-chimney", "State: Manual");
                            MainActivity.self.handlerManual.sendMessage(new Message());
                            isBluetoothSendOK = cm.protocol.txGetSensorSignals();
                            break;
                        case BatteryTest:
                            Log.d("doff-chimney", "State: BatteryTest");
                            MainActivity.self.handlerBatteryTest.sendMessage(new Message());
                            isBluetoothSendOK = cm.protocol.txBatteryStatus();
                            break;
                    }
                    if ( isBluetoothSendOK == false ) {
                        //@todo rapportering måste göras i UI-tråden
                        MainActivity.self.handlerBluetoothSendError.sendMessage(new Message());
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e("doff-chimney", e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
