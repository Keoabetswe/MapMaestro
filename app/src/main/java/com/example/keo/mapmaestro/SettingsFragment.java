package com.example.keo.mapmaestro;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment //implements TextToSpeech.OnInitListener
{
    TextToSpeech tts;
    TextView settings;

    //widgets
    RadioGroup radioGroupUnits;
    RadioGroup radioGroupMode; //Mode - mode of transport
    RadioButton radUnits;
    RadioButton radMode;

    //shared prefs
    SharedPreferences unitsPrefs;
    SharedPreferences getUnitsData;
    SharedPreferences modePrefs;
    SharedPreferences getModeData;
    SharedPreferences voicePrefs;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    public static final String SWITCH = "SWITCH";
    public static final String MODE_OF_TRANSPORT = "MOT";
    public static final String UNIT_TYPES = "UNITS";


    Boolean isVoiceOn;
    TextView tvSwitchStatus;

    Switch switchVoiceAssistant;
    public static final String KEY_PREF_EXAMPLE_SWITCH = "example_switch";

    Button btnSaveSettings;
    String unitText;
    String modeText;

    private boolean voiceOnOFF;
    private boolean radUnitSelected;
    private boolean radModeSelected;



    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        btnSaveSettings = v.findViewById(R.id.btnSettingsSave);
        radioGroupUnits = v.findViewById(R.id.radUnitTypes);
        radioGroupMode = v.findViewById(R.id.radModeOfTransport);
 //       switchVoiceAssistant = v.findViewById(R.id.switchVoice);


        //tts = new TextToSpeech(getActivity(), this);

        //Units shared prefs
        unitsPrefs = getActivity().getSharedPreferences("unitsPrefSettings", MODE_PRIVATE);
        getUnitsData = getActivity().getSharedPreferences("unitsPrefSettings", MODE_PRIVATE);

        //Mode of transport shared prefs
        modePrefs = getActivity().getSharedPreferences("modePrefSettings", MODE_PRIVATE);
        getModeData = getActivity().getSharedPreferences("modePrefSettings", MODE_PRIVATE);

        //settings shared prefs

        //sets the selected unit type and mode of transport (radio buttons)
        //setUnitType();
        //setModeOfTransport();
        //setVoiceAssistance();

        btnSaveSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                saveSettings();
            }
        });

        //updates and loads the current settings
        loadSettings();
        //updateVoiceSettings();

        return v;
    }

    public void saveSettings()
    {
        //radio button selected to be saved
        int radUnitId = radioGroupUnits.getCheckedRadioButtonId();
        int radModeId = radioGroupMode.getCheckedRadioButtonId();

        radUnits = getActivity().findViewById(radUnitId);
        radMode = getActivity().findViewById(radModeId);

        SharedPreferences.Editor unitsEditor = unitsPrefs.edit();
        SharedPreferences.Editor modeEditor = modePrefs.edit();

        String units = radUnits.getText().toString();
        String modeOfTrans = radMode.getText().toString();

        unitsEditor.putString("units_text_key",units);
        modeEditor.putString("mode_text_key",modeOfTrans);

        //stores units & Mode of transport data permanently
        unitsEditor.commit();
        modeEditor.commit();

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();


        editor.putBoolean(UNIT_TYPES, radioGroupUnits.isSelected());
        editor.putBoolean(MODE_OF_TRANSPORT, radioGroupMode.isSelected());
        editor.apply();

        Toast.makeText(getActivity(), "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    public void loadSettings()
    {
       // SharedPreferences sharedPrefs = getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //voiceOnOFF = sharedPrefs.getBoolean(SWITCH, false);

        setUnitType();
        setModeOfTransport();

    }

    public void updateVoiceSettings()
    {
        switchVoiceAssistant.setChecked(voiceOnOFF);
    }

    public void setUnitType()
    {
        unitText = getUnitsData.getString("units_text_key", "Metric");

        if(unitText.equals("Imperial"))
        {
            radioGroupUnits.check(R.id.radImperial);
        }
        else if(unitText.equals("Metric"))
        {
            radioGroupUnits.check(R.id.radMetric);
        }
    }

    public void setModeOfTransport()
    {
        modeText = getModeData.getString("mode_text_key", "Driving");

        if(modeText.equals("Driving"))
        {
            radioGroupMode.check(R.id.radDriving);
        }
        else if(modeText.equals("Walking"))
        {
            radioGroupMode.check(R.id.radWalking);
        }
    }

    /*public void setVoiceAssistance()
    {
        voiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if(isChecked)
                {
                    tvSwitchStatus.setText("Voice On");
                    tvSwitchStatus.setVisibility(View.VISIBLE);
                    //voiceOutput();
                }
                else
                {
                    tvSwitchStatus.setText("Voice Off");
                }
            }
        });
    }

    //Text to Speech function reads voice assistant is activated
    public void voiceOutput()
    {
        String voiceOn = "Voice Assistant Activated";
        tts.speak(voiceOn, TextToSpeech.QUEUE_FLUSH, null, "id1");
    }

    @Override
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            //set language, you can change to any built in language given we are using US language
            int result = tts.setLanguage(Locale.US);
            float i = 50;

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Toast.makeText(getActivity(), "Language not supported!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                voiceOutput();
            }
        }
        else
        {
            Toast.makeText(getActivity(), "Initialization Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView()
    {
        //shuts down TextToSpeech, saves resources
        if(tts != null)
        {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroyView();
    }*/
}
