package com.xql.safehaven;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xql.safehaven.databinding.CardEnergylevelBinding;
import com.xql.safehaven.databinding.CardRestingstatusBinding;
import com.xql.safehaven.databinding.CardWydBinding;
import com.xql.safehaven.databinding.DialogProgressbarBinding;
import com.xql.safehaven.databinding.DialogRestingstatusBinding;
import com.xql.safehaven.databinding.DialogWydBinding;
import com.xql.safehaven.databinding.FragmentHomeBinding;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String userId;
    String myRestStatus, myRestNote, myEnergyNote, myWydNote;
    Long myEnergyProgress;
    Timestamp myRestUpdate, myEnergyUpdate, myWydUpdate;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(currentUser == null){
            Intent intent = new Intent(requireContext(), HomepageActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
        else{
            userId = currentUser.getUid();
        }

        CardRestingstatusBinding cardRestingstatusBinding = CardRestingstatusBinding.bind(binding.getRoot().findViewById(R.id.cardRestingStatus));
        CardRestingstatusBinding partnerRestingstatusBinding = CardRestingstatusBinding.bind(binding.getRoot().findViewById(R.id.partnerRestingStatus));
        CardEnergylevelBinding cardEnergylevelBinding = CardEnergylevelBinding.bind(binding.getRoot().findViewById(R.id.cardEnergyLevel));
        CardWydBinding cardWydBinding = CardWydBinding.bind(binding.getRoot().findViewById(R.id.cardWyd));

        fetchMyStatus();

        cardRestingstatusBinding.main.setOnClickListener(v -> {
            showSleepDialog(userId);
        });
        cardEnergylevelBinding.main.setOnClickListener(v -> {
            showProgressDialog(userId, "energy");
        });
        cardWydBinding.main.setOnClickListener(v -> {
            showWydDialog();
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchMyStatus();
            new Handler().postDelayed(() -> {
                binding.swipeRefreshLayout.setRefreshing(false);
            }, 2000);
        });


        return binding.getRoot();
    }

    private void fetchMyStatus(){
        db.collection("status").document(userId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                String restStatus = document.getString("restStatus");
                Timestamp restUpdate = document.getTimestamp("restUpdate");
                String restNote = document.getString("restNote");
                String partnerId = document.getString("partnerId");

                if (restStatus != null) {
                    myRestStatus = restStatus;
                    if(restStatus.equals("Awake")){
                        binding.cardRestingStatus.restStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
                        binding.cardRestingStatus.timeLabel.setText("Wake-up time: ");
                    }
                    else{
                        binding.cardRestingStatus.restStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.rosyRed));
                        binding.cardRestingStatus.timeLabel.setText("Sleep time: ");
                    }
                    binding.cardRestingStatus.restStatus.setText(restStatus);
                }

                if (restUpdate != null) {
                    myRestUpdate = restUpdate;
                    Date date = restUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                    binding.cardRestingStatus.restUpdate.setText(capitalizedDate);
                }

                if (restNote != null){
                    binding.cardRestingStatus.restNote.setVisibility(View.VISIBLE);
                    if(restNote.isEmpty()){
                        binding.cardRestingStatus.restNote.setVisibility(View.GONE);
                    }
                    else{
                        myRestNote = restNote;
                        binding.cardRestingStatus.restNote.setText("\""+restNote+"\"");
                    }

                }
                else{
                    binding.cardRestingStatus.restNote.setVisibility(View.GONE);
                }

                Long energyProgress = document.getLong("energyProgress");
                String energyNote = document.getString("energyNote");
                Timestamp energyUpdate = document.getTimestamp("energyUpdate");

                if(energyProgress != null){
                    myEnergyProgress = energyProgress;
                    binding.cardEnergyLevel.energyProgressBar.setProgress(energyProgress.intValue());
                    binding.cardEnergyLevel.energyProgessText.setText(String.valueOf(energyProgress.intValue())+"%");
                }
                if(energyUpdate != null){
                    myEnergyUpdate = energyUpdate;
                    Date date = energyUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                    binding.cardEnergyLevel.energyUpdate.setText(capitalizedDate);
                }
                if(energyNote != null){
                    binding.cardEnergyLevel.note.setVisibility(View.VISIBLE);
                    if (energyNote.isEmpty()){
                        binding.cardEnergyLevel.note.setVisibility(View.GONE);
                    }
                    else{
                        myEnergyNote = energyNote;
                        binding.cardEnergyLevel.note.setText("\""+energyNote+"\"");
                    }
                }
                else{
                    binding.cardEnergyLevel.note.setVisibility(View.GONE);
                }

                String wydNote = document.getString("wydNote");
                Timestamp wydUpdate = document.getTimestamp("wydUpdate");
                if(wydNote != null){
                    myWydNote = wydNote;
                    binding.cardWyd.note.setText("\""+wydNote+"\"");
                }
                if(wydUpdate != null){
                    myWydUpdate = wydUpdate;
                    Date date = wydUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                    binding.cardWyd.wydUpdate.setText(capitalizedDate);
                }

                fetchPartner(partnerId);

            }
        });
    }
    private void fetchPartner(String partnerId){
        db.collection("status").document(partnerId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                String restStatus = document.getString("restStatus");
                Timestamp restUpdate = document.getTimestamp("restUpdate");
                String restNote = document.getString("restNote");

                if (restStatus != null) {
                    if(restStatus.equals("Awake")){
                        binding.partnerRestingStatus.restStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
                        binding.partnerRestingStatus.timeLabel.setText("Wake-up time: ");
                    }
                    else{
                        binding.partnerRestingStatus.restStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.rosyRed));
                        binding.partnerRestingStatus.timeLabel.setText("Sleep time: ");
                    }
                    binding.partnerRestingStatus.restStatus.setText(restStatus);
                }

                if (restUpdate != null) {
                    Date date = restUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                    binding.partnerRestingStatus.restUpdate.setText(capitalizedDate);
                }

                binding.partnerRestingStatus.restNote.setVisibility(View.VISIBLE);
                if (restNote != null){
                    if(restNote.isEmpty()){
                        binding.partnerRestingStatus.restNote.setVisibility(View.GONE);
                    }
                    else{
                        binding.partnerRestingStatus.restNote.setText("\""+restNote+"\"");
                    }

                }
                else{
                    binding.partnerRestingStatus.restNote.setVisibility(View.GONE);
                }

                Long energyProgress = document.getLong("energyProgress");
                String energyNote = document.getString("energyNote");
                Timestamp energyUpdate = document.getTimestamp("energyUpdate");

                if(energyProgress != null){
                    binding.partnerEnergyLevel.energyProgressBar.setProgress(energyProgress.intValue());
                    binding.partnerEnergyLevel.energyProgessText.setText(String.valueOf(energyProgress.intValue())+"%");
                }
                if(energyUpdate != null){
                    Date date = energyUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                    binding.partnerEnergyLevel.energyUpdate.setText(capitalizedDate);
                }
                if(energyNote != null){
                    binding.partnerEnergyLevel.note.setVisibility(View.VISIBLE);
                    if (energyNote.isEmpty()){
                        binding.partnerEnergyLevel.note.setVisibility(View.GONE);
                    }
                    else{
                        binding.partnerEnergyLevel.note.setText("\""+energyNote+"\"");
                    }
                }
                else{
                    binding.partnerEnergyLevel.note.setVisibility(View.GONE);
                }

                String wydNote = document.getString("wydNote");
                Timestamp wydUpdate = document.getTimestamp("wydUpdate");
                if(wydNote != null){
                    binding.partnerWyd.note.setText("\""+wydNote+"\"");
                }
                if(wydUpdate != null){
                    Date date = wydUpdate.toDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy h:mma", Locale.getDefault());
                    String formattedDate = formatter.format(date).toLowerCase();
                    String capitalizedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                    binding.partnerWyd.wydUpdate.setText(capitalizedDate);
                }

            }
        });
    }
    private void showSleepDialog(String userId) {
        DialogRestingstatusBinding binding = DialogRestingstatusBinding.inflate(getLayoutInflater());

        int rosyRed = ContextCompat.getColor(requireContext(), R.color.rosyRed);
        int success = ContextCompat.getColor(requireContext(), R.color.success);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();

        if(myRestStatus != null){
            binding.switchSleep.setText(myRestStatus);
            if(myRestStatus.equals("Awake")){
                binding.switchSleep.setChecked(true);
                binding.switchSleep.setThumbTintList(ColorStateList.valueOf(success));
            }
            else{
                binding.switchSleep.setChecked(false);
                binding.switchSleep.setThumbTintList(ColorStateList.valueOf(rosyRed));
            }
        }

        if(myRestNote != null && !myRestNote.isEmpty()){
            binding.note.setText(myRestNote);
        }

        binding.switchSleep.setOnClickListener(v -> {
            if (binding.switchSleep.isChecked()) {
                binding.switchSleep.setText("Awake");
                binding.switchSleep.setThumbTintList(ColorStateList.valueOf(success));
            }
            else {
                binding.switchSleep.setText("Asleep");
                binding.switchSleep.setThumbTintList(ColorStateList.valueOf(rosyRed));
            }
        });

        binding.confirm.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            String restNote = binding.note.getText().toString();

            if (binding.switchSleep.isChecked()) {
                String restStatus = "Awake";
                Timestamp restUpdate = Timestamp.now();
                submitRestingStatus(userId, restStatus, restUpdate, restNote);

            } else {
                String restStatus = "Asleep";
                Timestamp restUpdate = Timestamp.now();
                submitRestingStatus(userId, restStatus, restUpdate, restNote);
            }
            dialog.dismiss();
            fetchMyStatus();
        });
        dialog.show();

    }
    private void submitRestingStatus(String userId, String restStatus, Timestamp restUpdate, String restNote){
        Map<String, Object> status = new HashMap<>();
        status.put("restStatus", restStatus);
        status.put("restUpdate", restUpdate);
        status.put("restNote", restNote);

        db.collection("status").document(userId).update(status).addOnSuccessListener(aVoid -> {
            Map<String, Object> logs = new HashMap<>();
            logs.put("userId", userId);
            logs.put("datetime", restUpdate);
            logs.put("category", "rest");
            logs.put("restStatus", restStatus);
            logs.put("restNote", restNote);
            db.collection("logs").add(logs);

            Toast.makeText(requireContext(), "Status saved successfully.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });


    }
    private void showProgressDialog(String userId, String category){
        DialogProgressbarBinding binding = DialogProgressbarBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        int progress = 0;
        String note = "";

        if(category.equals("energy")){
            if(myEnergyProgress != null){
                progress = myEnergyProgress.intValue();
            }
            if(myEnergyNote != null){
                note = myEnergyNote;
            }
        }

        if (myEnergyProgress != null) {
            binding.seekBar.setProgress(progress);
            binding.progressText.setText("Energy Level: " + String.valueOf(progress)+"%");

        }
        if(note != null && !note.isEmpty()){
            binding.note.setText(note);
        }

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.progressText.setText("Energy Level: " + String.valueOf(progress)+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.confirm.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            String submitNote = binding.note.getText().toString();
            int submitProgress = binding.seekBar.getProgress();

            String noteField = category+"Note";
            String progressField = category+"Progress";
            String updateField = category+"Update";

            Map<String, Object> updates = new HashMap<>();
            updates.put(noteField, submitNote);
            updates.put(progressField, submitProgress);
            updates.put(updateField, Timestamp.now());

            db.collection("status").document(userId).update(updates).addOnSuccessListener(aVoid -> {
                Map<String, Object> logs = new HashMap<>();
                logs.put("userId", userId);
                logs.put("datetime", Timestamp.now());
                logs.put("category", category);
                logs.put(progressField, submitProgress);
                logs.put(noteField, submitNote);
                db.collection("logs").add(logs);
                Toast.makeText(requireContext(), "Status saved successfully.", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

            dialog.dismiss();
            fetchMyStatus();
        });


        dialog.show();
    }

    private void showWydDialog(){
        DialogWydBinding binding = DialogWydBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();

        if(myWydNote != null && !myWydNote.isEmpty()){
            binding.note.setText(myWydNote);
        }

        binding.confirm.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            String wydNote = binding.note.getText().toString();
            Timestamp wydUpdate = Timestamp.now();
            submitWyd(wydNote, wydUpdate);
            dialog.dismiss();
            fetchMyStatus();
        });

        dialog.show();
    }

    private void submitWyd(String wydNote, Timestamp wydUpdate){
        Map<String, Object> status = new HashMap<>();
        status.put("wydNote", wydNote);
        status.put("wydUpdate", wydUpdate);

        db.collection("status").document(userId).update(status).addOnSuccessListener(aVoid -> {
            Map<String, Object> logs = new HashMap<>();
            logs.put("userId", userId);
            logs.put("datetime", wydUpdate);
            logs.put("category", "wyd");
            logs.put("wydNote", wydNote);
            db.collection("logs").add(logs);

            Toast.makeText(requireContext(), "Status saved successfully.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}